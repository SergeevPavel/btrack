package ru.spbau.sergeev.btrack.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavel
 */
public class Main {
    public static void main(String[] args) {
        try {
            Logger log = Logger.getGlobal();
            log.log(Level.INFO, "Server started");
            final Server server = new Server(new InetSocketAddress("localhost", 20001));
            new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
