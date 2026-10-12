
package com.chess.chessboard.model;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.GameStatus;
import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.evaluator.StandardGameStateEvaluator;
import com.chess.chessboard.pieces.Piece;
import com.chess.chessboard.rules.CastlingRule;
import com.chess.chessboard.rules.EnPassantRule;
import com.chess.chessboard.validator.BasicMoveValidator;
import com.chess.chessboard.validator.MoveValidator;
import lombok.Getter;

@Getter
public class ChessGame {

    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private MoveHistory moveHistory;
    private Color currentTurn;
    private GameStatus status;

    private final MoveValidator moveValidator = new BasicMoveValidator();
    private final StandardGameStateEvaluator evaluator =
            new StandardGameStateEvaluator();

    private final CastlingRule castlingRule = new CastlingRule();
    private final EnPassantRule enPassantRule = new EnPassantRule();

    public ChessGame(Player whitePlayer, Player blackPlayer) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.status = GameStatus.NOT_STARTED;
    }

    public void start() {
        board = new Board();
        moveHistory = new MoveHistory();
        currentTurn = Color.WHITE;
        status = GameStatus.IN_PROGRESS;
    }

    public void switchTurn() {
        currentTurn = currentTurn == Color.WHITE
                ? Color.BLACK : Color.WHITE;
    }

    public boolean isGameOver() {
        return status == GameStatus.CHECKMATE
                || status == GameStatus.STALEMATE
                || status == GameStatus.DRAW;
    }

    public boolean makeMove(Move move) {

        if (board == null || move == null || isGameOver()
                || status == GameStatus.NOT_STARTED) {
            return false;
        }

        if (!moveValidator.validate(board, move)) {
            return false;
        }

        Piece piece = board.getSquare(move.getFrom()).getPiece();

        if (piece.getColor() != currentTurn) {
            return false;
        }

        if (move.getMoveType() == MoveType.CASTLING) {

            if (!castlingRule.isKingPathSafe(board, move, evaluator)) {
                return false;
            }

            if (castlingRule.hasKingOrRookMoved(move, moveHistory)) {
                return false;
            }

        } else {

            if (move.getMoveType() == MoveType.EN_PASSANT) {
                if (!enPassantRule.isPreviousMoveValid(
                        board, move, moveHistory)) {
                    return false;
                }
            }

            if (!evaluator.isKingSafeAfterMove(board, move)) {
                return false;
            }
        }

        move.setPiece(piece);

        if (move.getMoveType() == MoveType.EN_PASSANT) {
            Position capturedPosition = new Position(
                    move.getFrom().getRow(),
                    move.getTo().getColumn()
            );

            move.setCapturedPiece(
                    board.getSquare(capturedPosition).getPiece()
            );
        } else {
            move.setCapturedPiece(
                    board.getSquare(move.getTo()).getPiece()
            );
        }

        board.movePiece(move);

        moveHistory.add(move);

        switchTurn();

        status = evaluator.evaluate(board, currentTurn);

        return true;
    }
}
