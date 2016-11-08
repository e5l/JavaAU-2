import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.torrent.client.Client;
import ru.spbau.mit.torrent.client.exceptions.UpdateFailedException;
import ru.spbau.mit.torrent.server.Server;
import ru.spbau.mit.torrent.storage.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TorrentTests {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void listingTest() throws IOException, ClassNotFoundException, InterruptedException, UpdateFailedException {
        final File clientFolder = createFolder("client1");
        final File file = createFile(clientFolder, "hello.bin", 1599);

        final Server server = new Server(8081, clientFolder.getAbsolutePath());
        final Client client = new Client(8082, "127.0.0.1", 8081, name -> {}, clientFolder.getAbsolutePath());

        client.upload(file.getAbsolutePath());
        final Map<Integer, FileInfo> result = client.listFiles();
        final Map<Integer, FileInfo> expected = new HashMap<>();
        expected.put(0, new FileInfo(0, file.getName(), file.length()));

        assertEquals(expected, result);
        server.stop();
    }

    @Test
    public void simpleDownloadTest() throws IOException, ClassNotFoundException, InterruptedException, UpdateFailedException {
        final File client1Folder = createFolder("client1");
        final File client2Folder = createFolder("client2");
        final File expected = createFile(client1Folder, "hello.bin", 1024 * 5);
        AtomicBoolean downloaded = new AtomicBoolean(false);

        final Server server = new Server(8081, client1Folder.getAbsolutePath());
        final Client client1 = new Client(8082, "127.0.0.1", 8081, name -> {}, client1Folder.getAbsolutePath());

        client1.upload(expected.getAbsolutePath());

        final Client client2 = new Client(8083, "127.0.0.1", 8081, name -> downloaded.set(true), client2Folder.getAbsolutePath());

        FileInfo fileInfo = client2.listFiles().get(0);
        client2.download(fileInfo.id, new File(client2Folder.getAbsolutePath(), "hello.bin").getAbsolutePath());

        while (!downloaded.get()) {
        }
        final File result = new File(client2Folder.getAbsolutePath(), "hello.bin");
        assertFileEquals(expected, result);
        server.stop();
    }

    @Test
    public void complexFileSizeTest() throws IOException, UpdateFailedException {
        final File client1Folder = createFolder("client1");
        final File client2Folder = createFolder("client2");
        final File expected = createFile(client1Folder, "hello.bin", 1000 * 15);
        AtomicBoolean downloaded = new AtomicBoolean(false);

        final Server server = new Server(8081, client1Folder.getAbsolutePath());
        final Client client1 = new Client(8082, "127.0.0.1", 8081, name -> {}, client1Folder.getAbsolutePath());

        client1.upload(expected.getAbsolutePath());

        final Client client2 = new Client(8083, "127.0.0.1", 8081, name -> downloaded.set(true), client2Folder.getAbsolutePath());

        FileInfo fileInfo = client2.listFiles().get(0);
        client2.download(fileInfo.id, new File(client2Folder.getAbsolutePath(), "hello.bin").getAbsolutePath());

        while (!downloaded.get()) {
        }
        final File result = new File(client2Folder.getAbsolutePath(), "hello.bin");
        assertFileEquals(expected, result);
        server.stop();
    }

    @Test
    public void multipleClientTest() throws IOException, UpdateFailedException {
        final File client1Folder = createFolder("client1");
        final File client2Folder = createFolder("client2");
        final File client3Folder = createFolder("client3");
        final File expected = createFile(client1Folder, "hello.bin", 1000 * 15);
        AtomicInteger downloaded = new AtomicInteger(0);

        final Server server = new Server(8081, client1Folder.getAbsolutePath());
        final Client client1 = new Client(8082, "127.0.0.1", 8081, name -> {
        }, client1Folder.getAbsolutePath());

        client1.upload(expected.getAbsolutePath());

        final Client client2 = new Client(8083, "127.0.0.1", 8081, name -> downloaded.incrementAndGet(), client2Folder.getAbsolutePath());
        final Client client3 = new Client(8084, "127.0.0.1", 8081, name -> downloaded.incrementAndGet(), client3Folder.getAbsolutePath());

        FileInfo file1Info = client2.listFiles().get(0);
        client2.download(file1Info.id, new File(client2Folder.getAbsolutePath(), "hello.bin").getAbsolutePath());

        FileInfo file2Info = client3.listFiles().get(0);
        client3.download(file2Info.id, new File(client3Folder.getAbsolutePath(), "hello.bin").getAbsolutePath());

        while (downloaded.get() != 2) {
        }
        final File result1 = new File(client2Folder.getAbsolutePath(), "hello.bin");
        final File result2 = new File(client3Folder.getAbsolutePath(), "hello.bin");

        assertFileEquals(expected, result1);
        assertFileEquals(expected, result2);
        server.stop();
    }

    @Test
    public void persistenceTest() throws IOException, UpdateFailedException {
        final File clientFolder = createFolder("client1");
        final File file = createFile(clientFolder, "hello.bin", 1599);

        final Server server = new Server(8081, clientFolder.getAbsolutePath());
        final Client client = new Client(8082, "127.0.0.1", 8081, name -> {}, clientFolder.getAbsolutePath());

        client.upload(file.getAbsolutePath());
        final Map<Integer, FileInfo> expected = client.listFiles();

        client.stop();
        server.stop();

        final Server serverSecond = new Server(8081, clientFolder.getAbsolutePath());
        final Client clientSecond = new Client(8082, "127.0.0.1", 8081, name -> {}, clientFolder.getAbsolutePath());

        final Map<Integer, FileInfo> result = clientSecond.listFiles();
        assertEquals(expected, result);
        serverSecond.stop();
    }

    private File createFolder(String name) throws IOException {
        return folder.newFolder(name);
    }

    private File createFile(File folder, String name, int length) throws IOException {
        final File file = new File(folder.getAbsoluteFile(), name);
        file.createNewFile();

        final RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        final Random random = new Random();

        byte[] data = new byte[length];
        random.nextBytes(data);
        raFile.write(data);
        raFile.close();
        return file;
    }

    private void assertFileEquals(File expected, File result) throws IOException {
        assertEquals(expected.length(), result.length());
        final RandomAccessFile left = new RandomAccessFile(expected, "r");
        final RandomAccessFile right = new RandomAccessFile(result, "r");

        byte[] leftData = new byte[(int) expected.length()];
        left.read(leftData);

        byte[] rightData = new byte[(int) result.length()];
        right.read(rightData);

        assertArrayEquals(leftData, rightData);
    }
}
