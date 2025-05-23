package com.example.chess.logic;

import com.example.chess.model.Move;
import com.example.chess.model.Piece;
import com.example.chess.model.PieceType;
import com.example.chess.model.Color;

import java.util.ArrayList;
import java.util.List;

public class Board {
    /** 8×8 board: row 0 = Black back rank, row 7 = White back rank */
    protected Piece[][] board = new Piece[8][8];

    /** Standard starting setup */
    public Board() {
        initialize();
    }

    /** Deep‐copy constructor */
    public Board(Board other) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = other.board[r][c];
                board[r][c] = (p == null) ? null : new Piece(p.getType(), p.getColor());
            }
        }
    }

    /** Place all pieces in their standard starting squares */
    private void initialize() {
        // Pawns
        for (int c = 0; c < 8; c++) {
            board[1][c] = new Piece(PieceType.PAWN, Color.BLACK);
            board[6][c] = new Piece(PieceType.PAWN, Color.WHITE);
        }
        // Rooks
        board[0][0] = new Piece(PieceType.ROOK, Color.BLACK);
        board[0][7] = new Piece(PieceType.ROOK, Color.BLACK);
        board[7][0] = new Piece(PieceType.ROOK, Color.WHITE);
        board[7][7] = new Piece(PieceType.ROOK, Color.WHITE);
        // Knights
        board[0][1] = new Piece(PieceType.KNIGHT, Color.BLACK);
        board[0][6] = new Piece(PieceType.KNIGHT, Color.BLACK);
        board[7][1] = new Piece(PieceType.KNIGHT, Color.WHITE);
        board[7][6] = new Piece(PieceType.KNIGHT, Color.WHITE);
        // Bishops
        board[0][2] = new Piece(PieceType.BISHOP, Color.BLACK);
        board[0][5] = new Piece(PieceType.BISHOP, Color.BLACK);
        board[7][2] = new Piece(PieceType.BISHOP, Color.WHITE);
        board[7][5] = new Piece(PieceType.BISHOP, Color.WHITE);
        // Queens
        board[0][3] = new Piece(PieceType.QUEEN, Color.BLACK);
        board[7][3] = new Piece(PieceType.QUEEN, Color.WHITE);
        // Kings
        board[0][4] = new Piece(PieceType.KING, Color.BLACK);
        board[7][4] = new Piece(PieceType.KING, Color.WHITE);
    }

    /** Returns the piece at (r,c), or null if empty */
    public Piece getPiece(int r, int c) {
        return board[r][c];
    }

    /** True if square is empty */
    public boolean isEmpty(int r, int c) {
        return board[r][c] == null;
    }

    /** Move a piece (no legality check here) */
    public void applyMove(Move m) {
        board[m.toRow][m.toCol] = board[m.fromRow][m.fromCol];
        board[m.fromRow][m.fromCol] = null;
    }

    /** True if m appears in the post‐check‐filter legal moves */
    public boolean isLegal(Move m) {
        Piece p = getPiece(m.fromRow, m.fromCol);
        if (p == null) return false;
        return generateLegalMoves(p.getColor()).stream()
            .anyMatch(x ->
                x.fromRow == m.fromRow &&
                x.fromCol == m.fromCol &&
                x.toRow   == m.toRow   &&
                x.toCol   == m.toCol
            );
    }

    /** True if that color’s king still exists on the board */
    public boolean hasKing(Color color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor() == color && p.getType() == PieceType.KING) {
                    return true;
                }
            }
        }
        return false;
    }

    /** True if the king of that color is under attack */
    public boolean isInCheck(Color color) {
        // find king location
        int kr = -1, kc = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor() == color && p.getType() == PieceType.KING) {
                    kr = r; kc = c;
                    break;
                }
            }
            if (kr != -1) break;
        }
        // generate opponent pseudo‐legal moves
        Color opp = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
        for (Move m : generatePseudoLegalMoves(opp)) {
            if (m.toRow == kr && m.toCol == kc) return true;
        }
        return false;
    }

    /**
     * Generate only those moves that do NOT leave your king in check.
     * I.e., filter out any pseudo‐legal move that places or leaves you in check.
     */
    public List<Move> generateLegalMoves(Color color) {
        List<Move> legal = new ArrayList<>();
        for (Move m : generatePseudoLegalMoves(color)) {
            Board copy = new Board(this);
            copy.applyMove(m);
            if (!copy.isInCheck(color)) {
                legal.add(m);
            }
        }
        return legal;
    }

    /**
     * Generate all moves ignoring check.
     * This is your “pseudo‐legal” move generator.
     */
    private List<Move> generatePseudoLegalMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor() == color) {
                    switch (p.getType()) {
                        case PAWN:
                            pawnMoves(moves, r, c, color);
                            break;
                        case KNIGHT:
                            knightMoves(moves, r, c, color);
                            break;
                        case BISHOP:
                            slidingMoves(moves, r, c, color,
                                new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
                            break;
                        case ROOK:
                            slidingMoves(moves, r, c, color,
                                new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
                            break;
                        case QUEEN:
                            slidingMoves(moves, r, c, color,
                                new int[][]{
                                    {1,0},{-1,0},{0,1},{0,-1},
                                    {1,1},{1,-1},{-1,1},{-1,-1}
                                });
                            break;
                        case KING:
                            kingMoves(moves, r, c, color);
                            break;
                    }
                }
            }
        }
        return moves;
    }

    /** Pawn moves: single, double from start, diagonal captures */
    private void pawnMoves(List<Move> moves, int r, int c, Color color) {
        int dir = (color == Color.WHITE) ? -1 : 1;
        int startRow = (color == Color.WHITE) ? 6 : 1;
        int nr = r + dir;
        // forward one
        if (inBounds(nr, c) && board[nr][c] == null) {
            moves.add(new Move(r, c, nr, c));
            // forward two
            int nr2 = nr + dir;
            if (r == startRow && inBounds(nr2, c) && board[nr2][c] == null) {
                moves.add(new Move(r, c, nr2, c));
            }
        }
        // diagonal captures
        for (int dc : new int[]{-1, 1}) {
            int nc = c + dc;
            if (inBounds(nr, nc) && board[nr][nc] != null &&
                board[nr][nc].getColor() != color) {
                moves.add(new Move(r, c, nr, nc));
            }
        }
    }

    /** Knight: eight L‐shape jumps */
    private void knightMoves(List<Move> moves, int r, int c, Color color) {
        int[][] deltas = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (var d : deltas) {
            int nr = r + d[0], nc = c + d[1];
            if (inBounds(nr, nc)) {
                Piece dest = board[nr][nc];
                if (dest == null || dest.getColor() != color) {
                    moves.add(new Move(r, c, nr, nc));
                }
            }
        }
    }

    /**
     * Bishop/Rook/Queen sliding moves along given directions.
     * @param dirs array of {dr,dc}
     */
    private void slidingMoves(List<Move> moves, int r, int c, Color color, int[][] dirs) {
        for (var d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            while (inBounds(nr, nc)) {
                if (board[nr][nc] == null) {
                    moves.add(new Move(r, c, nr, nc));
                } else {
                    if (board[nr][nc].getColor() != color) {
                        moves.add(new Move(r, c, nr, nc));
                    }
                    break;
                }
                nr += d[0];
                nc += d[1];
            }
        }
    }

    /** King: one square any direction (castling not yet implemented) */
    private void kingMoves(List<Move> moves, int r, int c, Color color) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (inBounds(nr, nc)) {
                    Piece dest = board[nr][nc];
                    if (dest == null || dest.getColor() != color) {
                        moves.add(new Move(r, c, nr, nc));
                    }
                }
            }
        }
    }

    /** True if (r,c) is on the 8×8 board */
    private boolean inBounds(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }
}
