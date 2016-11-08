package ru.spbau.mit.torrent.client;

import ru.spbau.mit.torrent.client.exceptions.UpdateFailedException;
import ru.spbau.mit.torrent.client.storage.BlockFile;
import ru.spbau.mit.torrent.client.utils.OnDownload;
import ru.spbau.mit.torrent.protocol.ClientServer.*;
import ru.spbau.mit.torrent.server.storage.FileInfo;
import ru.spbau.mit.torrent.server.storage.SocketInfo;
import ru.spbau.mit.utils.net.DataStreamClient;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MINUTES;

/*
TODO: update request
 */

public class Client extends DataStreamClient {
    private static final String CLIENT_CONFIG_PATH = "client.config";
    private static final int UPDATE_TIMEOUT = 5;

    private final Timer timer = new Timer();
    private ConcurrentHashMap<Integer, BlockFile> files = new ConcurrentHashMap<>();
    private Map<Integer, FileInfo> catalog = new HashMap<>();

    private final Seeder seeder;
    private final Downloader downloader;

    public Client(int seederPort, String serverIp, int serverPort, OnDownload onDownload) throws IOException {
        super(new Socket(serverIp, serverPort));
        load();

        seeder = new Seeder(files, seederPort);
        downloader = new Downloader(file -> {
            if (file.getRemainingBlocksSize() > 0) {
                // TODO
            } else {
                onDownload.onDownload(file);
            }
        });

        addUnfinishedTasks();
        scheduleUpdate();
    }

    public synchronized Map<Integer, FileInfo> listFiles() throws IOException {
        new ListRequest().write(outputStream);
        final ListResponse response = ListResponse.readFrom(inputStream);
        catalog = response.response.stream().collect(Collectors.toMap(it -> it.id, it -> it));
        return catalog;
    }

    public synchronized void upload(String path) throws IOException, UpdateFailedException {
        final File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException(path);
        }

        new UploadRequest(file.getName(), file.length()).write(outputStream);
        final UploadResponse response = UploadResponse.readFrom(inputStream);
        files.put(response.id, new BlockFile(file, file.length(), response.id));
        update();
    }

    public void download(int id, String destination) throws IOException {
        if (!catalog.containsKey(id)) {
            throw new IndexOutOfBoundsException();
        }

        Set<SocketInfo> seedList = getSeedsList(id);
        BlockFile file = new BlockFile(new File(destination), catalog.get(id).size, id);
        files.put(id, file);

        downloader.addTask(file, seedList);
    }

    public void stop() {
        stopJobs();
    }

    @Override
    protected void stopJobs() {
        timer.cancel();
        downloader.stop();
        seeder.stop();
        super.stopJobs();

        save();
    }

    private synchronized void update() throws IOException, UpdateFailedException {
        final Enumeration<Integer> keys = files.keys();
        final Set<Integer> toSend = new HashSet<>();
        while (keys.hasMoreElements()) {
            toSend.add(keys.nextElement());
        }

        new UpdateRequest(seeder.getPort(), toSend).write(outputStream);
        final UpdateResponse response = UpdateResponse.readFrom(inputStream);
        if (!response.status) {
            throw new UpdateFailedException();
        }
    }

    private synchronized Set<SocketInfo> getSeedsList(int id) throws IOException {
        new SourcesRequest(id).write(outputStream);
        final SourcesResponse response = SourcesResponse.readFrom(inputStream);

        return response.sources;
    }

    private void load() throws IOException {
        final File file = new File(CLIENT_CONFIG_PATH);
        if (!file.exists() || file.isDirectory()) {
            return;
        }

        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file))) {
            files = (ConcurrentHashMap<Integer, BlockFile>) stream.readObject();
        } catch (ClassNotFoundException e) {
            // TODO
            System.out.println(e.getMessage());
        }
    }

    private void save() {
        final File file = new File(CLIENT_CONFIG_PATH);
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file))) {
            if (!file.exists()) {
                file.createNewFile();
            }
            stream.writeObject(files);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            // TODO
        }
    }

    private void scheduleUpdate() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    update();
                } catch (Exception e) {
                    System.out.printf("Failed to update: %s%n", e.getMessage());
                }
            }
        }, 0, MINUTES.toMillis(UPDATE_TIMEOUT));
    }

    private void addUnfinishedTasks() {
        files.entrySet().stream().filter(it -> it.getValue().getRemainingBlocksSize() > 0).forEach(it -> {
            try {
                Set<SocketInfo> seedsList = getSeedsList(it.getKey());
                downloader.addTask(it.getValue(), seedsList);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
