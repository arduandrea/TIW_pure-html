package org.tiw.tiw_purehtml.controllers;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.tiw.tiw_purehtml.beans.Image;
import org.tiw.tiw_purehtml.beans.User;
import org.tiw.tiw_purehtml.dao.ImageDAO;
import org.tiw.tiw_purehtml.utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import static org.tiw.tiw_purehtml.dao.ImageDAO.getPathForImages;

@WebServlet(name = "Delete Image", value = "/delete-image")
public class DeleteImagePage extends HttpServlet {

    private Connection connection = null;
    private TemplateEngine templateEngine = null;

    public DeleteImagePage() {
        super();
    }

    @Override
    public void init() throws UnavailableException {
        connection = ConnectionHandler.getConnection(getServletContext());
        templateEngine = ConnectionHandler.getTemplateEngine(getServletContext());
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
        int imageId;
        try {
            imageId = Integer.parseInt(req.getParameter("imageId"));
        } catch (NumberFormatException e) {
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

        if (image.getAuthorId() != loggedUser.getId()) {
            ctx.setVariable("errorMessage", "You can't delete an image that is not yours");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        try {
            imageDAO.deleteImage(imageId);
        } catch (SQLException e) {
            ctx.setVariable("errorMessage", "Error while deleting the image from the db");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        // Retrieve the image file from the filesystem
        Path imagePath = Paths.get(
                getPathForImages()).resolve(
                image.getFileName()
        );
        // Delete the image file
        File imageFile = new File(imagePath.toString());
        if (!imageFile.delete()) {
            ctx.setVariable("errorMessage", "Error while deleting the image from the server");
            templateEngine.process(path, ctx, resp.getWriter());
        }
        resp.sendRedirect("./home");
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
