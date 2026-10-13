package com.chess.chessboard;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.enums.PieceType;
import com.chess.chessboard.model.Board;
import com.chess.chessboard.model.Move;
import com.chess.chessboard.model.Position;
import com.chess.chessboard.pieces.Knight;
import com.chess.chessboard.pieces.Pawn;
import com.chess.chessboard.rules.PromotionRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromotionRuleTests {

    @Test
    void promotesPawnToTheSelectedPiece() {
        Board board = promotionBoard();
        Move move = promotionMove(PieceType.KNIGHT);

        assertTrue(new PromotionRule().validate(board, move));
        board.movePiece(move);

        assertNull(board.getSquare(new Position(6, 0)).getPiece());
        assertInstanceOf(Knight.class, board.getSquare(new Position(7, 0)).getPiece());
    }

    @Test
    void rejectsKingAndPawnAsPromotionChoices() {
        Board board = promotionBoard();
        PromotionRule rule = new PromotionRule();

        assertFalse(rule.validate(board, promotionMove(PieceType.KING)));
        assertFalse(rule.validate(board, promotionMove(PieceType.PAWN)));
        assertFalse(rule.validate(board, promotionMove(null)));
    }

    private Board promotionBoard() {
        Board board = new Board();
        board.getSquare(new Position(6, 0)).setPiece(new Pawn(Color.WHITE));
        board.getSquare(new Position(7, 0)).setPiece(null);
        return board;
    }

    private Move promotionMove(PieceType pieceType) {
        return new Move(new Position(6, 0), new Position(7, 0),
                null, null, MoveType.PROMOTION, pieceType);
    }
}
