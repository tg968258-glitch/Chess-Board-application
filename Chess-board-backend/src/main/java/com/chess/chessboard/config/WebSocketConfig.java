package com.chess.chessboard.config;

import com.chess.chessboard.websocket.ChessWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChessWebSocketHandler chessWebSocketHandler;
    private final String allowedOrigin;

    public WebSocketConfig(
            ChessWebSocketHandler chessWebSocketHandler,
            @Value("${app.security.allowed-origin}") String allowedOrigin) {
        this.chessWebSocketHandler = chessWebSocketHandler;
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chessWebSocketHandler, "/ws/chess")
                .setAllowedOrigins(allowedOrigin.split(","));
    }
}
