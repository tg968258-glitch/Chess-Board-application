package com.chess.chessboard.rules;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.model.*;
import com.chess.chessboard.pieces.*;

public class EnPassantRule implements GameRule {

    @Override
    public boolean isApplicable(Board board, Move move) {

        Piece piece = board.getSquare(move.getFrom()).getPiece();

        return move.getMoveType() == MoveType.EN_PASSANT
                && piece instanceof Pawn
                && Math.abs(move.getTo().getColumn()
                - move.getFrom().getColumn()) == 1
                && !board.isOccupied(move.getTo());
    }

    @Override
    public boolean validate(Board board, Move move) {

        if (!isApplicable(board, move)) {
            return false;
        }

        Position from = move.getFrom();
        Position to = move.getTo();

        Piece pawn = board.getSquare(from).getPiece();

        int direction = pawn.getColor() == Color.WHITE ? 1 : -1;

        Position capturedPosition = new Position(
                from.getRow(), to.getColumn());

        Piece captured = board.getSquare(capturedPosition).getPiece();

        return to.getRow() - from.getRow() == direction
                && from.getRow() == (pawn.getColor() == Color.WHITE ? 4 : 3)
                && captured instanceof Pawn
                && captured.getColor() != pawn.getColor();
    }

    public boolean isPreviousMoveValid(
            Board board,
            Move move,
            MoveHistory moveHistory) {

        if (!validate(board, move)) {
            return false;
        }

        Move lastMove = moveHistory.getLastMove();

        if (lastMove == null || !(lastMove.getPiece() instanceof Pawn)) {
            return false;
        }

        Position lastFrom = lastMove.getFrom();
        Position lastTo = lastMove.getTo();

        Position from = move.getFrom();
        Position to = move.getTo();

        Piece pawn = board.getSquare(from).getPiece();

        return lastMove.getPiece().getColor() != pawn.getColor()
                && Math.abs(lastTo.getRow() - lastFrom.getRow()) == 2
                && lastFrom.getColumn() == lastTo.getColumn()
                && lastTo.getRow() == from.getRow()
                && lastTo.getColumn() == to.getColumn()
                && board.getSquare(lastTo).getPiece() == lastMove.getPiece();
    }
}