package ru.spbau.sergeev.btrack.common.messages;

import java.net.InetSocketAddress;

/**
 * @author pavel
 */
public class ChapterOwnerResponse implements Message {
    public InetSocketAddress owner;
    public String bookName;
    public int chapterNum;

    public ChapterOwnerResponse(InetSocketAddress owner, String bookName, int chapterNum) {
        this.owner = owner;
        this.bookName = bookName;
        this.chapterNum = chapterNum;
    }

    @Override
    public MessageType getType() {
        return MessageType.CHAPTER_OWNER_RESPONSE;
    }
}
