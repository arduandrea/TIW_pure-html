package org.tiw.tiw_purehtml.dao;


import org.tiw.tiw_purehtml.beans.Comment;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    private final Connection connection;

    public CommentDAO(Connection connection) {
        this.connection = connection;
    }

    public void addComment(int userId, int imageId, String comment) throws SQLException {
        String query = "INSERT INTO Comment (author, image , comment, time) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, imageId);
        preparedStatement.setString(3, comment);
        Date date = new Date(System.currentTimeMillis());
        date.setTime(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = format.format(date);
        preparedStatement.setString(4, currentDateTime);
        preparedStatement.executeUpdate();
    }

    public List<Comment> getCommentsByImageId(int imageId) throws SQLException {
        String query = "SELECT * FROM Comment WHERE image = ? ORDER BY time DESC";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, imageId);
        ResultSet result = preparedStatement.executeQuery();
        List<Comment> comments = new ArrayList<>();
        while (result.next()) {
            Comment comment = new Comment();
            comment.setId(result.getInt("id"));
            comment.setAuthorId(result.getInt("author"));
            comment.setPictureId(result.getInt("image"));
            comment.setCommentText(result.getString("comment"));
            UserDAO userDAO = new UserDAO(connection);
            comment.setAuthorUsername(userDAO.getUsernameById(comment.getAuthorId()));
            String dateString = result.getString("time");
            long dateLong = Timestamp.valueOf(dateString).getTime();
            Date date = new Date(dateLong);
            date.setTime(dateLong);
            comment.setCommentDate(date);
            comments.add(comment);
        }
        return comments;
    }

}
