package com.chess.chessboard.factory;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.PieceType;
import com.chess.chessboard.pieces.*;

public class PieceFactory {
    public static Piece createPiece(PieceType type, Color color) {

        return switch (type) {
            case KING -> new King(color);
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            case PAWN -> new Pawn(color);
        };
    }
}
