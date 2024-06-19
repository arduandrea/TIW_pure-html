package org.tiw.tiw_purehtml.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Base64;

public class GenericUtils {
    public static String generateFileName(){
        String md5Hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(String.valueOf((int) (Math.random() * 10000)));
        return md5Hash.substring(0, 21)+".jpg";
    }

    public static String getCurrentSQLDateTime(){
        Date date = new Date(System.currentTimeMillis());
        date.setTime(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
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

    public static Date getDateTimeFromSQLDateTime(String dateTime){
        long dateLong = java.sql.Timestamp.valueOf(dateTime).getTime();
        Date date = new Date(dateLong);
        date.setTime(dateLong);
        return date;
    }
}
