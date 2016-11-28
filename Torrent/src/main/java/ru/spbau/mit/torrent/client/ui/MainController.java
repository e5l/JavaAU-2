package ru.spbau.mit.torrent.client.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.spbau.mit.torrent.client.Client;
import ru.spbau.mit.torrent.client.exceptions.UpdateFailedException;
import ru.spbau.mit.torrent.storage.BlockFile;
import ru.spbau.mit.torrent.storage.FileInfo;
import ru.spbau.mit.utils.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.StringJoiner;

public class MainController {
    private static final int SERVER_PORT = 8081;
    private static final int KB = 1024;

    @FXML
    private ListView filesList;
    private Client client;
    private Stage stage;

    public MainController() {
    }

    public void init(final ClientConfig info, final Stage stage) throws IOException {
        this.stage = stage;
        client = new Client(
                info.getClientPort(),
                info.getServerUrl(),
                SERVER_PORT,
                file -> Platform.runLater(this::updateListView),
                info.getConfigPath());

        updateListView();
    }

    @FXML
    protected void updateClick(final ActionEvent actionEvent) throws IOException {
        updateListView();
    }

    @FXML
    protected void uploadClick(final ActionEvent actionEvent) throws IOException, UpdateFailedException {
        final FileChooser chooser = new FileChooser();
        final File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        client.upload(file.getAbsolutePath());
        updateListView();
    }

    @FXML
    protected void downloadClick(ActionEvent actionEvent) {
        final DownloadItem selectedItem = (DownloadItem) filesList.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        final FileChooser chooser = new FileChooser();
        final File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            client.download(selectedItem.id, file.getAbsolutePath());
        } catch (IOException e) {
            System.out.printf("Failed to download file: %s%n", file.getAbsolutePath());
        }
    }

    public void close() {
        client.stop();
    }

    private void updateListView() {
        try {
            final Map<Integer, Pair<FileInfo, BlockFile>> files = client.listFiles();
            final ObservableList<DownloadItem> items = FXCollections.observableArrayList();

            files.forEach((id, file) -> {
                final String format = (file.second == null) ?
                        String.format("file id %d; %s; Size: %d KB", file.first.id, file.first.name, file.first.size / KB) :
                        String.format("file id %d; %s; Size: %d KB; Path: %s; Ready: %d / %d blocks",
                                file.first.id, file.first.name, file.first.size / KB, file.second.getPath(),
                                file.second.getBlocksCount() - file.second.getRemainingBlocksSize(), file.second.getBlocksCount());

                items.add(new DownloadItem(id, format));
            });

            filesList.setItems(items);
        } catch (IOException e) {
            System.out.printf("Failed to list client files: %s%n", e.getMessage());
        }


    }

    private class DownloadItem {
        private final int id;
        private final String format;

        public DownloadItem(int id, String format) {
            this.id = id;
            this.format = format;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return format;
        }
    }
}
