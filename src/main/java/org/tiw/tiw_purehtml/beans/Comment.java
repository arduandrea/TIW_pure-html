package org.tiw.tiw_purehtml.beans;

import java.sql.Date;

public class Comment {
    public int id;
    public int authorId;
    public String commentText;
    public Date commentDate;
    public int pictureId;
    public String authorUsername;

    public int getId() {
        return id;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getCommentText() {
        return commentText;
    }

    public Date getCommentDate() {
        return commentDate;
    }

    public int getPictureId() {
        return pictureId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public void setCommentDate(Date commentDate) {
        this.commentDate = commentDate;
    }

    public void setPictureId(int pictureId) {
        this.pictureId = pictureId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }
}
