package ru.spbau.mit.torrent.server;

import ru.spbau.mit.torrent.protocol.ClientServer.*;
import ru.spbau.mit.torrent.protocol.ClientType;
import ru.spbau.mit.torrent.server.storage.SocketInfo;
import ru.spbau.mit.torrent.server.storage.Storage;
import ru.spbau.mit.utils.net.DataStreamHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerHandler extends DataStreamHandler {

    private final Storage storage;

    public ServerHandler(Socket connection, Storage storage) throws IOException {
        super(connection);
        this.storage = storage;
    }

    @Override
    protected void processCommand() throws IOException {
        final ClientType type = ClientType.fromByte(inputStream.readByte());
        switch (type) {
            case LIST:
                list();
                break;
            case UPLOAD:
                upload();
                break;
            case SOURCES:
                sources();
                break;
            case UPDATE:
                update();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void list() throws IOException {
        new ListResponse(storage.list()).write(outputStream);
    }

    private void upload() throws IOException {
        final UploadRequest request = UploadRequest.readFrom(inputStream);
        final int id = storage.upload(request.name, request.size);
        new UploadResponse(id).write(outputStream);
    }

    private void sources() throws IOException {
        final SourcesRequest request = SourcesRequest.readFrom(inputStream);
        final Set<SocketInfo> sources = storage.sources(request.id).stream().map(it -> it.socket).collect(Collectors.toSet());
        new SourcesResponse(sources).write(outputStream);
    }

    private void update() throws IOException {
        final UpdateRequest request = UpdateRequest.readFrom(inputStream);
        final boolean status = storage.update(socket.getInetAddress().getAddress(), request.port, request.files);
        new UpdateResponse(status).write(outputStream);
    }
}
