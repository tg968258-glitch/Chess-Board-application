package com.chess.chessboard.service;

import com.chess.chessboard.enums.GameStatus;
import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.model.*;
import com.chess.chessboard.pieces.Piece;
import org.springframework.stereotype.Service;

@Service
public class ChessGameService {
    private ChessGame chessGame;
    public ChessGameService() {startGame();}
    public void startGame() {
        Player white = new Player("White", com.chess.chessboard.enums.Color.WHITE);
        Player black = new Player("Black", com.chess.chessboard.enums.Color.BLACK);
        chessGame = new ChessGame(white, black);
        chessGame.start();
    }
    public boolean makeMove(int fromRow, int fromColumn, int toRow, int toColumn, MoveType moveType) {
        if (!isValidPosition(fromRow, fromColumn)
                || !isValidPosition(toRow, toColumn)
                || moveType == null) {
            return false;
        }
        Position from = new Position(fromRow, fromColumn);
        Position to = new Position(toRow, toColumn);

        Piece piece = chessGame.getBoard().getSquare(from).getPiece();

        Move move = new Move(from, to, piece, null, moveType);
        return chessGame.makeMove(move);
    }
    public Board getBoard() {
        return chessGame.getBoard();
    }
    public GameStatus getStatus() {
        return chessGame.getStatus();
    }
    public ChessGame getGame() {
        return chessGame;
    }
    private boolean isValidPosition(int row, int column) {
        return row >= 0 && row < 8 && column >= 0 && column < 8;
    }
}
