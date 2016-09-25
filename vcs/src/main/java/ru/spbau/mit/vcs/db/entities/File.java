package ru.spbau.mit.vcs.db.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "file")
public class File {
    @DatabaseField(generatedId = true)
    public long id;

    @DatabaseField(foreign = true, canBeNull = false)
    public Commit commit;

    @DatabaseField(canBeNull = false)
    public String path;

    @DatabaseField(canBeNull = false)
    public String content;

    public File(Commit commit, String path, String content) {
        this.commit = commit;
        this.path = path;
        this.content = content;
    }

    File() {}
}
