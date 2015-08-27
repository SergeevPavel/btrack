package ru.spbau.sergeev.btrack.common;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author pavel
 */
public class Index {
    private List<InetSocketAddress> activePeers;
    private ConcurrentMap<String, List<List<InetSocketAddress>>> peers;

    public Index() {
        activePeers = new ArrayList<>();
        peers = new ConcurrentHashMap<>();
    }

    void peerIsActivated(InetSocketAddress peer) {

    }

    void peerIsDeactivated(InetSocketAddress peer) {

    }

    public void addFile() {

    }


}
