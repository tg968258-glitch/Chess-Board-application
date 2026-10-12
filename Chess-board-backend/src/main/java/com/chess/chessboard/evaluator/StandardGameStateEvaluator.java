package com.chess.chessboard.evaluator;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.GameStatus;
import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.model.Board;
import com.chess.chessboard.model.Move;
import com.chess.chessboard.model.Position;
import com.chess.chessboard.pieces.King;
import com.chess.chessboard.pieces.Pawn;
import com.chess.chessboard.pieces.Piece;
import com.chess.chessboard.validator.BasicMoveValidator;
import com.chess.chessboard.validator.MoveValidator;

public class StandardGameStateEvaluator implements GameStateEvaluator {

    private final MoveValidator validator = new BasicMoveValidator();

    @Override
    public GameStatus evaluate(Board board, Color color) {

        boolean check = isInCheck(board, color);
        boolean hasMoves = hasLegalMoves(board, color);

        if (check && !hasMoves) {
            return GameStatus.CHECKMATE;
        }

        if (!check && !hasMoves) {
            return GameStatus.STALEMATE;
        }

        if (check) {
            return GameStatus.CHECK;
        }

        return GameStatus.IN_PROGRESS;
    }

    public boolean isInCheck(Board board, Color color) {

        Position kingPosition = findKing(board, color);

        if (kingPosition == null) {
            return true;
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Position from = new Position(row, col);
                Piece piece = board.getSquare(from).getPiece();

                if (piece != null && piece.getColor() != color) {
                    if (canAttack(board, from, kingPosition)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private Position findKing(Board board, Color color) {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Position position = new Position(row, col);
                Piece piece = board.getSquare(position).getPiece();

                if (piece instanceof King && piece.getColor() == color) {
                    return position;
                }
            }
        }

        return null;
    }

    private boolean hasLegalMoves(Board board, Color color) {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Position from = new Position(row, col);
                Piece piece = board.getSquare(from).getPiece();

                if (piece == null || piece.getColor() != color) {
                    continue;
                }

                for (int r = 0; r < 8; r++) {
                    for (int c = 0; c < 8; c++) {

                        Position to = new Position(r, c);

                        MoveType type = board.isOccupied(to)
                                ? MoveType.CAPTURE : MoveType.NORMAL;

                        if (piece instanceof Pawn && (r == 0 || r == 7)) {
                            type = MoveType.PROMOTION;
                        }

                        Move move = new Move(from, to, piece, null, type);

                        if (!validator.validate(board, move)) {
                            continue;
                        }

                        if (isKingSafeAfterMove(board, move)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean isKingSafeAfterMove(Board board, Move move) {

        Position from = move.getFrom();
        Position to = move.getTo();

        Piece piece = board.getSquare(from).getPiece();

        if (piece == null) {
            return false;
        }

        Piece captured = board.getSquare(to).getPiece();

        Position enPassantPosition = null;
        Piece enPassantPawn = null;

        if (move.getMoveType() == MoveType.EN_PASSANT) {

            enPassantPosition = new Position(
                    from.getRow(), to.getColumn());

            enPassantPawn = board.getSquare(enPassantPosition).getPiece();

            board.getSquare(enPassantPosition).setPiece(null);
        }

        board.movePiece(from, to);

        boolean safe = !isInCheck(board, piece.getColor());

        board.movePiece(to, from);
        board.getSquare(to).setPiece(captured);

        if (enPassantPosition != null) {
            board.getSquare(enPassantPosition).setPiece(enPassantPawn);
        }

        return safe;
    }

    private boolean canAttack(Board board, Position from, Position to) {

        Piece piece = board.getSquare(from).getPiece();

        if (piece instanceof Pawn) {

            int direction = piece.getColor() == Color.WHITE ? 1 : -1;

            return to.getRow() - from.getRow() == direction
                    && Math.abs(to.getColumn() - from.getColumn()) == 1;
        }

        return piece.isValidMove(board, from, to);
    }
}