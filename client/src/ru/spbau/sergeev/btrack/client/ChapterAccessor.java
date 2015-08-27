package ru.spbau.sergeev.btrack.client;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

import static java.nio.file.Files.newByteChannel;

/**
 * @author pavel
 */
public class ChapterAccessor {
    private int chaptersCount;
    private Path workingDirectory;

    public ChapterAccessor(int chaptersCount, Path workingDirectory) throws IOException {
        this.chaptersCount = chaptersCount;
        this.workingDirectory = workingDirectory;
    }

    private Path getPathByName(String filename) {
        return workingDirectory.resolve(filename);
    }

    public byte[] readChapter(String filename, int num) throws IOException {
        assert num > 0 && num < chaptersCount;
        try (SeekableByteChannel file = newByteChannel(getPathByName(filename))) {
            long fileSize = file.size();
            long chapterSize = roundUp(fileSize, chaptersCount);
            file.position(chapterSize * num);
            long forRead = Long.min(chapterSize, fileSize - chapterSize * num);
            assert forRead <= Integer.MAX_VALUE;
            ByteBuffer buffer = ByteBuffer.allocate((int) forRead);
            file.read(buffer);
            return buffer.array();
        }
    }

    public void writeChapter(String filename, int num, byte[] chapter) throws IOException {
        assert num > 0 && num < chaptersCount;
        try (SeekableByteChannel file = newByteChannel(getPathByName(filename))) {
            long fileSize = file.size();
            long chapterSize = roundUp(fileSize, chaptersCount);
            file.position(chapterSize * num);
            file.write(ByteBuffer.wrap(chapter));
        }
    }

    public void createFileWithSize(String filename, long size) throws IOException {
        new RandomAccessFile(getPathByName(filename).toString(), "rw").setLength(size);
    }

    public static long roundUp(long num, long divisor) {
        return (num + divisor - 1) / divisor;
    }
}
