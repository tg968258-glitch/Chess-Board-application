package com.chess.chessboard.validator;

import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.model.Board;
import com.chess.chessboard.model.Move;
import com.chess.chessboard.model.Position;
import com.chess.chessboard.pieces.Piece;
import com.chess.chessboard.pieces.King;
import com.chess.chessboard.pieces.Pawn;
import com.chess.chessboard.rules.*;

import java.util.List;

public class BasicMoveValidator implements MoveValidator {

    private final List<GameRule> rules = List.of(
            new CastlingRule(),
            new EnPassantRule(),
            new PromotionRule()
    );

    @Override
    public boolean validate(Board board, Move move) {

        if (board == null || move == null
                || !isWithinBoard(move.getFrom())
                || !isWithinBoard(move.getTo())) {
            return false;
        }

        Position from = move.getFrom();
        Position to = move.getTo();

        Piece piece = board.getSquare(from).getPiece();
        Piece target = board.getSquare(to).getPiece();

        if (piece == null || move.getMoveType() == null
                || (target != null && target.getColor() == piece.getColor())) {
            return false;
        }

        if (from.getRow() == to.getRow()
                && from.getColumn() == to.getColumn()) {
            return false;
        }

        if (target instanceof King) {
            return false;
        }

        if (move.getMoveType() == MoveType.CASTLING
                || move.getMoveType() == MoveType.EN_PASSANT
                || move.getMoveType() == MoveType.PROMOTION) {

            for (GameRule rule : rules) {
                if (rule.isApplicable(board, move)) {
                    return rule.validate(board, move);
                }
            }
            return false;
        }

        if (move.getMoveType() == MoveType.NORMAL && target != null) {
            return false;
        }

        if (move.getMoveType() == MoveType.CAPTURE && target == null) {
            return false;
        }

        if (piece instanceof Pawn
                && (to.getRow() == 0 || to.getRow() == 7)) {
            return false;
        }

        return piece.isValidMove(board, from, to);
    }

    private boolean isWithinBoard(Position position) {
        return position != null
                && position.getRow() >= 0 && position.getRow() < 8
                && position.getColumn() >= 0 && position.getColumn() < 8;
    }
}