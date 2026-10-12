package com.chess.chessboard.pieces;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.model.Board;
import com.chess.chessboard.model.Position;

public class King extends Piece{
    public King(Color color) {
        super(color);
    }
    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getColumn() - from.getColumn());

        return rowDiff <= 1 && colDiff <= 1
                && (rowDiff != 0 || colDiff != 0);
    }}
