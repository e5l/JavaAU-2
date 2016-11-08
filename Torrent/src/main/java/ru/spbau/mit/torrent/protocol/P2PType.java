package ru.spbau.mit.torrent.protocol;

public enum P2PType {
    STAT,
    GET;

    public static P2PType fromByte(byte id) {
        switch (id) {
            case 1:
                return STAT;
            case 2:
                return GET;
            default:
                throw new IllegalArgumentException(String.format("Unknown command: %b", id));
        }
    }

    public byte toByte() {
        switch (this) {
            case STAT:
                return 1;
            case GET:
                return 2;
            default:
                throw new RuntimeException();
        }
    }
}
