package ru.spbau.sergeev.btrack.common.messages;

/**
 * @author pavel
 */
public class AddChapter implements Message {
    public String bookName;
    public int chapterNum;

    public AddChapter(String bookName, int chapterNum) {
        this.bookName = bookName;
        this.chapterNum = chapterNum;
    }

    @Override
    public MessageType getType() {
        return MessageType.ADD_CHAPTER;
    }
}
