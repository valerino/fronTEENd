package com.valerino.fronteend;
/**
 * Created by valerino on 06/10/14.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static String APP_NAME = "fronTEENd";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
        Parent root = loader.load();
        final MainController mc = ((MainController)loader.getController());
        Scene s = new Scene(root);
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
        primaryStage.setTitle(APP_NAME);
        primaryStage.setMaximized(true);
        primaryStage.setScene(s);
        primaryStage.show();

        // init
        int res = mc.initController(primaryStage);
        if (res != 0) {
            Platform.exit();
        }
    }
}
