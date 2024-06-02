package org.tiw.tiw_purehtml.dao;

import org.tiw.tiw_purehtml.beans.Image;

import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ImageDAO {
    private final Connection connection;

    public ImageDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Image> getImagesByAuthorId(int authorId) throws SQLException, IOException {
        String query = "SELECT * FROM Image WHERE author = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, authorId);
        ResultSet result = preparedStatement.executeQuery();
        List<Image> images = new ArrayList<>();
        while (result.next()) {
            Image image = new Image();
            image.setId(result.getInt("id"));
            image.setAuthorId(result.getInt("author"));
            image.setTitle(result.getString("title"));
            String dateString = result.getString("creation_date");
            long dateLong = Timestamp.valueOf(dateString).getTime();
            Date date = new Date(dateLong);
            date.setTime(dateLong);
            image.setCreationDate(date);
            // Retrieve the image file from the filesystem
            Path imagePath = Paths.get(
                    findCorrectPathFromResources("")).resolve(
                    image.getFileName()
            );
            File imageFile = new File(imagePath.toString());
            image.setBase64Image(encodeImageToBase64(imageFile));
            images.add(image);
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
            Image image = new Image();
            image.setId(result.getInt("id"));
            image.setAuthorId(result.getInt("author"));
            image.setTitle(result.getString("title"));
            String dateString = result.getString("creation_date");
            long dateLong = Timestamp.valueOf(dateString).getTime();
            Date date = new Date(dateLong);
            date.setTime(dateLong);
            image.setCreationDate(date);
            // Retrieve the image file from the filesystem
            Path imagePath = Paths.get(
                    findCorrectPathFromResources("")).resolve(
                    image.getFileName()
            );
            File imageFile = new File(imagePath.toString());
            image.setBase64Image(encodeImageToBase64(imageFile));
            return image;
        }
    }

    public String createImage(int authorID, String title, Part image) throws SQLException, IOException {

        String query = "INSERT INTO Image (author, title, creation_date) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, authorID);
        preparedStatement.setString(2, title);
        Date date = new Date(System.currentTimeMillis());
        date.setTime(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = format.format(date);
        preparedStatement.setString(3, currentDateTime);
        int affectedRows;
        affectedRows = preparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error while uploading the image to the DB");
        }
        Path imagePath = Paths.get(
                findCorrectPathFromResources("")).resolve(
                imageName(title, authorID, String.valueOf(Timestamp.valueOf(currentDateTime).getTime())));
        Files.copy(image.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
        return currentDateTime;
    }

    public int getImageIdByAuthorAndTime(int authorId, String dateTime) {
        String query = "SELECT id FROM Image WHERE author = ? AND creation_date = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, authorId);
            preparedStatement.setString(2, dateTime);
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, image not found
                    return -1;
                else {
                    result.next();
                    return result.getInt("id");
                }
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    public void deleteImage(int id) throws SQLException {
        String query = "DELETE FROM Image WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error while deleting the image from the DB", e);
        }
    }

    public static String findCorrectPathFromResources(String pathFromRes) {
        return "/Users/andreaardu/IdeaProjects/TIW_Project_pure_html/images";
    }

    public static String imageName(String imageTitle, int authorID, String timestamp) {
        return imageTitle + "_" + authorID + "_" + timestamp + ".jpg";
    }

    public static String encodeImageToBase64(File imageFile) throws IOException {
        try (FileInputStream imageInFile = new FileInputStream(imageFile);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = imageInFile.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }
}
