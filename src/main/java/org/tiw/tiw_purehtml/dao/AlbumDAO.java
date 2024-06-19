package org.tiw.tiw_purehtml.dao;


import org.tiw.tiw_purehtml.beans.Album;
import org.tiw.tiw_purehtml.beans.AlbumHome;
import org.tiw.tiw_purehtml.beans.Comment;
import org.tiw.tiw_purehtml.beans.Image;
import org.tiw.tiw_purehtml.utils.GenericUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.tiw.tiw_purehtml.dao.ImageDAO.getPathForImages;

public class AlbumDAO {
    private final Connection connection;

    public AlbumDAO(Connection connection) {
        this.connection = connection;
    }

    public String createAlbum(int authorId, String albumTitle, List<Integer> imagesIdList) throws SQLException {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = format.format(date);
        String query = "INSERT INTO Album (title, author, creation_date) VALUES (?, ?, ?)";
        int result;
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, albumTitle);
        preparedStatement.setInt(2, authorId);
        preparedStatement.setString(3, currentDateTime);
        result = preparedStatement.executeUpdate();


        if (result == -1) {
            throw new RuntimeException("Error in creating album");
        }

        int albumId = getAlbumIdByAuthorAndTime(authorId, currentDateTime);

        if (albumId == -1) {
            throw new RuntimeException("Error in creating album");
        }

        query = "INSERT INTO AlbumImage (album, image) VALUES (?, ?)";
        preparedStatement = connection.prepareStatement(query);
        for (int imageId : imagesIdList) {
            preparedStatement.setInt(1, albumId);
            preparedStatement.setInt(2, imageId);
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        return currentDateTime;

    }


    public int getAlbumIdByAuthorAndTime(int author, String dateTime) throws SQLException {
        String query = "SELECT id FROM Album WHERE author = ? AND creation_date = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, author);
        preparedStatement.setString(2, dateTime);
        ResultSet result = preparedStatement.executeQuery();
        if (!result.isBeforeFirst()) // no results, album not found
            return -1;
        else {
            result.next();
            Album album = new Album();
            return result.getInt("id");
        }
    }

    public Album getAlbumById(int albumId) throws IOException, SQLException {

        if (!albumExists(albumId)) {
            return new Album();
        }

        String query = "SELECT al.title, " +
                "al.author, " +
                "GROUP_CONCAT(DISTINCT im.creation_date ORDER BY im.creation_date DESC) as creation_dates," +
                "GROUP_CONCAT(DISTINCT im.id ORDER BY im.creation_date DESC) as ids," +
                "GROUP_CONCAT(im.title ORDER BY im.creation_date DESC) as titles, " +
                "GROUP_CONCAT(im.fileName ORDER BY im.creation_date DESC) as file_names " +
                "FROM AlbumImage as ai " +
                "JOIN Album as al ON ai.album = al.id " +
                "JOIN Image as im ON ai.image = im.id " +
                "WHERE al.id = ? GROUP BY al.title and al.author;";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, albumId);
        ResultSet result = preparedStatement.executeQuery();
        if (!result.isBeforeFirst()) {
            return null;
        }
        result.next();
        Album album = new Album();
        album.setId(albumId);
        album.setTitle(result.getString("title"));
        album.setAuthorId(result.getInt("author"));
        List<Image> imageList = new ArrayList<>();
        List<List<Comment>> imagesCommentList = new ArrayList<>();
        List<String> base64ImageList = new ArrayList<>();
        String[] imagesFileNameList = result.getString("file_names").split(",");
        String[] imagesTitleList = result.getString("titles").split(",");
        String[] imagesIdList = result.getString("ids").split(",");


        if (imagesTitleList.length == 0) {
            // Album is Empty
            return null;
        }

        CommentDAO commentDAO = new CommentDAO(connection);

        for (int i = 0; i < imagesFileNameList.length; i++) {
            Image image = new Image();
            image.setId(Integer.parseInt(imagesIdList[i]));
            image.setAuthorId(result.getInt("author"));
            image.setTitle(imagesTitleList[i]);

            // Retrieve the image file from the filesystem
            Path imagePath = Paths.get(
                    getPathForImages()).resolve(
                    imagesFileNameList[i]
            );
            File imageFile = new File(imagePath.toString());
            image.setBase64Image(GenericUtils.encodeImageToBase64(imageFile));
            image.setCommentList(commentDAO.getCommentsByImageId(image.getId()));
            imageList.add(image);
        }

        album.setImageList(imageList);
        return album;
    }

    protected boolean albumExists(int albumId) throws SQLException {
        String query = "SELECT al.id as albumId, " +
                "COUNT(DISTINCT ai.image) as image_count " +
                "FROM Album as al " +
                "JOIN AlbumImage as ai on ai.album = al.id " +
                "WHERE al.id = ? ";
        PreparedStatement preparedStatement1 = connection.prepareStatement(query);
        preparedStatement1.setInt(1, albumId);
        ResultSet checkAlbum = preparedStatement1.executeQuery();
        if (!checkAlbum.isBeforeFirst()) {
            // Album does not exist
            return false;
        }

        checkAlbum.next();

        if (checkAlbum.getInt("image_count") == 0) {
            // The Album is empty
            query = "DELETE FROM Album WHERE id = ?";
            PreparedStatement preparedStatement2 = connection.prepareStatement(query);
            preparedStatement2.setInt(1, albumId);
            preparedStatement2.executeUpdate();
            return false;
        }

        return true;
    }

    public List<AlbumHome> getAlbumForHome(int userId, boolean isUser) throws SQLException {
        String query;
        if (isUser) {
            query = "SELECT al.id, al.title, al.author, al.creation_date FROM Album as al WHERE al.author = ? ORDER BY al.creation_date DESC;";
        } else {
            query = "SELECT al.id, al.title, al.author, al.creation_date FROM Album as al WHERE al.author != ? ORDER BY al.creation_date DESC;";
        }

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, userId);
        ResultSet result = preparedStatement.executeQuery();
        if (!result.isBeforeFirst()) {
            return new ArrayList<>();
        } else {
            List<AlbumHome> albumList = new ArrayList<>();
            while (result.next()) {
                AlbumHome album = new AlbumHome();
                album.setId(result.getInt("id"));
                album.setTitle(result.getString("title"));
                UserDAO userDAO = new UserDAO(connection);
                album.setAuthorUsername(userDAO.getUsernameById(result.getInt("author")));
                album.setCreationDate(result.getString("creation_date"));
                albumList.add(album);
            }
            return albumList;
        }
    }
}

