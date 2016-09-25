package ru.spbau.mit.vcs.utils;

import java.util.Set;

public class VcsStatus {
    private final Set<String> added;
    private final Set<String> removed;
    private final Set<String> modified;

    public VcsStatus(Set<String> added, Set<String> removed, Set<String> modified) {
        this.added = added;
        this.removed = removed;
        this.modified = modified;
    }

    public Set<String> getAdded() {
        return added;
    }

    public Set<String> getRemoved() {
        return removed;
    }

    public Set<String> getModified() {
        return modified;
    }
}
