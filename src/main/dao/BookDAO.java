package main.dao;

import main.connection.DatabaseConnection;
import main.domain.Book;
import main.domain.Reader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDAO implements BaseDAO<Book, Integer> {

    @Override
    public Optional<Book> findById(Integer id) {
        String selectBookById = "SELECT * FROM books WHERE id = ?";
        Book book = null;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectBookById)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                book = new Book();
                book.setId(resultSet.getInt(UtilDAO.ID));
                book.setTitle(resultSet.getString(UtilDAO.TITLE));
                book.setAuthor_id(resultSet.getInt(UtilDAO.AUTHOR_ID));
                book.setPublishedYear(resultSet.getInt(UtilDAO.PUBLISHED_YEAR));

                book.setReaders(findReadersForBook(id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        String selectAllBooks = "SELECT * FROM books";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectAllBooks)) {

            while (resultSet.next()) {
                Book book = new Book();
                book.setId(resultSet.getInt(UtilDAO.ID));
                book.setTitle(resultSet.getString(UtilDAO.TITLE));
                book.setAuthor_id(resultSet.getInt(UtilDAO.AUTHOR_ID));
                book.setPublishedYear(resultSet.getInt(UtilDAO.PUBLISHED_YEAR));

                book.setReaders(findReadersForBook(book.getId()));
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    @Override
    public Book save(Book book) {
        String insertBook = "INSERT INTO books (title, author_id, published_year) VALUES (?, ?, ?)";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(insertBook, Statement.RETURN_GENERATED_KEYS);
            connection.setAutoCommit(false);

            statement.setString(1, book.getTitle());
            statement.setInt(2, book.getAuthor_id());
            statement.setInt(3, book.getPublishedYear());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                book.setId(generatedKeys.getInt(1));
            }

            if (book.getReaders() != null && !book.getReaders().isEmpty()) {
                saveReadersForBook(book.getId(), book.getReaders());
            }
            connection.commit();
        } catch (SQLException e) {
            UtilDAO.rollback(connection);
            e.printStackTrace();
        } finally {
            UtilDAO.closeConnectionAndStatement(connection, statement);
        }
        return book;
    }

    @Override
    public void update(Book book) {
        String updateBook = "UPDATE books SET title = ?, author_id = ?, published_year = ? WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(updateBook);
            connection.setAutoCommit(false);

            statement.setString(1, book.getTitle());
            statement.setInt(2, book.getAuthor_id());
            statement.setInt(3, book.getPublishedYear());
            statement.setInt(4, book.getId());
            statement.executeUpdate();

            updateReadersForBook(book.getId(), book.getReaders());
            connection.commit();

        } catch (SQLException e) {
            UtilDAO.rollback(connection);
            e.printStackTrace();
        } finally {
            UtilDAO.closeConnectionAndStatement(connection, statement);
        }
    }

    @Override
    public void deleteById(Integer id) {

        String deleteBookById = "DELETE FROM books WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(deleteBookById);
            connection.setAutoCommit(false);

            deleteReadersForBook(id);

            statement.setInt(1, id);
            statement.executeUpdate();
            connection.commit();

        } catch (SQLException e) {
            UtilDAO.rollback(connection);
            e.printStackTrace();
        } finally {
            UtilDAO.closeConnectionAndStatement(connection, statement);
        }
    }

    public List<Book> findByTitleOrAuthor(String searchTerm) {
        List<Book> books = new ArrayList<>();
        String SelectBookByTitleOrAuthor = "SELECT b.* FROM books b " +
                "LEFT JOIN authors a ON b.author_id = a.id " +
                "WHERE b.title LIKE ? OR a.first_name LIKE ? OR a.last_name LIKE ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SelectBookByTitleOrAuthor)) {

            String likePattern = "%" + searchTerm + "%";
            statement.setString(1, likePattern);
            statement.setString(2, likePattern);
            statement.setString(3, likePattern);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Book book = new Book();
                book.setId(resultSet.getInt(UtilDAO.ID));
                book.setTitle(resultSet.getString(UtilDAO.TITLE));
                book.setAuthor_id(resultSet.getInt(UtilDAO.AUTHOR_ID));
                book.setPublishedYear(resultSet.getInt(UtilDAO.PUBLISHED_YEAR));
                book.setReaders(findReadersForBook(book.getId()));
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    private List<Reader> findReadersForBook(Integer bookId) {
        List<Reader> readers = new ArrayList<>();
        String selectBooksReader = "SELECT r.* FROM readers r " +
                "JOIN readers_books rb ON r.id = rb.reader_id " +
                "WHERE rb.book_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectBooksReader)) {

            statement.setInt(1, bookId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Reader reader = new Reader();
                reader.setId(resultSet.getInt(UtilDAO.ID));
                reader.setFirstName(resultSet.getString(UtilDAO.FIRST_NAME));
                reader.setLastName(resultSet.getString(UtilDAO.LAST_NAME));
                reader.setEmail(resultSet.getString(UtilDAO.EMAIL));
                readers.add(reader);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return readers;
    }

    private void saveReadersForBook(Integer bookId, List<Reader> readers) {
        String insertReaderAndBook = "INSERT INTO readers_books (reader_id, book_id) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertReaderAndBook)) {

            for (Reader reader : readers) {
                statement.setInt(1, reader.getId());
                statement.setInt(2, bookId);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateReadersForBook(Integer bookId, List<Reader> readers) {
        deleteReadersForBook(bookId);

        if (readers != null && !readers.isEmpty()) {
            saveReadersForBook(bookId, readers);
        }
    }

    private void deleteReadersForBook(Integer bookId) {
        String sql = "DELETE FROM readers_books WHERE book_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}