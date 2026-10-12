package com.chess.chessboard.rules;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.model.*;
import com.chess.chessboard.pieces.*;

public class PromotionRule implements GameRule {

    @Override
    public boolean isApplicable(Board board, Move move) {

        Piece piece = board.getSquare(move.getFrom()).getPiece();

        return move.getMoveType() == MoveType.PROMOTION
                && piece instanceof Pawn;
    }

    @Override
    public boolean validate(Board board, Move move) {

        if (!isApplicable(board, move)) return false;

        Piece pawn = board.getSquare(move.getFrom()).getPiece();

        int lastRow = pawn.getColor() == Color.WHITE ? 7 : 0;

        return move.getTo().getRow() == lastRow
                && pawn.isValidMove(board, move.getFrom(), move.getTo());
    }
}