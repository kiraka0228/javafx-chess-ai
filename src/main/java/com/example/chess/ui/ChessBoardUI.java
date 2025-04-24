package com.example.chess.ui;

import com.example.chess.logic.Board;
import com.example.chess.logic.AI;
import com.example.chess.model.Move;
import com.example.chess.model.Color;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class ChessBoardUI {
  private final Stage stage;
  private final String playerName;
  private Board board;
  private AI ai;
  private final GridPane grid = new GridPane();
  private int selRow = -1, selCol = -1;
  private boolean gameOver = false;
  private final Map<String, Image> cache = new HashMap<>();

  public ChessBoardUI(Stage stage, String playerName) {
    this.stage = stage;
    this.playerName = playerName;
  }

  /** Initialize new game state and draw first board. */
  public Parent getRoot() {
    board    = new Board();
    ai       = new AI();
    selRow   = selCol = -1;
    gameOver = false;
    draw();
    return grid;
  }

  /** Draws the 8×8 board with pieces and highlights. */
  private void draw() {
    grid.getChildren().clear();
    for (int r = 0; r < 8; r++) {
      for (int c = 0; c < 8; c++) {
        StackPane cell = new StackPane();
        boolean light = (r + c) % 2 == 0;
        Rectangle bg = new Rectangle(60, 60,
            Paint.valueOf(light ? "#EEEED2" : "#769656"));

        // highlight selected square
        if (r == selRow && c == selCol) {
          bg.setStroke(Paint.valueOf("red"));
          bg.setStrokeWidth(3);
        }

        cell.getChildren().add(bg);

        var p = board.getPiece(r, c);
        if (p != null) {
          String key = (p.getColor() == Color.WHITE ? "white_" : "black_")
                       + p.getType().name().toLowerCase() + ".png";
          Image img = cache.computeIfAbsent(key,
              k -> new Image(getClass().getResourceAsStream("/images/" + k)));
          ImageView iv = new ImageView(img);
          iv.setFitWidth(50);
          iv.setFitHeight(50);
          cell.getChildren().add(iv);
        }

        final int rr = r, cc = c;
        cell.setOnMouseClicked(e -> handleClick(rr, cc));
        grid.add(cell, c, r);
      }
    }
    grid.setAlignment(Pos.CENTER);
  }

  /** Handle a click at (r,c): select/move or ignore if game over. */
  private void handleClick(int r, int c) {
    if (gameOver) return;

    // 1) First click: select piece
    if (selRow < 0) {
      if (board.getPiece(r, c) != null) {
        selRow = r;
        selCol = c;
        draw();
      }
      return;
    }

    // 2) Second click: attempt move
    Move humanMove = new Move(selRow, selCol, r, c);
    if (board.isLegal(humanMove)) {
      board.applyMove(humanMove);
      draw();

      // Check if opponent (Black) is checkmated
      if (isCheckmate(Color.BLACK)) {
        gameOver = true;
        showGameOver(playerName);
        return;
      }

      // AI's turn
      Move aiMove = ai.nextMove(board);
      board.applyMove(aiMove);
      draw();

      // Check if human (White) is checkmated
      if (isCheckmate(Color.WHITE)) {
        gameOver = true;
        showGameOver("Computer");
        return;
      }
    }

    // Clear selection on illegal or after attempt
    selRow = selCol = -1;
    draw();
  }

  /** True if 'color' is in check AND has no legal moves left. */
  private boolean isCheckmate(Color color) {
    return board.isInCheck(color) &&
           board.generateLegalMoves(color).isEmpty();
  }

  /**
   * Display end‐of‐game screen: Congratulations + Play Again + Exit.
   */
  private void showGameOver(String winner) {
    Label msg = new Label("Congratulations, " + winner + "!");
    msg.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

    Button playAgain = new Button("Play Again");
    playAgain.setOnAction(e -> {
      ChessBoardUI fresh = new ChessBoardUI(stage, playerName);
      Scene scene = new Scene(fresh.getRoot());
      stage.setScene(scene);
      stage.setTitle("Chess AI Platform");
    });

    Button exit = new Button("Exit");
    exit.setOnAction(e -> Platform.exit());

    VBox root = new VBox(20, msg, playAgain, exit);
    root.setAlignment(Pos.CENTER);
    root.setStyle("-fx-padding: 20;");

    Scene scene = new Scene(root, 400, 200);
    stage.setScene(scene);
    stage.setTitle("Game Over");
  }
}
