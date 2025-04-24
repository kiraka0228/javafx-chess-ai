package com.example.chess.ui;

import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

public class ChessUI {
  private final Stage stage;

  public ChessUI(Stage stage) {
    this.stage = stage;
  }

  public void show() {
    // 1) Ask for player name
    TextInputDialog dialog = new TextInputDialog("Player");
    dialog.setTitle("Player Name");
    dialog.setHeaderText("Enter your name:");
    dialog.setContentText("Name:");
    Optional<String> result = dialog.showAndWait();
    String playerName = result.orElse("Player");

    // 2) Create the board UI with the player name
    ChessBoardUI boardUI = new ChessBoardUI(stage, playerName);
    Scene scene = new Scene(boardUI.getRoot());
    stage.setTitle("Chess AI Platform");
    stage.setScene(scene);
    stage.show();
  }
}
