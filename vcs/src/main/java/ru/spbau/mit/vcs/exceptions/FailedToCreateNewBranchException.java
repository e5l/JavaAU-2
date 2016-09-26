package ru.spbau.mit.vcs.exceptions;

public class FailedToCreateNewBranchException extends Exception {
    public FailedToCreateNewBranchException() {
        super("Failed to create new branch");
    }
}
