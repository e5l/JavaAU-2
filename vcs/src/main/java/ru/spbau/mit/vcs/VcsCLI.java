package ru.spbau.mit.vcs;

/*
Система контроля версий, представляющая из себя консольное приложение и умеющая следующее:
commit + commit message (можно еще дату, автора, что угодно, но сообщение обязательно)
создание и удаление ветки
checkout по имени ревизии или ветки
log -- список ревизий вместе с commit message в текущей ветке
merge -- конфликты разрешайте (или не разрешайте) любым разумным способом
*/

import com.j256.ormlite.logger.LocalLog;
import ru.spbau.mit.vcs.utils.VcsStatus;

public class VcsCLI {
    private static Vcs vcs;

    public static void main(String[] args) {
        System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "ERROR");
        vcs = new Vcs();

        if (args.length < 1) {
            help();
            return;
        }

        String command = args[0];
        switch (command) {
            case "init":
                init();
                break;
            case "status":
                status(args);
                break;
            case "log":
                log();
                break;
            case "branch":
                branch(args);
                break;
            case "commit":
                commit(args);
                break;
            case "checkout":
                checkout(args);
                break;
            case "merge":
                merge(args);
                break;
            default:
                help();
                break;
        }

        vcs.close();
    }


    private static void init() {
        vcs.create();
        System.out.println("create empty repository");
    }

    private static void status(String[] args) {
        VcsStatus status = vcs.status();

        System.out.println("Added:");
        status.getAdded().forEach(name -> System.out.println(String.format("+ %s", name)));
        System.out.println();

        System.out.println("Removed:");
        status.getRemoved().forEach(name -> System.out.println(String.format("- %s", name)));
        System.out.println();

        System.out.println("Modified:");
        status.getModified().forEach(name -> System.out.println(String.format("~ %s", name)));
        System.out.println();
    }

    private static void log() {
        System.out.println("Log:");
        vcs.log().forEach(line -> System.out.println(line));
    }

    private static void branch(String[] args) {
        if (args.length == 1) {
            vcs.branches().forEach(line -> System.out.println(line));
            return;
        }

        if (args.length != 3) {
            System.out.println("branch: invalid arguments count");
            return;
        }

        String name = args[2];
        switch (args[1]) {
            case "create":
                vcs.createBranch(name);
                break;
            case "close":
                vcs.closeBranch(name);
                break;
            default:
                System.out.println("Invalid branch command");
                break;
        }
    }

    private static void commit(String[] args) {
        if (args.length < 3) {
            System.out.println("Enter commit message and author");
            return;
        }

        String message = args[1];
        String author = args[2];

        vcs.commit(message, author);
    }

    private static void merge(String[] args) {
        if (args.length != 2) {
            System.out.println("Merge: invalid arguments count");
            return;
        }

        vcs.mergeBranch(args[1]);
    }

    private static void checkout(String[] args) {
        if (args.length != 3) {
            System.out.println("Invalid args count");
            return;
        }

        switch (args[1]) {
            case "branch":
                vcs.checkoutBranch(args[2]);
                break;
            case "commit":
                try {
                    int number = Integer.parseInt(args[2]);
                    vcs.checkoutCommit(number);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format");
                }
                break;
            default:
                System.out.println("Invalid checkout command");
                break;
        }
    }

    private static void help() {
    }
}
