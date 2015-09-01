package ru.spbau.sergeev.btrack.server;

import ru.spbau.sergeev.btrack.common.Actor;
import ru.spbau.sergeev.btrack.common.messages.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavel
 */
public class Server extends Actor {
    private static Logger log = Logger.getLogger(Server.class.getName());
    public static int CHAPTERS_COUNT = 1000;
    private static ConcurrentMap<SocketChannel, ClientState> activeClients = new ConcurrentHashMap<>();
    private final Index index = new Index();

    public Server(InetSocketAddress isa) throws IOException {
        super(isa);
        log.setLevel(Level.SEVERE);
    }

    @Override
    public void onStart() {
    }

    void onSettingsRequest(SettingsRequest msg, SocketChannel socketChannel) {
        try {
            activeClients.putIfAbsent(socketChannel, new ClientState());
            final ClientState clientState = activeClients.get(socketChannel);
            clientState.isa = msg.isa;
            index.peerIsActivated(msg.isa);
            sendMessage(socketChannel, new SettingsResponse(CHAPTERS_COUNT));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error on Settings response", e);
        }
    }

    void onAddBook(AddBook msg, SocketChannel socketChannel) {
        log.log(Level.INFO, String.format("Add book: %s %d", msg.bookName, msg.size));
        index.addOwner(msg.bookName, msg.size, activeClients.get(socketChannel).isa);
    }

    void onAddChapter(AddChapter msg, SocketChannel socketChannel) {
        log.log(Level.INFO, String.format("Add chapter: %s %d", msg.bookName, msg.chapterNum));
        index.addChapterOwner(msg.bookName, msg.chapterNum, activeClients.get(socketChannel).isa);
    }

    void onStatisticRequest(StatisticRequest msg, SocketChannel socketChannel) {
        try {
            sendMessage(socketChannel, index.generateStatistic());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error on Statistic response", e);
        }
    }

    @Override
    public void processMessage(Message msg, SocketChannel socketChannel) {
        log.log(Level.INFO, "Server message processor");
        try {
            switch (msg.getType()) {
                case SETTINGS_REQUEST:
                    log.log(Level.INFO, "Got SETTINGS_REQUEST");
                    onSettingsRequest((SettingsRequest) msg, socketChannel);
                    break;
                case ADD_BOOK:
                    log.log(Level.INFO, "Got ADD_BOOK");
                    onAddBook((AddBook) msg, socketChannel);
                    break;
                case ADD_CHAPTER:
                    log.log(Level.INFO, "Got ADD_CHAPTER");
                    onAddChapter((AddChapter) msg, socketChannel);
                    break;
                case STATISTIC_REQUEST:
                    log.log(Level.INFO, "Got STATISTIC_REQUEST");
                    onStatisticRequest((StatisticRequest) msg, socketChannel);
                    break;
                case CHAPTER_OWNER_REQUEST:
                    log.log(Level.INFO, "Got CHAPTER_OWNER_REQUEST");
                    onChapterOwnerRequest((ChapterOwnerRequest) msg, socketChannel);
                    break;
                case STOP_SEEDING:
                    log.log(Level.INFO, "Got STOP_SEEDING");
                    onStopSeeding((StopSeeding) msg, socketChannel);
                    break;
                default:
                    log.log(Level.INFO, "Wrong message type: " + msg.getType().toString());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error with message processing:", e);
            shutDown();
        }
    }

    public void onChapterOwnerRequest(ChapterOwnerRequest msg, SocketChannel socketChannel) {
        try {
            sendMessage(socketChannel, index.generateChapterOwnerResponse(msg));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error on generating chapter owner response", e);
        }
    }

    public void onStopSeeding(StopSeeding msg, SocketChannel socketChannel) {
        final InetSocketAddress owner = activeClients.get(socketChannel).isa;
        index.stopSeeding(msg.bookName, owner);
    }

    @Override
    public void onConnectingFinished(SocketChannel socketChannel) {
        log.log(Level.INFO, "On connected");
    }

    @Override
    public void onDisconnect(SocketChannel socketChannel) {
        log.log(Level.INFO, "Disconnected client");
        try {
            index.peerIsDeactivated(activeClients.get(socketChannel).isa);
            activeClients.remove(socketChannel);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error on disconnecting", e);
        }
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void onConnectingError(SocketChannel socketChannel) {

    }
}
