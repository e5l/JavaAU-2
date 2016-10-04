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

    @DatabaseField(foreign = true, canBeNull = false)
    public FileEntity entity;

    public File(Commit commit, String path, FileEntity entity) {
        this.commit = commit;
        this.path = path;
        this.entity = entity;
    }

    File() {}
}
