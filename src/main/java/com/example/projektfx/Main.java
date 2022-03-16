package com.example.projektfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.EventObject;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main extends Application {

    static AnchorPane root;
    private final static int X = 1800;
    private final static int Y = 800;
    private Controller controller;
    static Scene scene;

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view.fxml"));
        root = fxmlLoader.load();

        scene = new Scene(root, X, Y);
        controller = fxmlLoader.getController();
        stage.setTitle("Projekt sklep");
        stage.setResizable(false);
        stage.setScene(scene);

        Stage finalStage = stage;
        stage.setOnCloseRequest(event -> {
            try {
                logout(finalStage);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        stage.show();
    }

    public void logout(Stage stage) throws InterruptedException {
        if(controller.sklep != null)
            controller.sklep.zamknijSklep(); //Koncz dzialanie
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch();
    }
}