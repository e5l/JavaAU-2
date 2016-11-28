package ru.spbau.mit.torrent.client.ui;

import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import ru.spbau.mit.torrent.client.exceptions.TorrentConfigException;
import ru.spbau.mit.torrent.protocol.ClientServer.SourcesRequest;
import sun.applet.Main;

import javax.naming.ConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class GUIClient extends Application {
    private static final String WINDOW_TITLE = "Torrent client";
    private static final String INFO_DIALOG_TITLE = "Client config";

    private static final String WINDOW_FXML = "window.fxml";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    private static final String SUBMIT_BUTTON_LABEL = "Start";
    private static final String SERVER_URL_LABEL = "Server url:";
    private static final String SERVER_URL_DEFAULT = "127.0.0.1";
    private static final String SEEDER_PORT_LABEL = "Seeder port:";
    private static final String SEEDER_PORT_DEFAULT = "8082";
    private static final String CLIENT_CONFIG_LABEL = "Config dir:";
    private static final String CLIENT_CONFIG_PROMPT = "user.dir if empty";

    @FXML
    private Button updateButton;

    @Override
    public void start(final Stage stage) {
        final URL window = getClass().getClassLoader().getResource(WINDOW_FXML);
        if (window == null) {
            System.out.println("Window config not found");
            return;
        }

        final FXMLLoader loader = new FXMLLoader(window);
        try {
            final BorderPane root = loader.load();
            final Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            stage.setTitle(WINDOW_TITLE);
            stage.setScene(scene);
            ClientConfig info = getInfo();

            final MainController controller = loader.getController();
            controller.init(info, stage);

            stage.setOnCloseRequest(event -> {
                controller.close();
            });
        } catch (IOException e) {
            System.out.printf("Couldn't init window: %s%n", e.getMessage());
        } catch (TorrentConfigException e) {
            System.out.printf("Failed to set client config: %s%n", e.getMessage());
        }

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static ClientConfig getInfo() throws TorrentConfigException {
        final Dialog<ClientConfig> infoDialog = new Dialog<>();
        infoDialog.setTitle(INFO_DIALOG_TITLE);

        final ButtonType submit = new ButtonType(SUBMIT_BUTTON_LABEL, ButtonBar.ButtonData.OK_DONE);
        infoDialog.getDialogPane().getButtonTypes().add(submit);

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        final TextField serverAddr = new TextField();
        final TextField port = new TextField();
        final TextField configPath = new TextField();
        serverAddr.setText(SERVER_URL_DEFAULT);
        port.setText(SEEDER_PORT_DEFAULT);
        configPath.setText("");
        configPath.setPromptText(CLIENT_CONFIG_PROMPT);

        grid.add(new Label(SERVER_URL_LABEL), 0, 0);
        grid.add(serverAddr, 1, 0);
        grid.add(new Label(SEEDER_PORT_LABEL), 0, 1);
        grid.add(port, 1, 1);
        grid.add(new Label(CLIENT_CONFIG_LABEL), 0, 2);
        grid.add(configPath, 1, 2);

        infoDialog.getDialogPane().setContent(grid);
        infoDialog.setResultConverter(btn -> new ClientConfig(serverAddr.getText(), port.getText(), configPath.getText()));

        final Optional<ClientConfig> result = infoDialog.showAndWait();
        if (!result.isPresent()) {
            throw new TorrentConfigException();
        }

        return result.get();
    }
}
