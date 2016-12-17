package ru.spbau.mit.benchmarks.control.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;

public final class ClientGUI extends Application {
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 300;

    private static final String MAIN_WINDOW_FXML = "main_window.fxml";
    private static final String WINDOW_TITLE = "Server benchmarks";

    @Override
    public void start(Stage stage) throws Exception {
        final URL window = getClass().getClassLoader().getResource(MAIN_WINDOW_FXML);
        if (window == null) {
            System.out.println("Window class not found ");
            return;
        }

        final FXMLLoader loader = new FXMLLoader(window);

        final HBox main = loader.load();
        final Scene scene = new Scene(main, WINDOW_WIDTH, WINDOW_HEIGHT);

        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);

        final ClientGUIController controller = loader.getController();
        controller.init(stage);
        stage.show();
    }
}

