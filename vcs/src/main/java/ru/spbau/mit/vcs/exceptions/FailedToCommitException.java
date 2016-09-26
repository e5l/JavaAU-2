package ru.spbau.mit.vcs.exceptions;

public class FailedToCommitException extends Exception {
    public FailedToCommitException() {
        super("Failed to commit");
    }
}
