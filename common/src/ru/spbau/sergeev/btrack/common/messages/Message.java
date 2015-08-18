package ru.spbau.sergeev.btrack.common.messages;

import java.io.Serializable;

/**
 * @author pavel
 */
public interface Message extends Serializable {
    MessageType getType();
}
