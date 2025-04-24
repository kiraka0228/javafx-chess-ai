package com.example.chess;

import javafx.application.Application;
import javafx.stage.Stage;
import com.example.chess.ui.ChessUI;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) {
    new ChessUI(primaryStage).show();
  }
  public static void main(String[] args) {
    launch(args);
  }
}
