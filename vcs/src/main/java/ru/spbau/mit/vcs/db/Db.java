package ru.spbau.mit.vcs.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.vcs.db.entities.Branch;
import ru.spbau.mit.vcs.db.entities.Commit;
import ru.spbau.mit.vcs.db.entities.File;
import ru.spbau.mit.vcs.exceptions.*;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class Db {
    private JdbcConnectionSource connectionSource;

    private Dao<Branch, String> branches;
    private Dao<Commit, Long> commits;
    private Dao<File, Long> files;

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

        TableUtils.createTableIfNotExists(connectionSource, Branch.class);
        TableUtils.createTableIfNotExists(connectionSource, Commit.class);
        TableUtils.createTableIfNotExists(connectionSource, File.class);
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

    public void commit(@NotNull Map<String, String> files, @NotNull String message, @NotNull String author) throws NoActiveBranchFoundException, FailedToCommitException {
        commit(files, message, author, getActiveBranch());
    }

    public void commit(@NotNull Map<String, String> files, @NotNull String message, @NotNull String author, @NotNull Branch branch) throws FailedToCommitException {
        Commit commit = new Commit(message, author, new Date(Calendar.getInstance().getTimeInMillis()), branch);

        try {
            commits.create(commit);
        } catch (SQLException e) {
            throw new FailedToCommitException();
        }

        files.entrySet()
                .stream()
                .map(file -> new File(commit, file.getKey(), file.getValue()))
                .forEach(file -> {
                    try {
                        Db.this.files.create(file);
                    } catch (SQLException e) {
                        throw new FailedToCreateFileException(file.path);
                    }

                });
    }

    public
    @NotNull
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

}
