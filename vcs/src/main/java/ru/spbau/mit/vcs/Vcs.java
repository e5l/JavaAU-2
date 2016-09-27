package ru.spbau.mit.vcs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.vcs.db.Db;
import ru.spbau.mit.vcs.db.entities.Branch;
import ru.spbau.mit.vcs.db.entities.Commit;
import ru.spbau.mit.vcs.db.entities.File;
import ru.spbau.mit.vcs.exceptions.FailedToCommitException;
import ru.spbau.mit.vcs.exceptions.FailedToCreateNewBranchException;
import ru.spbau.mit.vcs.exceptions.FailedToSetActiveBranchException;
import ru.spbau.mit.vcs.exceptions.NoActiveBranchFoundException;
import ru.spbau.mit.vcs.utils.VcsStatus;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public final class Vcs {
    private final Db db;
    private final String dbpath = ".auvcs.sqlite";

    /**
     * Initialize database
     */
    public Vcs() {
        db = new Db(dbpath);
    }

    /**
     * create Db
     */
    public void create() throws FailedToSetActiveBranchException, FailedToCreateNewBranchException, FailedToCommitException, NoActiveBranchFoundException {
        createDb();
    }

    private final void createDb() throws FailedToCreateNewBranchException, FailedToSetActiveBranchException, FailedToCommitException, NoActiveBranchFoundException {
        final Branch master = db.createBranch("master", true);
        db.commit(new HashMap<>(), "initial commit", "vcs");
    }

    /* Commit API */

    /**
     * Get current files changes
     */
    public
    @NotNull
    VcsStatus status() throws Exception {
        final Branch branch = db.getActiveBranch();
        final List<ru.spbau.mit.vcs.db.entities.File> files = db.getCommitFiles(db.getLastCommit(branch));
        final Map<String, String> currentFiles = readLocalFiles();

        final Map<String, String> commitFilenames = files
                .stream()
                .collect(Collectors.toMap(file -> file.path, file -> file.content));

        final Set<String> added = new HashSet<>(currentFiles.keySet());
        final Set<String> removed = new HashSet<>(commitFilenames.keySet());

        added.removeAll(commitFilenames.keySet());
        removed.removeAll(currentFiles.keySet());


        final Set<String> modified = new HashSet<>();
        modified.addAll(currentFiles.keySet());
        modified.removeAll(added);
        modified.removeAll(removed);

        currentFiles.forEach((path, value) -> {
            String inVcs = commitFilenames.get(path);
            if (value.equals(inVcs)) {
                modified.remove(path);
            }
        });

        return new VcsStatus(added, removed, modified);
    }

    /**
     * Commit to current branch
     *
     * @param message
     */
    public void commit(String message, String author) throws Exception {
        db.commit(readLocalFiles(), message, author);
    }

    /**
     * Print history of current branch
     */
    public
    @NotNull
    List<String> log() throws Exception {
        return db
                .getLog()
                .stream()
                .map(commit -> String.format("id: %d, @%s: %s", commit.id, commit.author, commit.message))
                .collect(Collectors.toList());
    }

    /* Branch API */

    /**
     * Create new branch
     *
     * @param name
     */
    public void createBranch(String name) throws Exception {
        final Branch branch = db.createBranch(name, false);
        db.commit(readLocalFiles(), "create new branch", "vcs", branch);
    }

    /**
     * Checkout existing branch
     *
     * @param name
     */
    public void checkoutBranch(String name) throws Exception {
        final Branch branch = db.getBranch(name);
        final Commit commit = db.getLastCommit(branch);
        final List<File> files = db.getCommitFiles(commit);
        replaceRepoContent(files);

        db.setActiveBranch(branch);
    }

    /**
     * Checkout commit
     *
     * @param id commit id
     */
    public void checkoutCommit(int id) throws Exception {
        final Commit commit = db.getCommitById(id);
        final List<File> files = db.getCommitFiles(commit);
        replaceRepoContent(files);

        db.setActiveBranch(commit.branch);
    }

    /**
     * Close branch
     *
     * @param name
     */
    public void closeBranch(String name) throws Exception {
        db.closeBranch(name);
    }

    /**
     * Merge selected branch in current
     *
     * @param name
     */
    public void mergeBranch(String name) throws Exception {
        if (Objects.equals(name, db.getActiveBranch().name)) {
            System.out.println("Can't merge branch with themself");
            return;
        }

        final Branch branch = db.getBranch(name);

        final List<File> other = db.getCommitFiles(db.getLastCommit(branch));
        final List<File> current = db.getCommitFiles(db.getLastCommit(db.getActiveBranch()));

        final Set<String> currentNames = current.stream().map(file -> file.path).collect(Collectors.toSet());
        final Set<String> otherNames = other.stream().map(file -> file.path).collect(Collectors.toSet());

        final List<File> result = new ArrayList<>();
        result.addAll(other.stream().filter(file -> !currentNames.contains(file.path)).collect(Collectors.toList()));
        result.addAll(current.stream().filter(file -> !otherNames.contains(file.path)).collect(Collectors.toList()));

        final Map<String, File> mergeLeftCandidates = current.stream().filter(file -> otherNames.contains(file.path)).collect(Collectors.toMap(file -> file.path, file -> file));
        final Map<String, File> mergeRightCandidates = other.stream().filter(file -> currentNames.contains(file.path)).collect(Collectors.toMap(file -> file.path, file -> file));

        final List<File> merged = mergeLeftCandidates.entrySet().stream().map(pair -> merge(pair.getValue(), mergeRightCandidates.get(pair.getKey()))).collect(Collectors.toList());
        result.addAll(merged);

        replaceRepoContent(result);
    }

    public void close() {
        db.close();
    }

    private
    @NotNull
    Map<String, String> readLocalFiles() {
        return FileUtils.listFiles(getPath(), null, true)
                .stream()
                .filter(file -> !file.getName().equals(".auvcs.sqlite"))
                .collect(Collectors.toMap(java.io.File::getPath, file -> {
                    try {
                        return FileUtils.readFileToString(file, Charset.defaultCharset());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }));
    }

    private void replaceRepoContent(List<File> content) {
        FileUtils
                .listFilesAndDirs(getPath(), TrueFileFilter.TRUE, TrueFileFilter.TRUE)
                .stream()
                .filter(it -> !it.getName().equals(".") && !it.getName().equals(dbpath))
                .forEach(java.io.File::delete);

        content.forEach(loaded -> {
            final java.io.File onFs = new java.io.File(loaded.path);
            try {
                onFs.createNewFile();
                FileUtils.writeStringToFile(onFs, loaded.content, Charset.defaultCharset());
            } catch (IOException e) {
                System.out.println("Failed to checkout file");
            }
        });
    }

    private static final
    @NotNull
    File merge(File left, File right) {
        final String[] leftContent = left.content.split("\n");
        final String[] rightContent = right.content.split("\n");

        final StringBuilder targetContent = new StringBuilder();

        for (int i = 0; i < Math.min(leftContent.length, rightContent.length); i++) {
            if (leftContent[i].equals(rightContent[i])) {
                targetContent.append(String.format("%s\n", leftContent[i]));
                continue;
            }

            targetContent.append(String.format("1> %s\n", leftContent[i]));
            targetContent.append(String.format("2> %s\n", rightContent[i]));
        }

        for (int i = Math.min(leftContent.length, rightContent.length); i < leftContent.length; i++) {
            targetContent.append(String.format("1> %s\n", leftContent[i]));
        }

        for (int i = Math.min(leftContent.length, rightContent.length); i < rightContent.length; i++) {
            targetContent.append(String.format("2> %s\n", rightContent[i]));
        }

        return new File(left.commit, left.path, targetContent.toString());
    }

    private java.io.File getPath() {
        return new java.io.File(System.getProperty("user.dir"));
    }

}
