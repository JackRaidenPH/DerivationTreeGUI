package dev.jackraidenph.dertreegui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jpl7.Query;

public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() {
        new Query("halt.").hasSolution();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class
                .getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Derivation Tree Visualizer");
        stage.setScene(scene);
        stage.show();
    }
}