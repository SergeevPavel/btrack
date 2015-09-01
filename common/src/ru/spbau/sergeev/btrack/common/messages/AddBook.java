package ru.spbau.sergeev.btrack.common.messages;

/**
 * @author pavel
 */
public class AddBook implements Message {
    public String bookName;
    public long size;

    public AddBook(String bookName, long size) {
        this.bookName = bookName;
        this.size = size;
    }

    @Override
    public MessageType getType() {
        return MessageType.ADD_BOOK;
    }
}
