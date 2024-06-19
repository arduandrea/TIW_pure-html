package org.tiw.tiw_purehtml.dao;

import org.tiw.tiw_purehtml.beans.Image;
import org.tiw.tiw_purehtml.utils.GenericUtils;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImageDAO {
    private final Connection connection;

    public ImageDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Image> getImagesByAuthorId(int authorId) throws SQLException, IOException {
        String query = "SELECT * FROM Image WHERE author = ? ORDER BY creation_date DESC";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, authorId);
        ResultSet result = preparedStatement.executeQuery();
        List<Image> images = new ArrayList<>();
        while (result.next()) {
            images.add(getImageFromQueryResult(result));
        }
        return images;
    }

    public Image getImageById(int id) throws SQLException, IOException {
        String query = "SELECT * FROM Image WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, id);
        ResultSet result = preparedStatement.executeQuery();
        if (!result.isBeforeFirst()) // no results, image not found
            return null;
        else {
            result.next();
            return getImageFromQueryResult(result);
        }
    }

    public String createImage(int authorID, String title, Part image) throws SQLException, IOException {
        String fileName = GenericUtils.generateFileName();

        Path imagePath = Paths.get(
                getPathForImages()).resolve(
                fileName);
        Files.copy(image.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

        String currentDateTime = GenericUtils.getCurrentSQLDateTime();

        String query = "INSERT INTO Image (author, title, creation_date, fileName) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, authorID);
        preparedStatement.setString(2, title);
        preparedStatement.setString(3, currentDateTime);
        preparedStatement.setString(4, fileName);
        int affectedRows;
        affectedRows = preparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error while uploading the image to the DB");
        }
        return fileName;
    }

    public int getImageIdByFileName(String fileName) throws SQLException {
        String query = "SELECT id FROM Image WHERE fileName = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, fileName);
        ResultSet result = preparedStatement.executeQuery();
        if (!result.isBeforeFirst()){  // no results, image not found
            return -1;
        }
        result.next();
        return result.getInt("id");
    }

    public void deleteImage(int id) throws SQLException {
        String query = "DELETE FROM Image WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    public static String getPathForImages() {
        return System.getProperty("user.dir")+"/galleryManagerResources/images";
    }

    public static Image getImageFromQueryResult(ResultSet result) throws SQLException, IOException {
        Image image = new Image();
        image.setId(result.getInt("id"));
        image.setAuthorId(result.getInt("author"));
        image.setTitle(result.getString("title"));
        image.setFileName(result.getString("fileName"));
        String dateString = result.getString("creation_date");
        image.setCreationDate(GenericUtils.getDateTimeFromSQLDateTime(dateString));
        // Retrieve the image file from the filesystem
        Path imagePath = Paths.get(
                getPathForImages()).resolve(
                image.getFileName()
        );


        File imageFile = new File(imagePath.toString());
        image.setBase64Image(GenericUtils.encodeImageToBase64(imageFile));
        return image;
    }
}
