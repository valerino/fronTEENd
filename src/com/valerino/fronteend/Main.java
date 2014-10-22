package com.valerino.fronteend;
/**
 * Created by valerino on 06/10/14.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Optional;

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

        primaryStage.setTitle(APP_NAME);
        primaryStage.setMaximized(true);
        primaryStage.setScene(s);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to quit ?");
                Optional<ButtonType> res = alert.showAndWait();
                if (res.get() != ButtonType.OK) {
                    // avoid closing
                    event.consume();
                } else {
                    // cleanup
                    mc.cleanupController();
                }
            }
        });
        primaryStage.show();

        // init
        int res = mc.initController(primaryStage);
        if (res != 0) {
            Platform.exit();
        }
    }
}
