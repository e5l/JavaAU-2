package ru.spbau.mit.vcs.exceptions;

public class FailedGetBranchException extends Exception {
    public FailedGetBranchException() {
        super("Failed to get branch");
    }
}
