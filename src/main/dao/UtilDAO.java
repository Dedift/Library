package main.dao;

import main.connection.DatabaseConnection;
import main.domain.Author;
import main.domain.Book;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class UtilDAO {

    public static final String ID = "id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String TITLE = "title";
    public static final String AUTHOR_ID = "author_id";
    public static final String PUBLISHED_YEAR = "published_year";
    public static final String EMAIL = "email";

    private UtilDAO() {
    }

    public static void saveBookWithAuthor(Book book, Author author) {

        AuthorDAO authorDao = new AuthorDAO();
        BookDAO bookDao = new BookDAO();
        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            Author savedAuthor = authorDao.save(author);
            book.setAuthor_id(savedAuthor.getId());

            bookDao.save(book);

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void closeConnectionAndStatement(Connection connection, Statement statement) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
