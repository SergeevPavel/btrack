package ru.spbau.sergeev.btrack.server;

import ru.spbau.sergeev.btrack.common.messages.ChapterOwnerRequest;
import ru.spbau.sergeev.btrack.common.messages.ChapterOwnerResponse;
import ru.spbau.sergeev.btrack.common.messages.StatisticResponse;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author pavel
 */
public class Index {
    private final Set<InetSocketAddress> activePeers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ConcurrentMap<String, BookIndex> index = new ConcurrentHashMap<>();
    private final Random rnd = new Random();

    static public class BookIndex {
        public CopyOnWriteArrayList<ChapterIndex> chapters;
        public long size;

        public BookIndex(long size) {
            ChapterIndex[] temp = new ChapterIndex[Server.CHAPTERS_COUNT];
            for (int i = 0; i < Server.CHAPTERS_COUNT; i++) {
                temp[i] = new ChapterIndex();
            }
            chapters = new CopyOnWriteArrayList<>(temp);
            this.size = size;
        }

    }
    static public class ChapterIndex {
        public Set<InetSocketAddress> owners = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    void peerIsActivated(InetSocketAddress peer) {
        activePeers.add(peer);
    }

    void peerIsDeactivated(InetSocketAddress peer) {
        activePeers.remove(peer);
    }

    public void addOwner(String filename, long size, InetSocketAddress isa) {
        index.putIfAbsent(filename, new BookIndex(size));
        final BookIndex bindex = index.get(filename);
        for (ChapterIndex cindex: bindex.chapters) {
            cindex.owners.add(isa);
        }
    }

    public ChapterOwnerResponse generateChapterOwnerResponse(ChapterOwnerRequest request) {
        final BookIndex bindex = index.get(request.bookName);
        final Set<InetSocketAddress> owners = bindex.chapters.get(request.chapterNumber).owners;
        final Set<InetSocketAddress> activeOwners = new HashSet<>(owners);
        activeOwners.retainAll(activePeers);
        int item = rnd.nextInt(activeOwners.size());
        int i = 0;
        InetSocketAddress owner = null;
        for(InetSocketAddress obj : activeOwners)
        {
            if (i == item) {
                owner = obj;
                break;
            }
            i = i + 1;
        }
        assert owner != null; // TODO chapter is not available
        return new ChapterOwnerResponse(owner, request.bookName, request.chapterNumber);
    }

    public StatisticResponse generateStatistic() {
        StatisticResponse response = new StatisticResponse();
        int booksCount = index.keySet().size();
        response.bookStatistics = new StatisticResponse.BookStatistic[booksCount];
        int i = 0;
        for (Map.Entry<String, BookIndex> entry: index.entrySet()) {
            response.bookStatistics[i] = new StatisticResponse.BookStatistic();
            response.bookStatistics[i].name = entry.getKey();
            response.bookStatistics[i].size = entry.getValue().size;
            response.bookStatistics[i].partIsAvailable = new boolean[Server.CHAPTERS_COUNT];
            for (int j = 0; j < Server.CHAPTERS_COUNT; j ++) {
                final Set<InetSocketAddress> owners = entry.getValue().chapters.get(j).owners;
                response.bookStatistics[i].partIsAvailable[j] = !Collections.disjoint(owners, activePeers);
            }
            i++;
        }
        return response;
    }
}
