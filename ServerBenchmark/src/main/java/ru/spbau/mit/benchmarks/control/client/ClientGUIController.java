package ru.spbau.mit.benchmarks.control.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.spbau.mit.benchmarks.generated.BenchmarkParamsOuterClass.BenchmarkParams;
import ru.spbau.mit.benchmarks.utils.Metrics;
import ru.spbau.mit.benchmarks.utils.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public final class ClientGUIController {
    public static final int PORT = 8081;
    Stage stage;

    @FXML
    protected TextField host;

    @FXML
    protected TextField arraySizeFrom;
    @FXML
    protected TextField arraySizeTo;
    @FXML
    protected TextField arraySizeStep;

    @FXML
    protected TextField clientsCountFrom;
    @FXML
    protected TextField clientsCountTo;
    @FXML
    protected TextField clientsCountStep;

    @FXML
    protected TextField queryDelayFrom;
    @FXML
    protected TextField queryDelayTo;
    @FXML
    protected TextField queryDelayStep;

    @FXML
    protected TextField requestsCountFrom;
    @FXML
    protected TextField requestsCountTo;
    @FXML
    protected TextField requestsCountStep;

    @FXML
    protected ToggleGroup group;

    @FXML
    protected Button startButton;

    public void init(Stage stage) {
        this.stage = stage;
    }

    @FXML
    protected void startClick(final ActionEvent actionEvent) {
        final Client client = new Client(host.getText(), PORT);
        final List<BenchmarkParams> params = readParams();
        final List<Pair<BenchmarkParams, Metrics>> results = new LinkedList<>();
        startButton.setDisable(true);
        new Thread(() -> {
            try {
                int ready = 0;
                for (final BenchmarkParams item : params) {
                    final Metrics metrics = client.measure(item);
                    results.add(new Pair<>(item, metrics));
                    ready++;

                    final String label = String.format("Ready: %d/%d", ready, params.size());
                    Platform.runLater(() -> startButton.setText(label));
                }
            } catch (IOException e) {
                System.out.printf("Failed to run benchmark: %s%n", e.getMessage());
            }

            Platform.runLater(() -> {
                startButton.setText("Start");
                saveToCsv(results);
                startButton.setDisable(false);
            });
        }).start();
    }

    private void saveToCsv(List<Pair<BenchmarkParams, Metrics>> results) {
        final FileChooser saveDialog = new FileChooser();
        final File file = saveDialog.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try (final FileWriter writer = new FileWriter(file)) {
            writer.write(String.format("%s, %s, %s, %s, %s, %s, %s, %s\n",
                    "server type",
                    "array size",
                    "clients count",
                    "message delay",
                    "requests count",
                    "average client work time",
                    "average request time",
                    "average sort time"));

            for (Pair<BenchmarkParams, Metrics> item : results) {
                writer.write(String.format("%s, %d, %d, %d, %d, %f, %f, %f\n",
                        item.first.getType().toString(),
                        item.first.getArraySize(),
                        item.first.getClientsCount(),
                        item.first.getMessageDelay(),
                        item.first.getRequestsCount(),
                        item.second.getAverageClientWorkTime(),
                        item.second.getAverageRequestTime(),
                        item.second.getAverageSortTime()));
            }
        } catch (IOException e) {
            System.out.printf("Failed to write to file: %s%n", e.getMessage());
        }
    }

    private List<BenchmarkParams> readParams() {
        final String type = group.getSelectedToggle().getUserData().toString();

        final int arraySizeStart = Integer.parseInt(arraySizeFrom.getText());
        final int arraySizeEnd = Integer.parseInt(arraySizeTo.getText());
        final int arraySizeDelta = Integer.parseInt(arraySizeStep.getText());

        final int messageDelayStart = Integer.parseInt(queryDelayFrom.getText());
        final int messageDelayEnd = Integer.parseInt(queryDelayTo.getText());
        final int messageDelayDelta = Integer.parseInt(queryDelayStep.getText());

        final int requestsCountStart = Integer.parseInt(requestsCountFrom.getText());
        final int requestsCountEnd = Integer.parseInt(requestsCountTo.getText());
        final int requestsCountDelta = Integer.parseInt(requestsCountStep.getText());

        final int clientsCountStart = Integer.parseInt(clientsCountFrom.getText());
        final int clientsCountEnd = Integer.parseInt(clientsCountTo.getText());
        final int clientsCountDelta = Integer.parseInt(clientsCountStep.getText());

        final List<BenchmarkParams> result = new LinkedList<>();

        for (int arraySize = arraySizeStart; arraySize <= arraySizeEnd; arraySize += arraySizeDelta) {
            for (int messageDelay = messageDelayStart; messageDelay <= messageDelayEnd; messageDelay += messageDelayDelta) {
                for (int requestsCount = requestsCountStart; requestsCount <= requestsCountEnd; requestsCount += requestsCountDelta) {
                    for (int clientsCount = clientsCountStart; clientsCount <= clientsCountEnd; clientsCount += clientsCountDelta) {
                        BenchmarkParams test = BenchmarkParams.newBuilder()
                                .setArraySize(arraySize)
                                .setMessageDelay(messageDelay)
                                .setClientsCount(clientsCount)
                                .setRequestsCount(requestsCount)
                                .setType(BenchmarkParams.ServerType.valueOf(type))
                                .build();

                        result.add(test);
                    }
                }
            }
        }

        return result;
    }
}
