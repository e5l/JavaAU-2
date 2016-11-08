package ru.spbau.mit.torrent.protocol;

import java.io.DataOutputStream;
import java.io.IOException;

public interface IMessage {
    void write(DataOutputStream output) throws IOException;
}
