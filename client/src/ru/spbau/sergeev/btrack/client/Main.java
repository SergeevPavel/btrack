package ru.spbau.sergeev.btrack.client;

import ui.MainWindow;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author pavel
 */
public class Main {
//    private static void runBackend(String[] args) {
//        try {
//            Logger log = Logger.getGlobal();
//            int port = Integer.parseInt(args[0]);
//            Path downloadPath = Paths.get(args[1]);
//            log.log(Level.INFO, String.format("Client started on %d", port));
//            final Client client = new Client(new InetSocketAddress("localhost", port), downloadPath);
//            new Thread(client).start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void showUI(String[] args) {
        //LoginWindow loginWindow = new LoginWindow(); TODO uncomment this line
        //final InetSocketAddress isa = loginWindow.getIsa();

        int port = Integer.parseInt(args[0]);
        Path downloadPath = Paths.get(args[1]);

        InetSocketAddress isa = new InetSocketAddress("localhost", port);
        if (isa != null) {
            try {
                Client.initClient(isa, downloadPath);
                Client.client.lockBeforeServerConnecting();
            } catch (Exception e) {
                e.printStackTrace();
            }
            MainWindow mainWindow = new MainWindow(Client.client);
        }
    }

    public static void main(String[] args) {
//        javax.swing.SwingUtilities.invokeLater(Main::showUI);
        showUI(args);
//        runBackend(args);
    }
}
