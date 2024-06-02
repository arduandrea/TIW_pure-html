package org.tiw.tiw_purehtml.beans;

import java.util.List;

public class Album {
    public int id;
    public String title;
    public List<Image> imageList;
    public int authorId;
    public List<String> base64ImageList;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Image> getImageList() {
        return imageList;
    }

    public int getAuthorId() {
        return authorId;
    }

    public List<String> getBase64ImageList() {
        return base64ImageList;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageList(List<Image> imageList) {
        this.imageList = imageList;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public void setBase64ImageList(List<String> base64ImageList) {
        this.base64ImageList = base64ImageList;
    }
}
