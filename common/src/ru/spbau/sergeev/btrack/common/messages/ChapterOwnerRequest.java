package ru.spbau.sergeev.btrack.common.messages;

/**
 * @author pavel
 */
public class ChapterOwnerRequest implements Message {
    public String bookName;
    public int chapterNumber;

    public ChapterOwnerRequest(String bookName, int chapterNumber) {
        this.bookName = bookName;
        this.chapterNumber = chapterNumber;
    }

    @Override
    public MessageType getType() {
        return MessageType.CHAPTER_OWNER_REQUEST;
    }
}
