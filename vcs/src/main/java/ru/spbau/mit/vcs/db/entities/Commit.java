package ru.spbau.mit.vcs.db.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Date;

@DatabaseTable(tableName = "commit")
public class Commit {
    @DatabaseField(generatedId = true)
    public long id;

    @DatabaseField(canBeNull = false)
    public String message;

    @DatabaseField(canBeNull = false)
    public String author;

    @DatabaseField(canBeNull = false)
    public Date submitted;

    @DatabaseField(canBeNull = false, foreign = true)
    public Branch branch;

    public Commit() {}

    public Commit(String message, String author, Date submitted, Branch branch) {
        this.message = message;
        this.author = author;
        this.submitted = submitted;
        this.branch = branch;
    }
}
