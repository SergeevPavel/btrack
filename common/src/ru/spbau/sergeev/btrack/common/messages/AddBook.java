package ru.spbau.sergeev.btrack.common.messages;

/**
 * @author pavel
 */
public class AddBook implements Message {
    String bookName;

    public AddBook(String bookName) {
        this.bookName = bookName;
    }

    @Override
    public MessageType getType() {
        return MessageType.ADD_BOOK;
    }
}
