import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.torrent.client.Client;
import ru.spbau.mit.torrent.client.exceptions.UpdateFailedException;
import ru.spbau.mit.torrent.client.storage.FileInfo;
import ru.spbau.mit.torrent.server.Server;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

public class TorrentTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void listingTest() throws IOException, ClassNotFoundException, InterruptedException, UpdateFailedException {
        final Server server = new Server(8081);
        server.start();

        final File clientFolder = createFolder("client1");
        final File file = createFile(clientFolder, "hello.bin", 1599);
        final Client client = new Client(8082, "127.0.0.1", 8081, name -> {
        });

        client.upload(file.getAbsolutePath());
        final HashMap<Integer, FileInfo> result = client.listFiles();
        final HashMap<Integer, FileInfo> expected = new HashMap<>();
        expected.put(0, new FileInfo(0, file.length(), file.getName()));

        assertEquals(expected, result);
        server.close();
        server.join();
    }

    @Test
    public void downloadTest() throws IOException, ClassNotFoundException, InterruptedException, UpdateFailedException {
        final Server server = new Server(8081);
        server.start();

        final File client1Folder = createFolder("client1");
        final File client2Folder = createFolder("client2");
        final File expected = createFile(client1Folder, "hello.bin", 1024 * 50);
        AtomicBoolean downloaded = new AtomicBoolean(false);

        final Client client1 = new Client(8082, "127.0.0.1", 8081, name -> {
        });

        client1.upload(expected.getAbsolutePath());

        final Client client2 = new Client(8083, "127.0.0.1", 8081, name -> downloaded.set(true));

        FileInfo fileInfo = client2.listFiles().get(0);
        client2.download(fileInfo.id, new File(client2Folder.getAbsolutePath(), "hello.bin").getAbsolutePath());

        while (!downloaded.get()) {}
        final File result = new File(client2Folder.getAbsolutePath(), "hello.bin");

        server.close();
        server.join();
    }

    public File createFolder(String name) throws IOException {
        return folder.newFolder(name);
    }

    private File createFile(File folder, String name, long length) throws IOException {
        final File file = new File(folder.getAbsoluteFile(), name);
        file.createNewFile();

        final RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        raFile.setLength(length);
        raFile.close();

        return file;
    }
}
