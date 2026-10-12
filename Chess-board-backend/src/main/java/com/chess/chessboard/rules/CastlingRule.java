
package com.chess.chessboard.rules;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.model.*;
import com.chess.chessboard.pieces.*;
import com.chess.chessboard.evaluator.StandardGameStateEvaluator;

public class CastlingRule implements GameRule {

    @Override
    public boolean isApplicable(Board board, Move move) {

        Piece piece = board.getSquare(move.getFrom()).getPiece();

        return move.getMoveType() == MoveType.CASTLING
                && piece instanceof King
                && move.getFrom().getRow() == move.getTo().getRow()
                && Math.abs(move.getTo().getColumn()
                - move.getFrom().getColumn()) == 2;
    }

    @Override
    public boolean validate(Board board, Move move) {

        if (!isApplicable(board, move)) {
            return false;
        }

        Position from = move.getFrom();
        Position to = move.getTo();

        Piece king = board.getSquare(from).getPiece();

        int row = king.getColor() == Color.WHITE ? 0 : 7;

        if (from.getRow() != row || from.getColumn() != 4) {
            return false;
        }

        int rookCol = to.getColumn() == 6 ? 7 : 0;

        Piece rook = board.getSquare(
                new Position(row, rookCol)).getPiece();

        if (!(rook instanceof Rook)
                || rook.getColor() != king.getColor()) {
            return false;
        }

        int step = rookCol == 7 ? 1 : -1;

        for (int col = 4 + step; col != rookCol; col += step) {

            if (board.isOccupied(new Position(row, col))) {
                return false;
            }
        }

        return true;
    }

    public boolean isKingPathSafe(
            Board board,
            Move move,
            StandardGameStateEvaluator evaluator) {

        if (!validate(board, move)) {
            return false;
        }

        Position from = move.getFrom();
        Position to = move.getTo();

        Piece king = board.getSquare(from).getPiece();

        if (evaluator.isInCheck(board, king.getColor())) {
            return false;
        }

        int step = to.getColumn() > from.getColumn() ? 1 : -1;

        Position middle = new Position(
                from.getRow(),
                from.getColumn() + step
        );

        Move middleMove = new Move(
                from, middle, king, null, MoveType.NORMAL
        );

        return evaluator.isKingSafeAfterMove(board, middleMove)
                && evaluator.isKingSafeAfterMove(board, move);
    }

    public boolean hasKingOrRookMoved(
            Move move,
            MoveHistory moveHistory) {

        Position from = move.getFrom();
        Position to = move.getTo();

        int row = from.getRow();
        int rookCol = to.getColumn() == 6 ? 7 : 0;

        for (Move previousMove : moveHistory.getMoves()) {

            Position previousFrom = previousMove.getFrom();

            if (previousFrom.getRow() == row
                    && previousFrom.getColumn() == 4
                    && previousMove.getPiece() instanceof King) {
                return true;
            }

            if (previousFrom.getRow() == row
                    && previousFrom.getColumn() == rookCol
                    && previousMove.getPiece() instanceof Rook) {
                return true;
            }
        }

        return false;
    }
}
