package ru.spbau.mit.vcs.exceptions;

public class FailedToGetCommitException extends Exception {
    public FailedToGetCommitException() {
        super("Failed to get commit");
    }
}
