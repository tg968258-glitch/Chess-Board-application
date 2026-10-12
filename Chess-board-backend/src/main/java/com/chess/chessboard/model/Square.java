package com.chess.chessboard.model;

import com.chess.chessboard.pieces.Piece;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Square {
    private Position position;
    private Piece piece;

    public boolean isEmpty() {
        return piece == null;
    }

}
