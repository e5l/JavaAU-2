package ru.spbau.mit.vcs;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertArrayEquals;

public class VcsCLITest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void init() throws IOException {
        final File workingDir = prepareWorkingDir();
        final String[] result = {".auvcs.sqlite"};

        initRepo();
        assertArrayEquals(result, workingDir.list());
    }

    @Test
    public void commitAndCheckout() throws IOException {
//        final File workingDir = prepareWorkingDir();
//        final File tempFile = new File(String.format("%s/%s", workingDir.getAbsolutePath(), "hello.txt"));
//        final String[] beforeCommit = {".auvcs.sqlite"};
//        final String[] afterCommit = {".auvcs.sqlite", "hello.txt"};
//        final String content = "hello, world";
//
//        initRepo();
//
//        tempFile.createNewFile();
//        FileUtils.writeStringToFile(tempFile, content, Charset.defaultCharset());
//
//        makeCommit();
//
//        checkoutCommit(1);
//        assertArrayEquals(beforeCommit, workingDir.list());
//        checkoutCommit(2);
//        assertArrayEquals(afterCommit, workingDir.list());
    }

    @Test
    public void createBranch() throws IOException {
        File workingDir = prepareWorkingDir();
    }

    @Test
    public void closeBranch() throws IOException {
        File workingDir = prepareWorkingDir();
    }

    @Test
    public void checkoutBranch() throws IOException {
        File workingDir = prepareWorkingDir();
    }

    @Test
    public void merge() throws IOException {
        File workingDir = prepareWorkingDir();
    }

    @Test
    public void log() throws IOException {
        File workingDir = prepareWorkingDir();
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
}
