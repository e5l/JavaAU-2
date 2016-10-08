package ru.spbau.mit.vcs;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.vcs.utils.VcsStatus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class VcsCLITest {
    private static final String[] DEFAULT_CONTENT = {".auvcs.sqlite", ".tracked.list"};

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void initTest() throws IOException {
        final File workingDir = prepareWorkingDir();

        initRepo();
        assertArrayEquals(DEFAULT_CONTENT, workingDir.list());
    }

    @Test
    public void addRemoveStatusTest() throws Exception {
        final File workingDir = prepareWorkingDir();
        final File tempFile = new File(String.format("%s/%s", workingDir.getAbsolutePath(), "hello.txt"));
        final String content = "hello, world";

        initRepo();

        tempFile.createNewFile();
        FileUtils.writeStringToFile(tempFile, content, Charset.defaultCharset());

        final String beforeAdd = checkStatus();
        addFile("hello.txt");
        final String beforeRemove = checkStatus();
        removeFile("hello.txt");
        final String afterRemove = checkStatus();

        assertEquals(beforeAdd, afterRemove);
        assertNotEquals(beforeRemove, afterRemove);
    }

    @Test
    public void createCheckoutCloseBranchTest() throws IOException {
        final File workingDir = prepareWorkingDir();
        final String content = "hello, world";
        final String[] newBranch = {".auvcs.sqlite", "hello.txt", ".tracked.list"};

        initRepo();

        createBranch("hello");
        checkoutBranch("hello");

        final File tempFile = new File(String.format("%s/%s", workingDir.getAbsolutePath(), "hello.txt"));
        tempFile.createNewFile();
        FileUtils.writeStringToFile(tempFile, content, Charset.defaultCharset());

        addFile("hello.txt");
        makeCommit();

        checkoutBranch("master");
        assertArrayEquals(DEFAULT_CONTENT, workingDir.list());

        checkoutBranch("hello");
        assertArrayEquals(newBranch, workingDir.list());

        checkoutBranch("master");
        closeBranch("hello");
    }

    @Test
    public void mergeTest() throws IOException {
        final File workingDir = prepareWorkingDir();
        final File tempFile = new File(String.format("%s/%s", workingDir.getAbsolutePath(), "hello.txt"));
        final String contentMaster = "Hello, Master";
        final String contentSlave = "Hello, slave";

        initRepo();

        tempFile.createNewFile();
        FileUtils.writeStringToFile(tempFile, contentMaster, Charset.defaultCharset());

        addFile("hello.txt");
        makeCommit();

        createBranch("slave");
        checkoutBranch("slave");
        tempFile.createNewFile();
        FileUtils.writeStringToFile(tempFile, contentSlave, Charset.defaultCharset());

        addFile("hello.txt");
        makeCommit();

        checkoutBranch("master");
        mergeBranch("slave");

        final String mergedContent = FileUtils.readFileToString(tempFile, Charset.defaultCharset());

        assertEquals(String.format("1> %s\n2> %s\n", contentMaster, contentSlave), mergedContent);
    }


    @Test
    public void logTest() throws Exception {
        File workingDir = prepareWorkingDir();
        String[] expected = {
                "id: 1, @vcs: initial commit",
                "id: 2, @vcsMaster: hello",
                "id: 3, @vcsMaster: hello",
                "id: 4, @vcsMaster: hello",
                "id: 5, @vcsMaster: hello",
                "id: 6, @vcsMaster: hello",
                "id: 7, @vcsMaster: hello",
                "id: 8, @vcsMaster: hello",
                "id: 9, @vcsMaster: hello",
                "id: 10, @vcsMaster: hello",
                "id: 11, @vcsMaster: hello"
        };

        initRepo();

        for (int i = 0; i < 10; i++) {
            makeCommit();
        }

        Vcs vcs = new Vcs();
        assertArrayEquals(expected, vcs.log().toArray());
    }

    private File prepareWorkingDir() throws IOException {
        File workingDir = folder.newFolder();
        System.setProperty("user.dir", workingDir.getAbsolutePath());

        return workingDir;
    }

    private void initRepo() {
        final String[] args = {"init"};
        VcsCLI.main(args);
    }

    private void addFile(String name) {
        final String[] args = {"add", name};
        VcsCLI.main(args);
    }

    private void removeFile(String name) {
        final String[] args = {"remove", name};
        VcsCLI.main(args);
    }

    private String checkStatus() throws Exception {
        final Vcs vcs = new Vcs();
        VcsStatus status = vcs.status();
        return String.format("%s %s %s", status.getAdded().toString(), status.getModified().toString(), status.getRemoved().toString());
    }

    private void makeCommit() {
        final String[] args = {"commit", "hello", "vcsMaster"};
        VcsCLI.main(args);
    }

    private void checkoutCommit(int id) {
        final String[] args = {"checkout", "commit", Integer.toString(id)};
        VcsCLI.main(args);
    }

    private void checkoutBranch(String name) {
        final String[] args = {"checkout", "branch", name};
        VcsCLI.main(args);
    }

    private void createBranch(String name) {
        final String[] args = {"branch", "create", name};
        VcsCLI.main(args);
    }

    private void closeBranch(String name) {
        final String[] args = {"branch", "close", name};
        VcsCLI.main(args);
    }

    private void mergeBranch(String name) {
        final String[] args = {"merge", name};
        VcsCLI.main(args);
    }

}