package ru.spbau.sergeev.btrack.common.messages;

/**
 * @author pavel
 */
public class ChapterResponse implements Message {
    public String bookName;
    public int chapterNumber;
    public byte[] chapter;

    public ChapterResponse(String bookName, int chapterNumber, byte[] chapter) {
        this.bookName = bookName;
        this.chapterNumber = chapterNumber;
        this.chapter = chapter;
    }

    @Override
    public MessageType getType() {
        return MessageType.CHAPTER_RESPONSE;
    }
}
