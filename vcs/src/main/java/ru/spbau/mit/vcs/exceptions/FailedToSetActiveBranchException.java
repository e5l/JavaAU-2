package ru.spbau.mit.vcs.exceptions;

public class FailedToSetActiveBranchException extends Exception {
    public FailedToSetActiveBranchException() {
        super("Failed to set active branch");
    }
}
