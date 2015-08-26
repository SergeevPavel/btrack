package ru.spbau.sergeev.btrack.client;

import ui.LoginWindow;
import ui.MainWindow;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavel
 */
public class Main {
    private static void runBackend(String[] args) {
        try {
            Logger log = Logger.getGlobal();
            int port = Integer.parseInt(args[0]);
            log.log(Level.INFO, String.format("Client started on %d", port));
            final Client client = new Client(new InetSocketAddress("localhost", port));
            new Thread(client).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showUI() {
        LoginWindow loginWindow = new LoginWindow();
        final InetSocketAddress isa = loginWindow.getIsa();
        if (isa != null) {
            MainWindow mainWindow = new MainWindow();
        }
    }

    public static void main(String[] args) {
//        javax.swing.SwingUtilities.invokeLater(Main::showUI);
        showUI();
    }
}
