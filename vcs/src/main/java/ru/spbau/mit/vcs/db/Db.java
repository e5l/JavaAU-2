package ru.spbau.mit.vcs.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.vcs.db.entities.Branch;
import ru.spbau.mit.vcs.db.entities.Commit;
import ru.spbau.mit.vcs.db.entities.File;
import ru.spbau.mit.vcs.db.entities.FileEntity;
import ru.spbau.mit.vcs.exceptions.*;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Db {
    private JdbcConnectionSource connectionSource;

    private Dao<Branch, String> branches;
    private Dao<Commit, Long> commits;
    private Dao<File, Long> files;
    private Dao<FileEntity, Long> fileEntities;

    final private String dbPath = "jdbc:sqlite:";

    public Db(String name) {
        try {
            Class.forName("org.sqlite.JDBC");
            initDb(name);
        } catch (SQLException e) {
            System.out.println("Failed to init databse");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC library not found");
        }
    }

    private void initDb(String name) throws SQLException {
        connectionSource = new JdbcConnectionSource(String.format("%s%s", dbPath, name));

        branches = DaoManager.createDao(connectionSource, Branch.class);
        commits = DaoManager.createDao(connectionSource, Commit.class);
        files = DaoManager.createDao(connectionSource, File.class);
        fileEntities = DaoManager.createDao(connectionSource, FileEntity.class);

        TableUtils.createTableIfNotExists(connectionSource, Branch.class);
        TableUtils.createTableIfNotExists(connectionSource, Commit.class);
        TableUtils.createTableIfNotExists(connectionSource, File.class);
        TableUtils.createTableIfNotExists(connectionSource, FileEntity.class);
    }

    public
    @NotNull
    Branch getActiveBranch() throws NoActiveBranchFoundException {
        List<Branch> result = null;

        try {
            result = branches.queryBuilder().selectColumns().where().eq("active", true).query();
        } catch (SQLException e) {
            System.out.println("Failed to get active branch");
        }

        assert result != null;
        if (result.size() > 1 || result.isEmpty()) {
            System.out.println("No active branch found");
            throw new NoActiveBranchFoundException();
        }

        return result.get(0);
    }

    public void setActiveBranch(@NotNull Branch branch) throws FailedToSetActiveBranchException, NoActiveBranchFoundException {
        Branch oldBranch = getActiveBranch();

        oldBranch.active = false;
        branch.active = true;

        try {
            branches.update(oldBranch);
            branches.update(branch);
        } catch (SQLException e) {
            throw new FailedToSetActiveBranchException();
        }
    }

    public
    @NotNull
    Branch createBranch(@NotNull String name, boolean active) throws FailedToCreateNewBranchException {
        Branch result = new Branch(name, active);

        try {
            branches.create(result);
        } catch (SQLException e) {
            throw new FailedToCreateNewBranchException();
        }

        return result;
    }

    public
    @NotNull
    Branch getBranch(@NotNull String name) throws FailedGetBranchException {
        List<Branch> result;

        try {
            result = branches.queryBuilder().selectColumns().where().eq("name", name).query();
        } catch (SQLException e) {
            throw new FailedGetBranchException();
        }

        if (result.isEmpty()) {
            throw new FailedGetBranchException();
        }

        return result.get(0);
    }

    public void closeBranch(@NotNull String name) throws FailedGetBranchException, FailedToCloseBranchException {
        Branch branch = getBranch(name);
        branch.closed = false;

        try {
            branches.update(branch);
        } catch (SQLException e) {
            throw new FailedToCloseBranchException();
        }
    }

    public void commit(@NotNull Map<String, String> files, @NotNull String message, @NotNull String author) throws NoActiveBranchFoundException, FailedToCommitException, FailedToGetCommitException, FailedGetCommitFilesException {
        commit(files, message, author, getActiveBranch());
    }

    public void commit(@NotNull Map<String, String> files, @NotNull String message, @NotNull String author, @NotNull Branch branch) throws FailedToCommitException, NoActiveBranchFoundException, FailedToGetCommitException, FailedGetCommitFilesException {

        final Map<String, FileEntity> createdLinks = makeNewLinks(files);

        Commit commit = new Commit(message, author, new Date(Calendar.getInstance().getTimeInMillis()), branch);
        try {
            commits.create(commit);
        } catch (SQLException e) {
            throw new FailedToCommitException();
        }

        createdLinks.entrySet()
                .stream()
                .map(it -> new File(commit, it.getKey(), it.getValue()))
                .forEach(file -> {
                    try {
                        Db.this.files.create(file);
                    } catch (SQLException e) {
                        throw new FailedToCreateFileException(file.path);
                    }
                });

        final Commit lastCommit = getLastCommit(getActiveBranch());
        if (lastCommit == null) {
            return;
        }

        final List<File> lastCommitFiles = getCommitFiles(lastCommit);
        final List<File> oldLinks = lastCommitFiles.stream().filter(it -> !files.containsKey(it.path)).collect(Collectors.toList());
        oldLinks
                .stream()
                .map(it -> new File(commit, it.path, it.entity))
                .forEach(file -> {
                    try {
                        Db.this.files.create(file);
                    } catch (SQLException e) {
                        throw new FailedToCreateFileException(file.path);
                    }
                });
    }

    public
    Commit getLastCommit(@NotNull Branch branch) throws FailedToGetCommitException {
        try {
            return commits
                    .queryBuilder()
                    .selectColumns()
                    .orderBy("submitted", false)
                    .where()
                    .eq("branch_id", branch).queryForFirst();
        } catch (SQLException e) {
            throw new FailedToGetCommitException();
        }
    }

    public
    @NotNull
    Commit getCommitById(int id) throws FailedToGetCommitException {
        try {
            return commits
                    .queryBuilder()
                    .selectColumns()
                    .where()
                    .eq("id", id)
                    .queryForFirst();
        } catch (SQLException e) {
            throw new FailedToGetCommitException();
        }
    }

    public
    @NotNull
    List<File> getCommitFiles(@NotNull Commit commit) throws FailedGetCommitFilesException {
        try {
            return files
                    .queryBuilder()
                    .selectColumns()
                    .where()
                    .eq("commit_id", commit)
                    .query();
        } catch (SQLException e) {
            throw new FailedGetCommitFilesException();
        }

    }

    public void close() {
        try {
            connectionSource.close();
        } catch (IOException e) {
            System.out.println("Failed to close Database");
        }
    }

    public
    @NotNull
    List<Commit> getLog() throws FailedToPrintLogException, NoActiveBranchFoundException {
        try {
            return commits.queryBuilder().selectColumns().orderBy("submitted", true).where().eq("branch_id", getActiveBranch()).query();
        } catch (SQLException e) {
            throw new FailedToPrintLogException();
        }
    }

    private Map<String, FileEntity> makeNewLinks(Map<String, String> files) {
        Map<String, FileEntity> entities = files.entrySet()
                .stream()
                .collect(Collectors.toMap(it -> it.getKey(), it -> new FileEntity(it.getValue())));

        entities.forEach((path, entity) -> {
            try {
                Db.this.fileEntities.create(entity);
            } catch (SQLException e) {
                throw new FailedToCreateFileException(path);
            }

        });

        return entities;
    }

    public FileEntity loadEntity(long id) throws FailedGetCommitFilesException {
        List<FileEntity> result;

        try {
            result = fileEntities.queryBuilder().selectColumns().where().eq("id", id).query();
        } catch (SQLException e) {
            throw new FailedGetCommitFilesException();
        }

        if (result.isEmpty()) {
            throw new FailedGetCommitFilesException();
        }

        return result.get(0);
    }
}
