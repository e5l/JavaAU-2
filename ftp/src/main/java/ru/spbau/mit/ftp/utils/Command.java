package ru.spbau.mit.ftp.utils;

import ru.spbau.mit.ftp.server.exception.UnknownCommandException;

public enum Command {
    DISCONNECT(0),
    LIST(1),
    GET(2);

    public final int id;

    Command(int id) {
        this.id = id;
    }

    public static Command fromInt(int id) throws UnknownCommandException {
        for (Command i : Command.values()) {
            if (i.id == id) {
                return i;
            }
        }

        throw new UnknownCommandException(id);
    }


}
