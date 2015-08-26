package ru.spbau.sergeev.btrack.client;

import ru.spbau.sergeev.btrack.common.Actor;
import ru.spbau.sergeev.btrack.common.messages.Message;
import ru.spbau.sergeev.btrack.common.messages.SettingsRequest;
import ru.spbau.sergeev.btrack.common.messages.SettingsResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavel
 */
public class Client extends Actor {
    private static Logger log = Logger.getLogger(Client.class.getName());
    private final InetSocketAddress isa;

    public Client(InetSocketAddress isa) throws IOException {
        super(isa);
        this.isa = isa;
    }

    @Override
    public void onStart() {
        try {
            initiateConnection(new InetSocketAddress("localhost", 20001));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void onSettingsResponse(SettingsResponse msg, SocketChannel socketChannel) {
        System.out.println(msg.N);
        closeConnection(socketChannel);
        shutDown();
    }

    @Override
    public void processMessage(Message msg, SocketChannel socketChannel) {
        log.log(Level.INFO, "Client message processor");
        try {
            switch (msg.getType()) {
                case SETTINGS_RESPONSE:
                    log.log(Level.INFO, "Got SETTINGS_RESPONSE");
                    onSettingsResponse((SettingsResponse) msg, socketChannel);
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
        try {
            sendMessage(socketChannel, new SettingsRequest(isa));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnect(SocketChannel socketChannel) {

    }
}
