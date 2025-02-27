package org.tiw.tiw_purehtml.controllers;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.tiw.tiw_purehtml.beans.Album;
import org.tiw.tiw_purehtml.beans.Image;
import org.tiw.tiw_purehtml.beans.Page;
import org.tiw.tiw_purehtml.beans.User;
import org.tiw.tiw_purehtml.dao.AlbumDAO;
import org.tiw.tiw_purehtml.utils.ConnectionHandler;
import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "Album page", value = "/album")
@MultipartConfig
public class AlbumPage extends HttpServlet {

    private Connection connection = null;
    private TemplateEngine templateEngine = null;

    public AlbumPage () {
        super();
    }

    @Override
    public void init() throws UnavailableException {
        templateEngine = ConnectionHandler.getTemplateEngine(getServletContext());
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession();
        User loggedUser = (User) s.getAttribute("user");
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());
        String path = "/errorPage";

        if (loggedUser == null) {
            ctx.setVariable("errorNotLoggedIn", "User must be logged in");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }
        int albumId;
        try {
            albumId = Integer.parseInt(req.getParameter("albumId"));
        } catch (NumberFormatException e) {
            ctx.setVariable("errorMessage", "ImageId must be a number and not null");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        AlbumDAO albumDAO = new AlbumDAO(connection);

        Album album;
        try {
            album = albumDAO.getAlbumById(albumId);
        } catch (IOException | SQLException e) {
            ctx.setVariable("errorMessage", "Error while retrieving the album");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        if(album.title == null) {
            ctx.setVariable("errorMessage", "The requested album was empty, hence it has been deleted");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        if (album == null) {
            ctx.setVariable("errorMessage", "Album does not exist or is empty");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        int numberOfImages = album.imageList.size();

        String pageParameter = req.getParameter("albumPage");
        int pageNumber = 0;
        if (pageParameter != null) {
            try {
                pageNumber = Integer.parseInt(pageParameter);
            } catch (NumberFormatException e) {
                ctx.setVariable("errorMessage", "Page number must be a number");
                templateEngine.process(path, ctx, resp.getWriter());
                return;
            }
        }
        int numberOfPages  = (int) Math.ceil( (double) numberOfImages/5.0);
        if (pageNumber >= numberOfPages) {
            ctx.setVariable("errorMessage", "The request page doesn't exist in this Album");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        path = "/album";
//        ctx.setVariable("numberOfPages", numberOfPages);
//        ctx.setVariable("albumTitle", album.title);
//        ctx.setVariable("albumId", album.id);
        List<Image> images = new ArrayList<>();
        for (int i = pageNumber*5; i < album.imageList.size() && i < pageNumber*5 + 5; i++) {
            images.add(album.imageList.get(i));
        }
        for (int i = images.size(); i < 5; i++) {
            images.add(null);
        }
//        ctx.setVariable("images", images);
//        ctx.setVariable("currentPageNumber", pageNumber);
        Page albumPage = new Page(
                album.id,album.title,album.authorId,images,numberOfPages, pageNumber);
        ctx.setVariable("albumPage", albumPage);
        templateEngine.process(path, ctx, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession();
        User loggedUser = (User) s.getAttribute("user");
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());
        String path = "/errorPage";

        if (loggedUser == null) {
            ctx.setVariable("errorNotLoggedIn", "User must be logged in");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        String albumTitle = req.getParameter("albumTitle");

        if (albumTitle == null || albumTitle.isEmpty()) {
            ctx.setVariable("errorMessage", "Album title must be not null or empty");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        // Get all the params from the request where its name contains the string "image"
        List<String> imageParams = req.getParameterMap().keySet().stream()
                .filter(key -> key.contains("image")).collect(Collectors.toList());

        List<Integer> imageIds = new ArrayList<>();
        try {
        imageIds = imageParams.stream().map(
                imageId -> Integer.parseInt(req.getParameter(imageId))).collect(Collectors.toList());
        } catch (NumberFormatException e) {
            ctx.setVariable("errorMessage", "ImageId must be a number and not null");
            templateEngine.process(path, ctx, resp.getWriter());
        }

        if (imageIds.isEmpty()){
            ctx.setVariable("errorMessage", "An album must contain at least one image");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        AlbumDAO albumDAO = new AlbumDAO(connection);
        String creationDateTime;
        try {
            creationDateTime = albumDAO.createAlbum(loggedUser.getId(), albumTitle, imageIds);
        } catch (SQLException e) {
            ctx.setVariable("errorMessage", "Error while creating the album");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        int albumId = -1;

        try {
            albumId = albumDAO.getAlbumIdByAuthorAndTime(loggedUser.getId(), creationDateTime);
            if (albumId == -1) {
                ctx.setVariable("errorMessage", "Error while getting the album");
                templateEngine.process(path, ctx, resp.getWriter());
                return;
            }
        } catch (SQLException e) {
            ctx.setVariable("errorMessage", "Error while getting the Album");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }
        resp.sendRedirect("./album?albumId="+albumId);
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
