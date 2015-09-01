package ru.spbau.sergeev.btrack.client;

import ru.spbau.sergeev.btrack.common.Actor;
import ru.spbau.sergeev.btrack.common.messages.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavel
 */
public class Client extends Actor {
    private final static Logger log = Logger.getLogger(Client.class.getName());
    public static Client client;
    private final InetSocketAddress isa;
    public static int chaptersCount;
    public static Path downloadPath;
    public final BookStorage bookStorage = new BookStorage();
    private SocketChannel serverChanel;
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final ScheduledExecutorService getStatisticExecutor = Executors.newSingleThreadScheduledExecutor();
    private int PARALLEL_CHAPTERS_REQUEST = 5;
    private final Semaphore waitingChaptersCounter = new Semaphore(PARALLEL_CHAPTERS_REQUEST);
    private final ExecutorService chapterRequestFeederExecutor = Executors.newSingleThreadExecutor();
    private final ConcurrentMap<SocketChannel, ChapterRequest> pendingChaptersRequests = new ConcurrentHashMap<>();

    public static void initClient(InetSocketAddress isa, Path downloadPath) throws IOException {
        client = new Client(isa, downloadPath);
        new Thread(client).start();
    }

    public void lockBeforeServerConnecting() throws InterruptedException {
        startLatch.await();
    }

    private Client(InetSocketAddress isa, Path downloadPath) throws IOException {
        super(isa);
        log.setLevel(Level.SEVERE);
        this.isa = isa;
        Client.downloadPath = downloadPath;
    }

    @Override
    public void onStart() {
        try {
            serverChanel = initiateConnection(new InetSocketAddress("localhost", 20001)); // TODO change to ISA
            startLatch.await();
            getStatisticExecutor.scheduleAtFixedRate(this::updateStatistic, 3, 3, TimeUnit.SECONDS);
            chapterRequestFeederExecutor.submit(this::feedChaptersRequests);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception onStart", e);
        }
    }

    @Override
    public void processMessage(Message msg, SocketChannel socketChannel) {
        log.log(Level.INFO, "Client message processor");
        try {
            switch (msg.getType()) {
                case SETTINGS_RESPONSE:
                    log.log(Level.INFO, "Got SETTINGS_RESPONSE");
                    onSettingsResponse((SettingsResponse) msg, socketChannel);
                    break;
                case STATISTIC_RESPONSE:
                    log.log(Level.INFO, "Got STATISTIC_RESPONSE");
                    onStatisticResponse((StatisticResponse) msg, socketChannel);
                    break;
                case CHAPTER_OWNER_RESPONSE:
                    log.log(Level.INFO, "Got chapter owner response");
                    onChapterOwnerResponse((ChapterOwnerResponse) msg, socketChannel);
                    break;
                case CHAPTER_REQUEST:
                    log.log(Level.INFO, "Got chapter request");
                    onChapterRequest((ChapterRequest) msg, socketChannel);
                    break;
                case CHAPTER_RESPONSE:
                    log.log(Level.INFO, "Got chapter response");
                    onChapterResponse((ChapterResponse) msg, socketChannel);
                    break;
                default:
                    log.log(Level.INFO, "Wrong message type: " + msg.getType().toString());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error with message processing:", e);
            shutDown();
        }
    }

    void onStatisticResponse(StatisticResponse msg, SocketChannel socketChannel) {
        try {
            bookStorage.updateStatistic(msg);
        } catch (Exception e) {
            log.log(Level.INFO, "Error on statistic updating", e);
        }
    }

    void onChapterOwnerResponse(ChapterOwnerResponse msg, SocketChannel socketChannel) {
        try {
            final SocketChannel ownerChanel = initiateConnection(msg.owner);
            synchronized (pendingChaptersRequests) {
                pendingChaptersRequests.put(ownerChanel, new ChapterRequest(msg.bookName, msg.chapterNum));
                pendingChaptersRequests.notify();
            }
            // TODO error when connecting not finished
            // TODO treat owner not found response
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error on initiation connection to peer: " +  msg.owner.toString(), e);
        }
    }

    void onChapterRequest(ChapterRequest msg, SocketChannel socketChannel) {
        try {
            sendMessage(socketChannel, bookStorage.generateChapterResponse(msg));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error on generating chapter response", e);
        }
    }

    void onChapterResponse(ChapterResponse msg, SocketChannel socketChannel) {
        try {
            bookStorage.addChapter(msg);
            closeConnection(socketChannel);
            sendMessage(serverChanel, new AddChapter(msg.bookName, msg.chapterNumber));
            waitingChaptersCounter.release();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error on adding chapter to book storage", e);
        }
    }

    void onSettingsResponse(SettingsResponse msg, SocketChannel socketChannel) {
        System.out.println(msg.N);
        chaptersCount = msg.N;
        startLatch.countDown();
    }

    @Override
    public void onConnectingFinished(SocketChannel socketChannel) {
        try {
            if (socketChannel.equals(serverChanel)) {
                sendMessage(socketChannel, new SettingsRequest(isa));
            } else {
                synchronized (pendingChaptersRequests) {
                    ChapterRequest request = pendingChaptersRequests.get(socketChannel);
                    while (request == null) {
                        try {
                            pendingChaptersRequests.wait();
                        } catch (InterruptedException e) {
                            //
                        }
                        request = pendingChaptersRequests.get(socketChannel);
                    }
                    sendMessage(socketChannel, request);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception on connectionFinished callback", e);
        }
    }

    @Override
    public void onDisconnect(SocketChannel socketChannel) {
        try {
            log.log(Level.INFO, "Disconnected");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception on disconnect callback", e);
        }
    }

    @Override
    public void onShutdown() {
        try {
            getStatisticExecutor.shutdown();
            chapterRequestFeederExecutor.shutdownNow();
            bookStorage.close();
            log.log(Level.INFO, "Client shutdown");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error on client shutdown", e);
        }
    }

    @Override
    public void onConnectingError(SocketChannel socketChannel) {
        try {
            if (waitingChaptersCounter.availablePermits() < PARALLEL_CHAPTERS_REQUEST) {
                waitingChaptersCounter.release();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error on treating connection error", e);
        }
    }

    public void addFile(Path filePath) throws IOException {
        Book book = Book.addExistingBook(filePath);
        bookStorage.addBook(book);
        sendMessage(serverChanel, book.generateAddBookMessage());
    }

    private void updateStatistic() {
        log.log(Level.INFO, "Update statistic");
        try {
            sendMessage(serverChanel, new StatisticRequest());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error is update statistic", e);
        }
    }

    private void feedChaptersRequests() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ChapterOwnerRequest request = bookStorage.generateChapterRequest();
                if (request != null) {
                    log.log(Level.INFO, String.format("Add chapter owner request %s %d", request.bookName, request.chapterNumber));
                    waitingChaptersCounter.acquire();
                    sendMessage(serverChanel, request);
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                log.log(Level.INFO, "Interrupted exception");
                break;
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error on Chapter request", e);
            }
        }
    }
}
