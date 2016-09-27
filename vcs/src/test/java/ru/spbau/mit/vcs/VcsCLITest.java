package ru.spbau.mit.vcs;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class VcsCLITest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void initTest() throws IOException {
        final File workingDir = prepareWorkingDir();
        final String[] result = {".auvcs.sqlite"};

        initRepo();
        assertArrayEquals(result, workingDir.list());
    }

    @Test
    public void commitAndCheckoutTest() throws IOException {
        final File workingDir = prepareWorkingDir();
        final File tempFile = new File(String.format("%s/%s", workingDir.getAbsolutePath(), "hello.txt"));
        final String[] beforeCommit = {".auvcs.sqlite"};
        final String[] afterCommit = {".auvcs.sqlite", "hello.txt"};
        final String content = "hello, world";

        initRepo();

        tempFile.createNewFile();
        FileUtils.writeStringToFile(tempFile, content, Charset.defaultCharset());

        makeCommit();

        checkoutCommit(1);
        assertArrayEquals(beforeCommit, workingDir.list());
        checkoutCommit(2);
        assertArrayEquals(afterCommit, workingDir.list());
    }

    @Test
    public void createCheckoutCloseBranchTest() throws IOException {
        final File workingDir = prepareWorkingDir();
        final String content = "hello, world";
        final String[] oldBranch = {".auvcs.sqlite"};
        final String[] newBranch = {".auvcs.sqlite", "hello.txt"};

        initRepo();

        createBranch("hello");
        checkoutBranch("hello");

        final File tempFile = new File(String.format("%s/%s", workingDir.getAbsolutePath(), "hello.txt"));
        tempFile.createNewFile();
        FileUtils.writeStringToFile(tempFile, content, Charset.defaultCharset());

        makeCommit();

        checkoutBranch("master");
        assertArrayEquals(oldBranch, workingDir.list());

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
        makeCommit();

        createBranch("slave");
        checkoutBranch("slave");
        tempFile.createNewFile();
        FileUtils.writeStringToFile(tempFile, contentSlave, Charset.defaultCharset());


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
