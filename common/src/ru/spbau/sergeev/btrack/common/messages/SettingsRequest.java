package ru.spbau.sergeev.btrack.common.messages;

import java.net.InetSocketAddress;

/**
 * @author pavel
 */
public class SettingsRequest implements Message {
    public final InetSocketAddress isa;

    public SettingsRequest(InetSocketAddress isa) {
        this.isa = isa;
    }

    @Override
    public MessageType getType() {
        return MessageType.SETTINGS_REQUEST;
    }
}
