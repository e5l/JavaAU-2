package ru.spbau.mit.benchmarks.control.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.spbau.mit.benchmarks.generated.BenchmarkParamsOuterClass;
import ru.spbau.mit.benchmarks.generated.MetricsResponseOuterClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class ClientGUIController {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 8081;

    final Client client = new Client(HOST, PORT);
    Stage stage;

    @FXML
    TextField arraySize;

    @FXML
    TextField clientsCount;

    @FXML
    TextField queryDelay;

    @FXML
    TextField requestsCount;

    @FXML
    ToggleGroup group;

    public void init(Stage stage) {
        this.stage = stage;
    }

    @FXML
    protected void startClick(final ActionEvent actionEvent) {
        final BenchmarkParamsOuterClass.BenchmarkParams params = readParams();

        try {
            final MetricsResponseOuterClass.MetricsResponse measure = client.measure(params);
            final FileChooser saveDialog = new FileChooser();
            final File file = saveDialog.showSaveDialog(stage);
            try (final FileWriter writer = new FileWriter(file)) {
                writer.write(String.format("%d %d", measure.getTotalRequestTime(), measure.getTotalSortTime()));
            }

        } catch (IOException e) {
            System.out.printf("Failed to run benchmark: %s%n", e.getMessage());
        }
    }

    private BenchmarkParamsOuterClass.BenchmarkParams readParams() {
        final int type = Integer.parseInt(group.getSelectedToggle().getUserData().toString());

        return BenchmarkParamsOuterClass.BenchmarkParams.newBuilder()
                .setArraySize(Integer.parseInt(arraySize.getText()))
                .setMessageDelay(Integer.parseInt(queryDelay.getText()))
                .setClientsCount(Integer.parseInt(clientsCount.getText()))
                .setRequestsCount(Integer.parseInt(requestsCount.getText()))
                .setType(BenchmarkParamsOuterClass.BenchmarkParams.ServerType.values()[type])
                .build();
    }

}
