package ru.spbau.mit.torrent.client;

import ru.spbau.mit.torrent.client.exceptions.UpdateFailedException;
import ru.spbau.mit.torrent.storage.FileInfo;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class ClientREPL {
    private final static Scanner scanner = new Scanner(System.in);
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final short SERVER_PORT = 8081;
    private static Client client;

    public static void main(String[] args) {
        short port = 8082;

        if (args.length > 1) {
            port = Short.parseShort(args[1]);
        }

        try {
            client = new Client(port, SERVER_ADDRESS, SERVER_PORT, file -> System.out.printf("Downloaded: %s%n", file), System.getProperty("user.dir"));
        } catch (IOException e) {
            System.out.println("Couldn't connect to server: " + e.getMessage());
        }

        boolean run = true;
        while (run) {
            try {
                run = evaluateCommand();
            } catch (Exception e) {
                System.out.println("Exception happened: " + e.getMessage());
            }
        }

        client.stop();
    }

    private static boolean evaluateCommand() throws IOException, UpdateFailedException {
        String command = scanner.nextLine();
        String[] commandWithArgs = command.split(" ", 2);
        if (commandWithArgs.length == 0) {
            return true;
        }

        switch (commandWithArgs[0].toLowerCase()) {
            case "list":
                Map<Integer, FileInfo> files = client.listFiles();
                files.entrySet()
                        .forEach(fileInfo -> System.out.println(
                                String.format("%d: %s; size: %d",
                                        fileInfo.getValue().id,
                                        fileInfo.getValue().name,
                                        fileInfo.getValue().size)));
                break;
            case "upload":
                if (commandWithArgs.length != 2) {
                    System.out.println("Enter filename");
                    return true;
                }

                client.upload(commandWithArgs[1]);
                break;
            case "download":
                if (commandWithArgs.length != 3) {
                    System.out.println("Enter file id and destination");
                    return true;
                }

                int id = Integer.parseInt(commandWithArgs[1]);
                String destination = commandWithArgs[2];
                client.download(id, destination);
                break;
            case "exit":
                return false;
            default:
                System.out.printf("%s: unknown command%n", commandWithArgs[0]);
                break;
        }
        return true;
    }

}
