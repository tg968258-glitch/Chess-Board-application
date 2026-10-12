package com.chess.chessboard.pieces;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.model.Board;
import com.chess.chessboard.model.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public abstract class Piece {
    private Color color;

    public abstract boolean isValidMove(
            Board board, Position from, Position to);
    public List<Position> getAvailableMoves(Board board, Position from) {
        List<Position> availableMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position to = new Position(row, col);
                if (from.getRow() == row && from.getColumn() == col) {
                    continue;
                }
                if (board.isOccupied(to)
                        && board.getSquare(to).getPiece().getColor() == color) {
                    continue;
                }
                if (isValidMove(board, from, to)) {
                    availableMoves.add(to);
                }}}
        return availableMoves;
    }
    public String getType() {
        return getClass().getSimpleName().toUpperCase();
    }
}
