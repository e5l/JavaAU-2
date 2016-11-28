package ru.spbau.mit.torrent.client.ui;

public class ClientConfig {
    private final String serverUrl;
    private final int clientPort;
    private final String configPath;

    public ClientConfig(String serverUrl, String clientPort, String configPath) {
        this.serverUrl = serverUrl;
        this.clientPort = Integer.parseInt(clientPort);
        this.configPath = configPath;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public int getClientPort() {
        return clientPort;
    }

    public String getConfigPath() {
        if (configPath.equals("")) {
            return System.getProperty("user.dir");
        }

        return configPath;
    }
}
