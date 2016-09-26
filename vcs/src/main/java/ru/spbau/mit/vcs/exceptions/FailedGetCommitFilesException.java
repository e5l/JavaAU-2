package ru.spbau.mit.vcs.exceptions;

public class FailedGetCommitFilesException extends Exception {
    public FailedGetCommitFilesException() {
        super("Failed to get commit files");
    }
}
