package ru.spbau.mit.ftp.server.exception;

public class UnknownCommandException extends Exception {

    public UnknownCommandException(int id) {
        super(String.format("Unknown command: %d", id));
    }
}
