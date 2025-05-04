package main.dao;

import main.connection.DatabaseConnection;
import main.domain.Author;
import main.domain.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthorDAO implements BaseDAO<Author, Integer> {

    @Override
    public Optional<Author> findById(Integer id) {
        String selectAuthorById = "SELECT * FROM authors WHERE id = ?";
        Author author = null;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectAuthorById)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                author = new Author();
                author.setId(resultSet.getInt(UtilDAO.ID));
                author.setFirstName(resultSet.getString(UtilDAO.FIRST_NAME));
                author.setLastName(resultSet.getString(UtilDAO.LAST_NAME));

                author.setBooks(findBooksForAuthor(id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(author);
    }

    @Override
    public List<Author> findAll() {
        List<Author> authors = new ArrayList<>();
        String selectAllAuthors = "SELECT * FROM authors";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectAllAuthors)) {

            while (resultSet.next()) {
                Author author = new Author();
                author.setId(resultSet.getInt(UtilDAO.ID));
                author.setFirstName(resultSet.getString(UtilDAO.FIRST_NAME));
                author.setLastName(resultSet.getString(UtilDAO.LAST_NAME));

                author.setBooks(findBooksForAuthor(author.getId()));
                authors.add(author);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return authors;
    }

    @Override
    public Author save(Author author) {
        String insertAuthor = "INSERT INTO authors (first_name, last_name) VALUES (?, ?)";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(insertAuthor, Statement.RETURN_GENERATED_KEYS);
            connection.setAutoCommit(false);

            statement.setString(1, author.getFirstName());
            statement.setString(2, author.getLastName());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                author.setId(generatedKeys.getInt(1));
            }

            if (author.getBooks() != null && !author.getBooks().isEmpty()) {
                saveBooksForAuthor(author.getId(), author.getBooks());
            }
            connection.commit();
        } catch (SQLException e) {
            UtilDAO.rollback(connection);
            e.printStackTrace();
        } finally {
            UtilDAO.closeConnectionAndStatement(connection, statement);
        }

        return author;
    }

    @Override
    public void update(Author author) {
        String updateAuthor = "UPDATE authors SET first_name = ?, last_name = ? WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(updateAuthor);
            connection.setAutoCommit(false);

            statement.setString(1, author.getFirstName());
            statement.setString(2, author.getLastName());
            statement.setInt(3, author.getId());
            statement.executeUpdate();

            updateBooksForAuthor(author.getId(), author.getBooks());
        } catch (SQLException e) {
            UtilDAO.rollback(connection);
            e.printStackTrace();
        } finally {
            UtilDAO.closeConnectionAndStatement(connection, statement);
        }
    }

    @Override
    public void deleteById(Integer id) {
        deleteBooksForAuthor(id);

        String deleteAuthorById = "DELETE FROM authors WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(deleteAuthorById);
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

    private List<Book> findBooksForAuthor(Integer authorId) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE author_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, authorId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Book book = new Book();
                book.setId(resultSet.getInt(UtilDAO.ID));
                book.setTitle(resultSet.getString(UtilDAO.TITLE));
                book.setAuthor_id(authorId);
                book.setPublishedYear(resultSet.getInt(UtilDAO.PUBLISHED_YEAR));
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    private void saveBooksForAuthor(Integer authorId, List<Book> books) {
        BookDAO bookDao = new BookDAO();
        for (Book book : books) {
            book.setAuthor_id(authorId);
            bookDao.save(book);
        }
    }

    private void updateBooksForAuthor(Integer authorId, List<Book> books) {
        deleteBooksForAuthor(authorId);

        if (books != null && !books.isEmpty()) {
            saveBooksForAuthor(authorId, books);
        }
    }

    private void deleteBooksForAuthor(Integer authorId) {
        String sql = "DELETE FROM books WHERE author_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, authorId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}