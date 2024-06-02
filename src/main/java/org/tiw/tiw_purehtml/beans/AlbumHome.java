package org.tiw.tiw_purehtml.beans;

public class AlbumHome {
    public int id;
    public String title;
    public String authorUsername;
    public String creationDate;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
