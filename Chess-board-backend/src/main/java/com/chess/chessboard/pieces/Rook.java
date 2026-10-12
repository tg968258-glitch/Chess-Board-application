package com.chess.chessboard.pieces;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.model.Board;
import com.chess.chessboard.model.Move;
import com.chess.chessboard.model.Position;

public class Rook extends Piece {
    public Rook(Color color) {
        super(color);
    }

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        boolean validMove = from.getRow() == to.getRow()
                || from.getColumn() == to.getColumn();
        if (!validMove || from.equals(to)) {
            return false;
        }
        return board.isPathClear(new Move(from, to, this, null, MoveType.NORMAL));}
}
