package ru.spbau.sergeev.btrack.common.messages;

import java.io.Serializable;

/**
 * @author pavel
 */
public class StatisticResponse implements Message {
    public static class BookStatistic implements Serializable {
        public String name;
        public long size;
        public boolean[] partIsAvailable;
    }

    public BookStatistic[] bookStatistics;

    @Override
    public MessageType getType() {
        return MessageType.STATISTIC_RESPONSE;
    }
}
