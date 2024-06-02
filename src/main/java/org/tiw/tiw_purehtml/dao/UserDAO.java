package org.tiw.tiw_purehtml.dao;

import org.tiw.tiw_purehtml.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public User checkCredentials(String username, String password) throws SQLException {
        String query = "SELECT * FROM User WHERE username = ? AND password = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    return null;
                else {
                    result.next();
                    User user = new User();
                    user.setUsername(result.getString("username"));
                    user.setEmail(result.getString("email"));
                    user.setPassword(result.getString("password"));
                    user.setId(result.getInt("id"));
                    return user;
                }
            }
            catch (SQLException e) {
                throw new SQLException("Error while retrieving user", e);
            }
        }
        catch (SQLException e) {
            throw new SQLException("Error while retrieving user", e);
        }
    }

    public boolean isUserPresent(String username) throws SQLException {
        String query = "SELECT username FROM User WHERE username = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, username);
        ResultSet result = preparedStatement.executeQuery();
        return result.isBeforeFirst();
    }

    public User createUser(String username, String password, String email) throws SQLException {
        String query = "INSERT INTO User (username, password, email) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        preparedStatement.setString(3, email);
        preparedStatement.executeUpdate();
        return checkCredentials(username, password);
    }

    public String getUsernameById(int id) throws SQLException {
        String query = "SELECT username FROM User WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, id);
        ResultSet result = preparedStatement.executeQuery();
        if (!result.isBeforeFirst()) {
            return null;
        }
        result.next();
        return result.getString("username");
    }
}
