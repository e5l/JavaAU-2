package ru.spbau.mit.vcs.db.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "file_entity")
public class FileEntity {
    @DatabaseField(generatedId = true)
    public long id;

    @DatabaseField(canBeNull = false)
    public String content;

    FileEntity() {
    }

    public FileEntity(String content) {
        this.content = content;
    }
}
