package ru.spbau.mit.vcs.exceptions;

public class FailedToCreateFileException extends RuntimeException {
    public FailedToCreateFileException(String path) {
        super(String.format("failed to created file: %s", path));
    }
}
