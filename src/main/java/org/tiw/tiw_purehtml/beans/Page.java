package org.tiw.tiw_purehtml.beans;

import java.util.List;

public class Page {
    public int id;
    public String title;
    public int authorId;
    public List<Image> imageList;
    public int numberOfPages;
    public int currentPage;


    public Page(int id, String title, int authorId, List<Image> imageList, int numberOfPages, int currentPage) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.imageList = imageList;
        this.numberOfPages = numberOfPages;
        this.currentPage = currentPage;
    }
}
