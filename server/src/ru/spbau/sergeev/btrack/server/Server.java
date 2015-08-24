package ru.spbau.sergeev.btrack.server;

import ru.spbau.sergeev.btrack.common.Actor;
import ru.spbau.sergeev.btrack.common.messages.Message;
import ru.spbau.sergeev.btrack.common.messages.SettingsRequest;
import ru.spbau.sergeev.btrack.common.messages.SettingsResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavel
 */
public class Server extends Actor {
    private static Logger log = Logger.getLogger(Server.class.getName());
    private static Map<SocketChannel, ClientState> activeClients = new HashMap<>();

    public Server(InetSocketAddress isa) throws IOException {
        super(isa);
    }

    @Override
    public void onStart() {
    }

    void onSettingsRequest(SettingsRequest msg, SocketChannel socketChannel) {
        try {
            sendMessage(socketChannel, new SettingsResponse(ServerConfig.CHAPTERS_COUNT));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error on Settings response", e);
        }
    }

    @Override
    public void processMessage(Message msg, SocketChannel socketChannel) {
        log.log(Level.INFO, "Server message processor");
        switch (msg.getType()) {
            case SETTINGS_REQUEST:
                log.log(Level.INFO, "Got SETTINGS_REQUEST");
                onSettingsRequest((SettingsRequest)msg, socketChannel);
                break;
            default:
                log.log(Level.INFO, "Wrong message type");
        }
    }

    @Override
    public void onConnect(SocketChannel socketChannel) {

    }

    @Override
    public void onDisconnect(SocketChannel socketChannel) {

    }
}
