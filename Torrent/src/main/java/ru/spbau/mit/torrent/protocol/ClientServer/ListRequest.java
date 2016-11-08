package ru.spbau.mit.torrent.protocol.ClientServer;

import ru.spbau.mit.torrent.protocol.ClientType;
import ru.spbau.mit.torrent.protocol.IMessage;

import java.io.DataOutputStream;
import java.io.IOException;

public class ListRequest implements IMessage {

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeByte(ClientType.LIST.toByte());
        output.flush();
    }
}
