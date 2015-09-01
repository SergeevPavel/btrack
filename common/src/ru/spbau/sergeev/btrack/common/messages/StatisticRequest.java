package ru.spbau.sergeev.btrack.common.messages;

/**
 * @author pavel
 */
public class StatisticRequest implements Message {
    @Override
    public MessageType getType() {
        return MessageType.STATISTIC_REQUEST;
    }
}
