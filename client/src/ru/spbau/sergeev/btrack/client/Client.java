package ru.spbau.sergeev.btrack.client;

import ru.spbau.sergeev.btrack.common.Actor;
import ru.spbau.sergeev.btrack.common.messages.Message;
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

    public Client(InetSocketAddress isa) throws IOException {
        super(isa);
    }

    void onSettingsResponse(SettingsResponse msg, SocketChannel socketChannel) {
        System.out.println(msg.N);
    }

    @Override
    public void processMessage(Message msg, SocketChannel socketChannel) {
        log.log(Level.INFO, "Client message processor");
        switch (msg.getType()) {
            case SETTINGS_RESPONSE:
                log.log(Level.INFO, "Got SETTINGS_RESPONSE");
                onSettingsResponse((SettingsResponse) msg, socketChannel);
                break;
            default:
                log.log(Level.INFO, "Wrong message type");
        }
    }
}
