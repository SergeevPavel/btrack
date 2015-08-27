package ru.spbau.sergeev.btrack.client;

import ru.spbau.sergeev.btrack.common.Actor;
import ru.spbau.sergeev.btrack.common.messages.Message;
import ru.spbau.sergeev.btrack.common.messages.SettingsRequest;
import ru.spbau.sergeev.btrack.common.messages.SettingsResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavel
 */
public class Client extends Actor {
    private static Logger log = Logger.getLogger(Client.class.getName());
    private final InetSocketAddress isa;
    private int chaptersCount;
    private CountDownLatch startLatch = new CountDownLatch(1);
    private Path workingDirectory;
    private ChapterAccessor chapterAccessor;

    public Client(InetSocketAddress isa) throws IOException {
        super(isa);
        this.isa = isa;
    }

    @Override
    public void onStart() {
        try {
            initiateConnection(new InetSocketAddress("localhost", 20001));
            startLatch.await();
            initWorkingDirectory();
            addFile(Paths.get("sample.pdf"));
            chapterAccessor = new ChapterAccessor(chaptersCount, workingDirectory);
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
                default:
                    log.log(Level.INFO, "Wrong message type");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error with message processing:", e);
            shutDown();
        }
    }

    void onSettingsResponse(SettingsResponse msg, SocketChannel socketChannel) {
        System.out.println(msg.N);
        chaptersCount = msg.N;
        closeConnection(socketChannel);
        startLatch.countDown();
        shutDown();
    }

    @Override
    public void onConnectingFinished(SocketChannel socketChannel) {
        try {
            sendMessage(socketChannel, new SettingsRequest(isa));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception on connectionFinished callback", e);
        }
    }

    @Override
    public void onDisconnect(SocketChannel socketChannel) {
        try {

        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception on disconnect callback", e);
        }
    }

    public void addFile(Path filePath) throws IOException {
        final Path filename = filePath.getFileName();
        final Path dest = workingDirectory.resolve(filename);
        Files.copy(filePath, dest);
        // TODO register file
    }

    private void initWorkingDirectory() throws IOException {
        final Date date = new Date();
        final Timestamp timestamp = new Timestamp(date.getTime());
        final Path path = Paths.get("WD" + timestamp.toString());
        Files.createDirectory(path);
        workingDirectory = path;
        log.log(Level.INFO, "New working directory created: " + path.toString());
    }
}
