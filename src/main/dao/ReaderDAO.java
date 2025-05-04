package main.dao;

import main.connection.DatabaseConnection;
import main.domain.Book;
import main.domain.Reader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReaderDAO implements BaseDAO<Reader, Integer> {

    @Override
    public Optional<Reader> findById(Integer id) {
        String selectReaderById = "SELECT * FROM readers WHERE id = ?";
        Reader reader = null;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectReaderById)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                reader = new Reader();
                reader.setId(resultSet.getInt(UtilDAO.ID));
                reader.setFirstName(resultSet.getString(UtilDAO.FIRST_NAME));
                reader.setLastName(resultSet.getString(UtilDAO.LAST_NAME));
                reader.setEmail(resultSet.getString(UtilDAO.EMAIL));

                reader.setBooks(findBooksForReader(id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(reader);
    }

    @Override
    public List<Reader> findAll() {
        List<Reader> readers = new ArrayList<>();
        String selectAllReaders = "SELECT * FROM readers";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectAllReaders)) {

            while (resultSet.next()) {
                Reader reader = new Reader();
                reader.setId(resultSet.getInt(UtilDAO.ID));
                reader.setFirstName(resultSet.getString(UtilDAO.FIRST_NAME));
                reader.setLastName(resultSet.getString(UtilDAO.LAST_NAME));
                reader.setEmail(resultSet.getString(UtilDAO.EMAIL));

                reader.setBooks(findBooksForReader(reader.getId()));
                readers.add(reader);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return readers;
    }

    @Override
    public Reader save(Reader reader) {
        String insertReader = "INSERT INTO readers (first_name, last_name, email) VALUES (?, ?, ?)";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(insertReader, Statement.RETURN_GENERATED_KEYS);
            connection.setAutoCommit(false);

            statement.setString(1, reader.getFirstName());
            statement.setString(2, reader.getLastName());
            statement.setString(3, reader.getEmail());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                reader.setId(generatedKeys.getInt(1));
            }

            if (reader.getBooks() != null && !reader.getBooks().isEmpty()) {
                saveBooksForReader(reader.getId(), reader.getBooks());
            }
            connection.commit();
        } catch (SQLException e) {
            UtilDAO.rollback(connection);
            e.printStackTrace();
        } finally {
            UtilDAO.closeConnectionAndStatement(connection, statement);
        }
        return reader;
    }

    @Override
    public void update(Reader reader) {
        String updateReader = "UPDATE readers SET first_name = ?, last_name = ?, email = ? WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(updateReader);
            connection.setAutoCommit(false);

            statement.setString(1, reader.getFirstName());
            statement.setString(2, reader.getLastName());
            statement.setString(3, reader.getEmail());
            statement.setInt(4, reader.getId());
            statement.executeUpdate();

            updateBooksForReader(reader.getId(), reader.getBooks());
        } catch (SQLException e) {
            UtilDAO.rollback(connection);
            e.printStackTrace();
        } finally {
            UtilDAO.closeConnectionAndStatement(connection, statement);
        }
    }

    @Override
    public void deleteById(Integer id) {
        deleteBooksForReader(id);

        String deleteReaderById = "DELETE FROM readers WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(deleteReaderById, Statement.RETURN_GENERATED_KEYS);
            connection.setAutoCommit(false);

            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            UtilDAO.rollback(connection);
            e.printStackTrace();
        } finally {
            UtilDAO.closeConnectionAndStatement(connection, statement);
        }
    }

    private List<Book> findBooksForReader(Integer readerId) {
        List<Book> books = new ArrayList<>();
        String selectAllReadersBook = "SELECT b.* FROM books b " +
                "JOIN readers_books rb ON b.id = rb.book_id " +
                "WHERE rb.reader_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectAllReadersBook)) {

            statement.setInt(1, readerId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Book book = new Book();
                book.setId(resultSet.getInt(UtilDAO.ID));
                book.setTitle(resultSet.getString(UtilDAO.TITLE));
                book.setAuthor_id(resultSet.getInt(UtilDAO.AUTHOR_ID));
                book.setPublishedYear(resultSet.getInt(UtilDAO.PUBLISHED_YEAR));
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    private void saveBooksForReader(Integer readerId, List<Book> books) {
        String insertAllReadersBook = "INSERT INTO readers_books (reader_id, book_id) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertAllReadersBook)) {

            for (Book book : books) {
                statement.setInt(1, readerId);
                statement.setInt(2, book.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateBooksForReader(Integer readerId, List<Book> books) {
        deleteBooksForReader(readerId);

        if (books != null && !books.isEmpty()) {
            saveBooksForReader(readerId, books);
        }
    }

    private void deleteBooksForReader(Integer readerId) {
        String deleteAllBooksByReaderId = "DELETE FROM readers_books WHERE reader_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteAllBooksByReaderId)) {

            statement.setInt(1, readerId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}