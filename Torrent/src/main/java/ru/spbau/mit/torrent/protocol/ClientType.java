package ru.spbau.mit.torrent.protocol;

import java.io.IOException;

public enum ClientType {
    LIST,
    UPLOAD,
    SOURCES,
    UPDATE;

    public static ClientType fromByte(byte id) throws IOException {
        switch (id) {
            case 1:
                return LIST;
            case 2:
                return UPLOAD;
            case 3:
                return SOURCES;
            case 4:
                return UPDATE;
            default:
                throw new IllegalArgumentException(String.format("Unknown command: %b", id));
        }
    }

    public byte toByte() {
        switch (this) {
            case LIST:
                return 1;
            case UPLOAD:
                return 2;
            case SOURCES:
                return 3;
            case UPDATE:
                return 4;
            default:
                return -1;
        }
    }
}
