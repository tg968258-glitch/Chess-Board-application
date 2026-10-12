package com.chess.chessboard.Controller;

import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.model.Board;
import com.chess.chessboard.service.ChessGameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chess")
@CrossOrigin(origins = "http://localhost:5173")
public class ChessGameController {
    private final ChessGameService chessGameService;
    public ChessGameController(ChessGameService chessGameService) {
        this.chessGameService = chessGameService;
    }
    @PostMapping("/start")
    public ResponseEntity<String> startGame() {
        chessGameService.startGame();
        return ResponseEntity.ok("Chess game started");
    }
    @PostMapping("/move")
    public ResponseEntity<String> makeMove(@RequestBody MoveRequest request) {
        if (request.fromRow() == null || request.fromColumn() == null
                || request.toRow() == null || request.toColumn() == null
                || request.moveType() == null) {
            return ResponseEntity.badRequest().body("Missing move details");
        }
        boolean success = chessGameService.makeMove(
                request.fromRow(),
                request.fromColumn(),
                request.toRow(),
                request.toColumn(),
                request.moveType()
        );
        if (success) {
            return ResponseEntity.ok("Move successful");
        }
        return ResponseEntity.badRequest().body("Invalid move");}
    @GetMapping("/board")
    public Board getBoard() {
        return chessGameService.getBoard();
    }
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return Map.of(
                "status", chessGameService.getStatus(),
                "currentTurn", chessGameService.getGame().getCurrentTurn()
        );
    }
    public record MoveRequest(
            Integer fromRow,
            Integer fromColumn,
            Integer toRow,
            Integer toColumn,
            MoveType moveType
    ){}
}




