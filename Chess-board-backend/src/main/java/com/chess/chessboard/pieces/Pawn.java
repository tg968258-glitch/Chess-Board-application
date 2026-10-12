package com.chess.chessboard.pieces;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.model.Board;
import com.chess.chessboard.model.Position;

public class Pawn extends Piece{
    public Pawn(Color color) {
        super(color);
    }
    @Override
        public boolean isValidMove(Board board, Position from, Position to) {

            int direction = getColor() == Color.WHITE ? 1 : -1;
            int startRow = getColor() == Color.WHITE ? 1 : 6;

            int rowDiff = to.getRow() - from.getRow();
            int colDiff = Math.abs(to.getColumn() - from.getColumn());

            if (colDiff == 0) {
                if (rowDiff == direction) {
                    return !board.isOccupied(to);
                }
                if (from.getRow() == startRow && rowDiff == 2 * direction) {
                    Position middle = new Position(
                            from.getRow() + direction,
                            from.getColumn());
                    return !board.isOccupied(middle) && !board.isOccupied(to);
                }
            }
            if (rowDiff == direction && colDiff == 1) {
                return board.isOccupied(to);
            }
            return false;
        }}
