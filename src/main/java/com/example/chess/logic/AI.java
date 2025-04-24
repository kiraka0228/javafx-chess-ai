package com.example.chess.logic;

import com.example.chess.model.Move;
import com.example.chess.model.Color;

import java.util.List;

public class AI {
  private static final int MAX_DEPTH = 2;

  public Move nextMove(Board board) {
    return minimax(board, MAX_DEPTH, Color.BLACK).move;
  }

  private Record minimax(Board b, int depth, Color player) {
    if (depth==0) return new Record(null, evaluate(b));
    List<Move> moves = b.generateLegalMoves(player);
    Record best = new Record(
      null,
      player==Color.BLACK ? Integer.MIN_VALUE : Integer.MAX_VALUE
    );
    for (Move m : moves) {
      Board copy = new Board(b);
      copy.applyMove(m);
      Record rec = minimax(copy, depth-1,
        player==Color.BLACK ? Color.WHITE : Color.BLACK
      );
      if (player==Color.BLACK && rec.score > best.score) {
        best = new Record(m, rec.score);
      } else if (player==Color.WHITE && rec.score < best.score) {
        best = new Record(m, rec.score);
      }
    }
    return best;
  }

  private int evaluate(Board b) {
    int score=0;
    for(int r=0;r<8;r++)for(int c=0;c<8;c++){
      var p = b.board[r][c];
      if(p!=null){
        int v = switch(p.getType()){
          case PAWN -> 1; case KNIGHT, BISHOP -> 3;
          case ROOK -> 5; case QUEEN -> 9; case KING -> 1000;
        };
        score += (p.getColor()==Color.BLACK) ? v : -v;
      }
    }
    return score;
  }

  private static class Record {
    final Move move; final int score;
    Record(Move m,int s){ move=m; score=s; }
  }
}
