package main;

import main.dao.AuthorDAO;
import main.dao.BookDAO;
import main.dao.ReaderDAO;
import main.dao.UtilDAO;
import main.domain.Author;
import main.domain.Book;
import main.domain.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        AuthorDAO authorDao = new AuthorDAO();
        BookDAO bookDao = new BookDAO();
        ReaderDAO readerDao = new ReaderDAO();

        Author author = new Author();
        author.setFirstName("George");
        author.setLastName("Orwell");
        authorDao.save(author);
        logger.debug("Saved author: " + author);

        Book book = new Book();
        book.setTitle("1984");
        book.setAuthor_id(author.getId());
        book.setPublishedYear(1949);
        bookDao.save(book);
        logger.debug("Saved book: " + book);

        Reader reader1 = new Reader();
        reader1.setFirstName("John");
        reader1.setLastName("Doe");
        reader1.setEmail("john.doe@example.com");
        readerDao.save(reader1);

        Reader reader2 = new Reader();
        reader2.setFirstName("Jane");
        reader2.setLastName("Smith");
        reader2.setEmail("jane.smith@example.com");
        readerDao.save(reader2);
        logger.debug("Saved readers: " + reader1 + " and " + reader2);

        List<Book> readersBooks = new ArrayList<>();
        readersBooks.add(book);
        reader1.setBooks(readersBooks);
        readerDao.update(reader1);
        logger.debug("Added book to reader: " + reader1);

        Optional<Book> foundBook = bookDao.findById(book.getId());
        foundBook.ifPresent(b -> {
            logger.debug("Readers of book '" + b.getTitle() + "': " + b.getReaders());
        });

        List<Reader> bookReaders = new ArrayList<>();
        bookReaders.add(reader1);
        bookReaders.add(reader2);
        book.setReaders(bookReaders);
        bookDao.update(book);
        logger.debug("Added readers to book: " + book.getReaders());

        Optional<Reader> foundReader = readerDao.findById(reader1.getId());
        foundReader.ifPresent(r -> {
            logger.debug("Books of reader '" + r.getFirstName() + "': " + r.getBooks());
        });

        Author newAuthor = new Author();
        newAuthor.setFirstName("J.R.R.");
        newAuthor.setLastName("Tolkien");

        Book newBook = new Book();
        newBook.setTitle("The Lord of the Rings");
        newBook.setPublishedYear(1954);
        UtilDAO.saveBookWithAuthor(newBook, newAuthor);
        logger.debug("Saved book with author: " + newBook);

        newBook.setReaders(List.of(reader1));
        bookDao.update(newBook);
        logger.debug("Added new book to existing reader");

        List<Book> allBooks = bookDao.findAll();
        logger.debug("All books with readers:");
        allBooks.forEach(b -> {
            logger.debug("- " + b.getTitle() + " (read by: " +
                    b.getReaders().stream().map(Reader::getFirstName).collect(Collectors.joining(", ")) + ")");
        });

        List<Reader> allReaders = readerDao.findAll();
        logger.debug("All readers with books:");
        allReaders.forEach(r -> {
            logger.debug("- " + r.getFirstName() + " " + r.getLastName() + " (books: " +
                    r.getBooks().stream().map(Book::getTitle).collect(Collectors.joining(", ")) + ")");
        });

        reader1.setBooks(new ArrayList<>()); // Очищаем список книг читателя
        readerDao.update(reader1);
        logger.debug("Removed all books from reader: " + reader1.getFirstName());

        bookDao.deleteById(book.getId());
        logger.debug("Deleted book: " + book.getTitle());

        Optional<Book> deletedBook = bookDao.findById(book.getId());
        logger.debug("Book after deletion: " + (deletedBook.isPresent() ? "exists" : "not found"));
    }
}