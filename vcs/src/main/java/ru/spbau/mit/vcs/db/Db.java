package ru.spbau.mit.vcs.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ru.spbau.mit.vcs.db.entities.Branch;
import ru.spbau.mit.vcs.db.entities.Commit;
import ru.spbau.mit.vcs.db.entities.File;

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
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC library not found");
        }

        try {
            connectionSource = new JdbcConnectionSource(String.format("%s%s", dbPath, name));

            branches = DaoManager.createDao(connectionSource, Branch.class);
            commits = DaoManager.createDao(connectionSource, Commit.class);
            files = DaoManager.createDao(connectionSource, File.class);

            TableUtils.createTableIfNotExists(connectionSource, Branch.class);
            TableUtils.createTableIfNotExists(connectionSource, Commit.class);
            TableUtils.createTableIfNotExists(connectionSource, File.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Branch getActiveBranch() {
        List<Branch> result = null;

        try {
            result = branches.queryBuilder().selectColumns().where().eq("active", true).query();
        } catch (SQLException e) {
            System.out.println("Failed to get active branch");
            return null;
        }

        if (result.size() > 1 || result.isEmpty()) {
            System.out.println("No active branch found");
            return null;
        }

        return result.get(0);
    }

    public void setActiveBranch(Branch branch) {
        branch.active = true;

        try {
            branches.update(branch);
        } catch (SQLException e) {
            System.out.println("Failed to set active branch");
        }
    }

    public Branch createBranch(String name) {
        Branch result = new Branch(name, false);

        try {
            branches.create(result);
        } catch (SQLException e) {
            System.out.println(String.format("Failed to create new branch: %s", e.getMessage()));
        }

        return result;
    }

    public Branch getBranch(String name) {
        List<Branch> result = null;

        try {
            result = branches.queryBuilder().selectColumns().where().eq("name", name).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }

    public void closeBranch(String name) {
        Branch branch = getBranch(name);
        branch.closed = false;

        try {
            branches.update(branch);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Commit commit(Map<String, String> files, String message, String author) {
        return commit(files, message, author, getActiveBranch());
    }

    public Commit commit(Map<String, String> files, String message, String author, Branch branch) {
        Commit commit = new Commit(message, author, new Date(Calendar.getInstance().getTimeInMillis()), branch);

        try {
            commits.create(commit);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        files.entrySet()
                .stream()
                .map(file -> new File(commit, file.getKey(), file.getValue()))
                .forEach(file -> {
                    try {
                        Db.this.files.create(file);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                });

        return commit;
    }

    public Commit getLastCommit(Branch branch) {
        try {
            return commits
                    .queryBuilder()
                    .selectColumns()
                    .orderBy("submitted", false)
                    .where()
                    .eq("branch_id", branch).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Commit getCommitById(int id) {
        try {
            return commits
                    .queryBuilder()
                    .selectColumns()
                    .where()
                    .eq("id", id)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<File> getCommitFiles(Commit commit) {
        try {
            return files
                    .queryBuilder()
                    .selectColumns()
                    .where()
                    .eq("commit_id", commit)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void close() {
        try {
            connectionSource.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Commit> getLog() {
        try {
            return commits.queryBuilder().selectColumns().orderBy("submitted", true).where().eq("branch_id", getActiveBranch()).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
