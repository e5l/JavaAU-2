package ru.spbau.mit.torrent.client.utils;

import ru.spbau.mit.torrent.storage.BlockFile;

import java.io.IOException;

public interface OnDownload {
    void onDownload(BlockFile name) throws IOException;
}
