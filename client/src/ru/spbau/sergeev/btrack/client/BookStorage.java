package ru.spbau.sergeev.btrack.client;

import ru.spbau.sergeev.btrack.common.messages.ChapterOwnerRequest;
import ru.spbau.sergeev.btrack.common.messages.ChapterRequest;
import ru.spbau.sergeev.btrack.common.messages.ChapterResponse;
import ru.spbau.sergeev.btrack.common.messages.StatisticResponse;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author pavel
 */
public class BookStorage implements Closeable {
    private List<Book> books = new CopyOnWriteArrayList<>();
    private Random rnd = new Random();

    public void addBook(Book book) {
        books.add(book);
    }

    public int size() {
        return books.size();
    }

    public Book get(int index) {
        return books.get(index);
    }

    public synchronized void updateStatistic(StatisticResponse statistic) throws IOException {
        for(int i = 0; i < statistic.bookStatistics.length; i++) {
            boolean isFound = false;
            StatisticResponse.BookStatistic bookStatistic = statistic.bookStatistics[i];
            for (Book book: books) {
                if (Objects.equals(book.getBookName(), bookStatistic.name)) {
                    isFound = true;
                    book.setChapterAvailability(bookStatistic.partIsAvailable);
                }
            }
            if (!isFound) {
                books.add(Book.createNewBook(bookStatistic.name, bookStatistic.size));
            }
        }
    }

    public void addChapter(ChapterResponse msg) throws IOException {
        for (Book book: books) {
            if (book.getBookName().equals(msg.bookName)) {
                book.writeChapter(msg.chapterNumber, msg.chapter);
            }
        }
        assert false;
    }

    public boolean[] getIsPresent(int index) {
        return books.get(index).getIsChapterPresent();
    }

    public boolean[] getIsAvailable(int index) {
        return books.get(index).getIsChapterAvailable();
    }

    synchronized public ChapterOwnerRequest generateChapterRequest() {
        final List<Book> notCompetedBooks = books.stream().filter(b -> !b.isBookCompleted()).collect(Collectors.toList());
        if (!notCompetedBooks.isEmpty()) {
            int index = rnd.nextInt(notCompetedBooks.size());
            final Book book = notCompetedBooks.get(index);
            int num = book.requestedChapter();
            if (num == -1) {
                return null;
            }
            return new ChapterOwnerRequest(book.getBookName(), num);
        } else {
            return null;
        }
    }

    public ChapterResponse generateChapterResponse(ChapterRequest msg) throws IOException {
        for (Book book: books) {
            if (book.getBookName().equals(msg.bookName)) {
                byte[] chapter = book.readChapter(msg.chapterNum);
                return new ChapterResponse(msg.bookName, msg.chapterNum, chapter);
            }
        }
        assert false;
        return null;
    }

    @Override
    public void close() throws IOException {
        for (Book book: books) {
            book.close();
        }
    }
}
