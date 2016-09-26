package ru.spbau.mit.vcs.exceptions;

public class NoActiveBranchFoundException extends Exception {
    public NoActiveBranchFoundException() {
        super("No active branch found");
    }
}
