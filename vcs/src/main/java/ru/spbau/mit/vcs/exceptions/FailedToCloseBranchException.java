package ru.spbau.mit.vcs.exceptions;

public class FailedToCloseBranchException extends Exception {
    public FailedToCloseBranchException() {
        super("Failed to close branch");
    }
}
