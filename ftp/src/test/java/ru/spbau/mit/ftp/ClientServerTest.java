package ru.spbau.mit.ftp;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.ftp.client.FtpClient;
import ru.spbau.mit.ftp.server.FtpServer;
import ru.spbau.mit.ftp.server.exception.MustBeDirectoryException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class ClientServerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void listDirTest() throws IOException, MustBeDirectoryException {
        final File root = prepareRoot();
        final Thread server = runServer(8080);
        final FtpClient client = connect(8080);
        final Browser browser = new Browser(root.getAbsolutePath());

        assertEquals(browser.listDirectory(""), client.listDirectory(""));
        assertEquals(0, client.listDirectory("42").size());

        server.interrupt();
    }

    @Test
    public void getFileTest() throws IOException, MustBeDirectoryException {
        final File root = prepareRoot();
        final Thread server = runServer(8080);
        final FtpClient client = connect(8080);
        final Browser browser = new Browser(root.getAbsolutePath());

        final ArrayList<Byte> result = new ArrayList<>();
        final byte[] data = client.readFile("hello.txt");
        for (byte b : data) {
            result.add(b);
        }

        assertEquals(browser.readFile("hello.txt"), result);
        assertEquals(0, client.readFile("hello2.txt").length);

        server.interrupt();
    }

    @Test
    public void disconnectTest() throws IOException, MustBeDirectoryException {
        prepareRoot();
        final Thread server = runServer(8080);
        final FtpClient client = connect(8080);

        client.close();
        server.interrupt();
    }

    private File prepareRoot() throws IOException {
        final File root = folder.newFolder("root");

        System.setProperty("user.dir", root.getAbsolutePath());

        final File hello = FileUtils.getFile(root.getAbsoluteFile(), "hello.txt");
        hello.createNewFile();
        FileUtils.write(hello, "Hello, world!", Charset.defaultCharset());
        return root;
    }

    private Thread runServer(int port) throws IOException {
        final Thread server = new Thread(new FtpServer(port));

        server.start();
        return server;
    }

    private FtpClient connect(int port) throws IOException {
        return new FtpClient("127.0.0.1", port);
    }

}
