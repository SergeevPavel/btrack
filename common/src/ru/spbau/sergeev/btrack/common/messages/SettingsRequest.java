package ru.spbau.sergeev.btrack.common.messages;

/**
 * @author pavel
 */
public class SettingsRequest implements Message {
    public SettingsRequest() {
    }

    @Override
    public MessageType getType() {
        return MessageType.SETTINGS_REQUEST;
    }
}
