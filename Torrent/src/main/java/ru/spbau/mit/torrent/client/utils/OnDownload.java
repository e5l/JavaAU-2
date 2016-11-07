package ru.spbau.mit.torrent.client.utils;

import ru.spbau.mit.torrent.client.storage.BlockFile;

public interface OnDownload {
    void onDownload(BlockFile name);
}
