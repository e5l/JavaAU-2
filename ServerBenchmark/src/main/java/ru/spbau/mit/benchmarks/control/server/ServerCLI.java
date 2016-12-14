package ru.spbau.mit.benchmarks.control.server;

import java.io.IOException;

public class ServerCLI {
    private static final int CONTROL_PORT = 8081;

    public static void main(String[] args) throws IOException {
        final Server controlServer = new Server(CONTROL_PORT);
        final Thread controlThread = new Thread(controlServer);

        controlThread.start();
        System.in.read();
        controlServer.stop();
    }
}
