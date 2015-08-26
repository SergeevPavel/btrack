package ru.spbau.sergeev.btrack.server;

import ru.spbau.sergeev.btrack.common.Actor;
import ru.spbau.sergeev.btrack.common.messages.Message;
import ru.spbau.sergeev.btrack.common.messages.SettingsRequest;
import ru.spbau.sergeev.btrack.common.messages.SettingsResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavel
 */
public class Server extends Actor {
    private static Logger log = Logger.getLogger(Server.class.getName());
    private static ConcurrentMap<SocketChannel, ClientState> activeClients = new ConcurrentHashMap<>();

    public Server(InetSocketAddress isa) throws IOException {
        super(isa);
    }

    @Override
    public void onStart() {
    }

    void onSettingsRequest(SettingsRequest msg, SocketChannel socketChannel) {
        try {
            activeClients.putIfAbsent(socketChannel, new ClientState());
            final ClientState clientState = activeClients.get(socketChannel);
            clientState.isa = msg.isa;
            sendMessage(socketChannel, new SettingsResponse(ServerConfig.CHAPTERS_COUNT));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error on Settings response", e);
        }
    }

    @Override
    public void processMessage(Message msg, SocketChannel socketChannel) {
        log.log(Level.INFO, "Server message processor");
        try {
            switch (msg.getType()) {
                case SETTINGS_REQUEST:
                    log.log(Level.INFO, "Got SETTINGS_REQUEST");
                    onSettingsRequest((SettingsRequest) msg, socketChannel);
                    break;
                default:
                    log.log(Level.INFO, "Wrong message type");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error with message processing:", e);
            shutDown();
        }
    }

    @Override
    public void onConnectingFinished(SocketChannel socketChannel) {
        log.log(Level.INFO, "On connected");
    }

    @Override
    public void onDisconnect(SocketChannel socketChannel) {
        log.log(Level.INFO, "Disconnected client");
        activeClients.remove(socketChannel);
    }
}
