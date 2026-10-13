package com.chess.chessboard.websocket;

import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.enums.PieceType;
import com.chess.chessboard.service.ChessGameService;
import com.chess.chessboard.service.PlayerAccountService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChessWebSocketHandler extends TextWebSocketHandler {
    private final ChessGameService chessGameService;
    private final AuthenticationManager authenticationManager;
    private final PlayerAccountService playerAccountService;
    private final ObjectMapper objectMapper;
    private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();

    public ChessWebSocketHandler(
            ChessGameService chessGameService,
            AuthenticationManager authenticationManager,
            PlayerAccountService playerAccountService) {
        this.chessGameService = chessGameService;
        this.authenticationManager = authenticationManager;
        this.playerAccountService = playerAccountService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), new ClientSession(
                new ConcurrentWebSocketSessionDecorator(session, 5_000, 64 * 1024)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        ClientSession client = sessions.get(session.getId());
        if (client == null) return;

        try {
            JsonNode payload = objectMapper.readTree(message.getPayload());
            String type = payload.path("type").asText();

            if ("AUTHENTICATE".equals(type)) {
                authenticate(client, payload);
                return;
            }
            if ("REGISTER".equals(type)) {
                register(client, payload);
                return;
            }
            if (client.username == null) {
                send(client, Map.of("type", "ERROR", "code", "UNAUTHORIZED", "message", "Authenticate before sending game commands."));
                return;
            }
            if ("START_GAME".equals(type)) {
                synchronized (chessGameService) {
                    chessGameService.startGame();
                    broadcastState("GAME_STARTED", null);
                }
                return;
            }
            if ("MOVE".equals(type)) {
                handleMove(client, payload);
                return;
            }
            if ("UNDO".equals(type)) {
                synchronized (chessGameService) {
                    if (chessGameService.undoLastMove()) {
                        broadcastState("MOVE_UNDONE", null);
                    } else {
                        send(client, Map.of("type", "ERROR", "code", "NOTHING_TO_UNDO", "message", "There are no moves to undo."));
                    }
                }
                return;
            }
            send(client, Map.of("type", "ERROR", "code", "UNKNOWN_COMMAND", "message", "Unsupported WebSocket command."));
        } catch (IllegalArgumentException exception) {
            send(client, Map.of("type", "ERROR", "code", "INVALID_MESSAGE", "message", "The WebSocket message is invalid."));
        }
    }

    private void authenticate(ClientSession client, JsonNode payload) throws IOException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            payload.path("username").asText(), payload.path("password").asText()));
            client.username = authentication.getName();
            send(client, Map.of("type", "AUTHENTICATED", "username", client.username));
            sendState(client, "SYNC", null);
        } catch (Exception exception) {
            send(client, Map.of("type", "ERROR", "code", "AUTHENTICATION_FAILED", "message", "Incorrect username or password."));
        }
    }

    private void register(ClientSession client, JsonNode payload) throws IOException {
        String username = payload.path("username").asText();
        String password = payload.path("password").asText();
        PlayerAccountService.RegistrationResult result = playerAccountService.register(username, password);
        if (!result.successful()) {
            send(client, Map.of("type", "ERROR", "code", "REGISTRATION_FAILED", "message", result.message()));
            return;
        }
        authenticate(client, payload);
    }

    private void handleMove(ClientSession client, JsonNode payload) throws IOException {
        JsonNode move = payload.path("move");
        boolean success;
        synchronized (chessGameService) {
            success = chessGameService.makeMove(
                    move.path("fromRow").asInt(-1), move.path("fromColumn").asInt(-1),
                    move.path("toRow").asInt(-1), move.path("toColumn").asInt(-1),
                    MoveType.valueOf(move.path("moveType").asText()),
                    move.hasNonNull("promotionPieceType")
                            ? PieceType.valueOf(move.path("promotionPieceType").asText()) : null);
            if (success) broadcastState("MOVE", move);
        }
        if (!success) {
            send(client, Map.of("type", "ERROR", "code", "INVALID_MOVE", "message", "That move is not legal."));
        }
    }

    private void broadcastState(String event, JsonNode move) {
        sessions.values().stream().filter(client -> client.username != null).forEach(client -> {
            try { sendState(client, event, move); } catch (IOException ignored) { }
        });
    }

    private void sendState(ClientSession client, String event, JsonNode move) throws IOException {
        Map<String, Object> state = new java.util.LinkedHashMap<>();
        state.put("type", "STATE");
        state.put("event", event);
        state.put("board", chessGameService.getBoard());
        state.put("status", chessGameService.getStatus());
        state.put("currentTurn", chessGameService.getGame().getCurrentTurn());
        if (move != null) state.put("move", move);
        send(client, state);
    }

    private void send(ClientSession client, Object payload) throws IOException {
        if (client.session.isOpen()) {
            client.session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }

    private static final class ClientSession {
        private final WebSocketSession session;
        private volatile String username;
        private ClientSession(WebSocketSession session) { this.session = session; }
    }
}
