package org.tiw.tiw_purehtml.beans;

import org.tiw.tiw_purehtml.dao.ImageDAO;

import java.sql.Date;
import java.util.List;

public class Image {

    public int id;
    public int authorId;
    public String title;
    public Date creationDate;
    public String base64Image;
    public String fileName;
    public List<Comment> commentList;

    public int getId() {
        return id;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getTitle() {
        return title;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getFileName() {
        return this.fileName;
    }

    public List<Comment> getComments(){
        return this.commentList;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setBase64Image(String image) {
        this.base64Image ="data:image/png;base64,"+ image;
    }

    public void setCommentList(List<Comment> commentList){
        this.commentList = commentList;
    }
}
