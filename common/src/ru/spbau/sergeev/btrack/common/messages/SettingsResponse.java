package ru.spbau.sergeev.btrack.common.messages;

/**
 * @author pavel
 */
public class SettingsResponse implements Message {
    public final int N;

    public SettingsResponse(int N) {
        this.N = N;
    }

    @Override
    public MessageType getType() {
        return MessageType.SETTINGS_RESPONSE;
    }
}
