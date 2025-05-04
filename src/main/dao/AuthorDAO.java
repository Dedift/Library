package main.dao;

import main.connection.DatabaseConnection;
import main.domain.Author;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthorDAO implements BaseDAO<Author, Integer> {



    @Override
    public Optional<Author> findById(Integer id) {
        String selectAuthorById = "SELECT * FROM authors WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectAuthorById)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Author author = new Author();
                author.setId(resultSet.getInt(UtilDAO.ID));
                author.setFirstName(resultSet.getString(UtilDAO.FIRST_NAME));
                author.setLastName(resultSet.getString(UtilDAO.LAST_NAME));
                return Optional.of(author);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
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

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertAuthor, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, author.getFirstName());
            statement.setString(2, author.getLastName());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                author.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return author;
    }

    @Override
    public void update(Author author) {
        String updateAuthor = "UPDATE authors SET first_name = ?, last_name = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateAuthor)) {

            statement.setString(1, author.getFirstName());
            statement.setString(2, author.getLastName());
            statement.setInt(3, author.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteById(Integer id) {
        String deleteAuthorById = "DELETE FROM authors WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteAuthorById)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}