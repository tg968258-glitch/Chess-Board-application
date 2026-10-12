package com.chess.chessboard.pieces;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.model.Board;
import com.chess.chessboard.model.Move;
import com.chess.chessboard.model.Position;

public class Bishop  extends Piece{
    public Bishop(Color color) {
        super(color);
    }
    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getColumn() - from.getColumn());
        if (rowDiff == 0 || rowDiff != colDiff) {
            return false;
        }
        return board.isPathClear(new Move(from, to, this, null, MoveType.NORMAL));
    }}
