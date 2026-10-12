package com.chess.chessboard.model;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.MoveType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Player {
    private String name;
    private Color color;
    public Move makeMove(Position from, Position to) {
        return new Move(from, to, null, null, MoveType.NORMAL);
    }
}
