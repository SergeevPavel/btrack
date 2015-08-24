package ru.spbau.sergeev.btrack.client;

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
            log.log(Level.INFO, "Client started");
            final Client client = new Client(new InetSocketAddress("localhost", 20002));
            new Thread(client).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
