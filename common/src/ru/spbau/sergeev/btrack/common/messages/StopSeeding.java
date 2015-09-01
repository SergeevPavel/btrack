package ru.spbau.sergeev.btrack.common.messages;

/**
 * @author pavel
 */
public class StopSeeding implements Message {
    public String bookName;

    public StopSeeding(String bookName) {
        this.bookName = bookName;
    }

    @Override
    public MessageType getType() {
        return MessageType.STOP_SEEDING;
    }
}
