package com.valerino.fronteend;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * simple progress dialog
 * Created by valerino on 15/10/14.
 */
public class ProgressDialog {
    Label _label = null;
    Stage _stage = null;
    Stage _parent = null;

    ProgressDialog(Stage parent) {
        _parent = parent;
        _label = new Label();
        _stage = new Stage(StageStyle.UNIFIED);
        VBox vb = new VBox();
        vb.getChildren().add(_label);
        _stage.setWidth(320);

        /*
        // disable close button
        _stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();
            }
        });
        */

        _stage.setScene(new Scene(vb));
        _stage.setResizable(false);
        _stage.setTitle("Adding romset");
        _stage.initModality(Modality.WINDOW_MODAL);
        _stage.initOwner(_parent.getScene().getWindow());
    }

    /**
     * set dialog text
     * @param s the text
     */
    void setText(final String s) {
        _label.setText(s);
    }

    /**
     * show the dialog
     */
    void show() {

    }

    /**
     * close the dialog
     */
    void close() {
        _stage.close();
    }
}
