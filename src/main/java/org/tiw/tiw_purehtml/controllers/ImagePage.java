package org.tiw.tiw_purehtml.controllers;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.tiw.tiw_purehtml.beans.Comment;
import org.tiw.tiw_purehtml.beans.Image;
import org.tiw.tiw_purehtml.beans.User;
import org.tiw.tiw_purehtml.dao.CommentDAO;
import org.tiw.tiw_purehtml.dao.ImageDAO;
import org.tiw.tiw_purehtml.utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ImagePage", value = "/image")
@MultipartConfig
public class ImagePage extends HttpServlet {

    public ImagePage() {
        super();
    }

    private Connection connection;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws UnavailableException {
        connection = ConnectionHandler.getConnection(getServletContext());
        templateEngine = ConnectionHandler.getTemplateEngine(getServletContext());
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
        int imageId;
        try {
            imageId = Integer.parseInt(req.getParameter("imageId"));
        } catch (Exception e) {
            ctx.setVariable("errorMessage", "ImageId must be a number and not null");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        ImageDAO imageDAO = new ImageDAO(connection);
        Image image = null;

        try {
            image = imageDAO.getImageById(imageId);
        } catch (SQLException e) {
            ctx.setVariable("errorMessage", "Error while retrieving the image from the db");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        if (image == null) {
            ctx.setVariable("errorMessage", "Image not found");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        CommentDAO commentDAO = new CommentDAO(connection);
        List<Comment> commentList = new ArrayList<>();
        try {
            commentList = commentDAO.getCommentsByImageId(imageId);
        } catch (SQLException e) {
            ctx.setVariable("errorMessage", "Error while retrieving the comments from the db");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }
        path = "/image";
        ctx.setVariable("image", image);
        String albumIdParam = req.getParameter("albumId");
        int albumId = 0;
        if (albumIdParam != null) {
            try {
                albumId = Integer.parseInt(albumIdParam);
            } catch (NumberFormatException e){
                ctx.setVariable("errorMessage", "If present Album id must be a number");
                templateEngine.process(path, ctx, resp.getWriter());
                return;
            }
            ctx.setVariable("albumId", albumId);
        }
        ctx.setVariable("userId", loggedUser.getId());
        ctx.setVariable("commentList", commentList);
        templateEngine.process(path, ctx, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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

        int loggedUserId = loggedUser.getId();

        String imageTitle = req.getParameter("imageTitle");
        if (imageTitle == null || imageTitle.isEmpty()) {
            ctx.setVariable("errorMessage", "Image title must not be null or empty");
            templateEngine.process(path, ctx, resp.getWriter());
        }

        Part image = null;
        try {
            image = req.getPart("image");
        } catch (Exception e) {
            ctx.setVariable("errorMessage", "Image must not be null");
            templateEngine.process(path, ctx, resp.getWriter());
        }
        if (image == null) {
            ctx.setVariable("errorMessage", "Image must not be null");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }
        String contentType = image.getContentType();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/jpg")) {
            ctx.setVariable("errorMessage", "Image valid formats are only .jpeg and .jpg");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        ImageDAO imageDAO = new ImageDAO(connection);
        String res;
        try {
            res = imageDAO.createImage(loggedUserId, imageTitle, image);
        } catch (SQLException | IOException e) {
            ctx.setVariable("errorMessage", "Error while uploading the image to the server");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        int newImageId = imageDAO.getImageIdByAuthorAndTime(loggedUserId, res);
        resp.sendRedirect("./image?imageId=" + newImageId);
    }
}
