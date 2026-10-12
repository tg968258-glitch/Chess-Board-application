package com.chess.chessboard.model;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.enums.PieceType;
import com.chess.chessboard.factory.PieceFactory;
import com.chess.chessboard.pieces.Piece;
import com.chess.chessboard.pieces.Queen;
import lombok.Getter;

@Getter
public class Board {

    private Square[][] squares = new Square[8][8];

    public Board() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                squares[i][j] = new Square(new Position(i, j), null);
            }
        }
        initializeBoard();
    }

    public void initializeBoard() {

        PieceType[] pieces = {
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK
        };

        for (int i = 0; i < 8; i++) {
            squares[0][i].setPiece(
                    PieceFactory.createPiece(pieces[i], Color.WHITE));

            squares[1][i].setPiece(
                    PieceFactory.createPiece(PieceType.PAWN, Color.WHITE));

            squares[6][i].setPiece(
                    PieceFactory.createPiece(PieceType.PAWN, Color.BLACK));

            squares[7][i].setPiece(
                    PieceFactory.createPiece(pieces[i], Color.BLACK));
        }
    }

    public Square getSquare(Position position) {
        return squares[position.getRow()][position.getColumn()];
    }

    public boolean isOccupied(Position position) {
        return getSquare(position).getPiece() != null;
    }

    public void movePiece(Position from, Position to) {

        Piece piece = getSquare(from).getPiece();

        getSquare(to).setPiece(piece);
        getSquare(from).setPiece(null);
    }

    public void movePiece(Move move) {

        Position from = move.getFrom();
        Position to = move.getTo();

        Piece piece = getSquare(from).getPiece();

        if (move.getMoveType() == MoveType.EN_PASSANT) {
            getSquare(new Position(from.getRow(), to.getColumn()))
                    .setPiece(null);
        }

        if (move.getMoveType() == MoveType.CASTLING) {

            int rookFrom = to.getColumn() == 6 ? 7 : 0;
            int rookTo = to.getColumn() == 6 ? 5 : 3;

            movePiece(
                    new Position(from.getRow(), rookFrom),
                    new Position(from.getRow(), rookTo)
            );
        }

        movePiece(from, to);

        if (move.getMoveType() == MoveType.PROMOTION) {
            getSquare(to).setPiece(new Queen(piece.getColor()));
        }
    }

    public boolean isPathClear(Move move) {

        Position from = move.getFrom();
        Position to = move.getTo();

        int rowStep = Integer.compare(to.getRow(), from.getRow());
        int colStep = Integer.compare(to.getColumn(), from.getColumn());

        int row = from.getRow() + rowStep;
        int col = from.getColumn() + colStep;

        while (row != to.getRow() || col != to.getColumn()) {

            if (squares[row][col].getPiece() != null) {
                return false;
            }

            row += rowStep;
            col += colStep;
        }

        return true;
    }
}