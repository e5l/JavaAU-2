package ru.spbau.mit.vcs.db.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "branch")
public class Branch {
    @DatabaseField(id = true, canBeNull = false, unique = true)
    public String name;

    @DatabaseField(canBeNull = false, defaultValue = "true")
    public boolean closed = false;

    @DatabaseField(canBeNull = false, defaultValue = "false")
    public boolean active = false;

    public Branch() {}

    public Branch(String name, boolean closed) {
        this.name = name;
        this.closed = closed;
    }

    public boolean equals(Object other) {
        if (other == null || other.getClass() != getClass()) {
            return false;
        }

        return name.equals(((Branch) other).name);
    }


}
