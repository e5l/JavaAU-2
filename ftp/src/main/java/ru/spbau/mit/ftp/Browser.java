package ru.spbau.mit.ftp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import ru.spbau.mit.ftp.server.exception.MustBeDirectoryException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Browser {
    private final File workingDir;

    public Browser(String path) throws MustBeDirectoryException, FileNotFoundException {
        workingDir = new File(path);

        if (!workingDir.isDirectory()) {
            throw new MustBeDirectoryException(path);
        }

        if (!workingDir.exists()) {
            throw new FileNotFoundException(path);
        }
    }

    public List<ImmutablePair<String, Boolean>> listDirectory(String path) {
        final File folder = new File(workingDir, path);
        if (!folder.exists()) {
            return new ArrayList<>();
        }

        return Arrays
                .stream(folder.listFiles())
                .map(it -> new ImmutablePair<>(it.getName(), it.isDirectory()))
                .collect(Collectors.toList());
    }

    public List<Byte> readFile(String path) throws IOException {
        final File folder = new File(workingDir, path);
        if (!folder.exists() || folder.isDirectory()) {
            throw new FileNotFoundException();
        }

        final byte[] bytes = FileUtils.readFileToByteArray(folder);

        final ArrayList<Byte> result = new ArrayList<>();
        result.ensureCapacity(bytes.length);

        for (byte it : bytes) {
            result.add(it);
        }

        return result;
    }
}
