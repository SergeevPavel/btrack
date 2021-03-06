package ru.spbau.sergeev.btrack.common.messages;

/**
 * @author pavel
 */
public class ChapterRequest implements Message {
    public String bookName;
    public int chapterNum;

    public ChapterRequest(String bookName, int chapterNum) {
        this.bookName = bookName;
        this.chapterNum = chapterNum;
    }

    @Override
    public MessageType getType() {
        return MessageType.CHAPTER_REQUEST;
    }
}
