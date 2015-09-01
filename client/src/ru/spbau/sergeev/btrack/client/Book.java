package ru.spbau.sergeev.btrack.client;

import ru.spbau.sergeev.btrack.common.messages.AddBook;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;


/**
 * @author pavel
 */
public class Book implements Closeable {
    private static Logger log = Logger.getLogger(Client.class.getName());
    private final Path path;
    private boolean[] isChapterPresent = new boolean[Client.chaptersCount];
    private boolean[] isChapterAvailable = new boolean[Client.chaptersCount];
    private boolean[] isChapterRequested = new boolean[Client.chaptersCount];
    private int presentedChaptersCount;
    private int availableChaptersCount;
    private final SeekableByteChannel file;
    private long fileSize;
    private boolean isSeeding;
    private Random rnd = new Random();

    final boolean[] getIsChapterPresent() {
        return isChapterPresent;
    }

    final boolean[] getIsChapterAvailable() {
        return isChapterAvailable;
    }

    public String getBookName() {
        return path.getFileName().toString();
    }

    public static Book createNewBook(String name, long size) throws IOException {
        return new Book(name, size);
    }

    public static Book addExistingBook(Path path) throws IOException {
        return new Book(path);
    }

    public AddBook generateAddBookMessage() throws IOException {
        return new AddBook(path.getFileName().toString(), file.size());
    }

    private Book(String name, long size) throws IOException {
        this.path = Client.downloadPath.resolve(name);
        presentedChaptersCount = 0;
        availableChaptersCount = 0;
        Arrays.fill(isChapterPresent, false);
        Arrays.fill(isChapterRequested, false);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        file = Files.newByteChannel(path, EnumSet.of(READ, WRITE));
        fileSize = size;
        isSeeding = true;
    }

    private Book(Path path) throws IOException {
        this.path = path;
        presentedChaptersCount = Client.chaptersCount;
        availableChaptersCount = Client.chaptersCount;
        Arrays.fill(isChapterPresent, true);
        Arrays.fill(isChapterRequested, false);
        file = Files.newByteChannel(path, EnumSet.of(READ, WRITE));
        fileSize = file.size();
        isSeeding = true;
    }

    synchronized public byte[] readChapter(int num) throws IOException {
        assert num > 0 && num < Client.chaptersCount;
        long chapterSize = roundUp(fileSize, Client.chaptersCount);
        file.position(chapterSize * num);
        long forRead = Long.min(chapterSize, Long.max(0, fileSize - chapterSize * num));
        assert forRead <= Integer.MAX_VALUE;
        ByteBuffer buffer = ByteBuffer.allocate((int) forRead);
        file.read(buffer);
        return buffer.array();
    }

    synchronized public void writeChapter(int num, byte[] chapter) throws IOException {
        assert num > 0 && num < Client.chaptersCount;
        if (!isChapterPresent[num]) {
            presentedChaptersCount += 1;
            isChapterPresent[num] = true;
            long chapterSize = roundUp(fileSize, Client.chaptersCount);
            file.position(Long.min(chapterSize * num, fileSize));
            final ByteBuffer buffer = ByteBuffer.wrap(chapter);
            int write = file.write(buffer);
            assert write == chapter.length;
        } else {
            log.log(Level.SEVERE, String.format("Multiple writing of chapter: %d", num));
        }
    }

    public double getCompletedPercent() {
        return (double)presentedChaptersCount / Client.chaptersCount * 100;
    }

    public double getAvailablePercent() {
        return (double)availableChaptersCount / Client.chaptersCount * 100;
    }

    public void setChapterAvailability(boolean[] isChapterAvailable) {
        int newChaptersAvailableCount = 0;
        for (int i = 0; i < Client.chaptersCount; i++) {
            this.isChapterAvailable[i] = isChapterAvailable[i];
            if (isChapterAvailable[i]) {
                newChaptersAvailableCount += 1;
            }
        }
        availableChaptersCount = newChaptersAvailableCount;
    }

    public static long roundUp(long num, long divisor) {
        return (num + divisor - 1) / divisor;
    }

    public boolean isBookCompleted() {
        return presentedChaptersCount == Client.chaptersCount;
    }

    public boolean isAvailableNew() {
        return presentedChaptersCount < availableChaptersCount;
    }

    public int requestedChapter() {
        List<Integer> chapters = new ArrayList<>();
        for (int i = 0; i < Client.chaptersCount; i++) {
            if (!isChapterPresent[i] && isChapterAvailable[i] && !isChapterRequested[i]) {
                chapters.add(i);
            }
        }
        if (chapters.isEmpty()) {
            return -1;
        } else {
            int num = rnd.nextInt(chapters.size());
            isChapterRequested[chapters.get(num)] = true;
            return chapters.get(num);
        }
    }

    public void chapterRequestRejected(int num) {
        isChapterRequested[num] = false;
    }

    public boolean isSeeding() {
        return isSeeding;
    }

    public void stopSeeding() {
        isSeeding = false;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
