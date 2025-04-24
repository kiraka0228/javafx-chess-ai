package com.example.chess.model;

public class Move {
  public final int fromRow, fromCol, toRow, toCol;

  public Move(int fromRow, int fromCol, int toRow, int toCol) {
    this.fromRow = fromRow;
    this.fromCol = fromCol;
    this.toRow = toRow;
    this.toCol = toCol;
  }
}
