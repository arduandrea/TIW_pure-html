package org.tiw.tiw_purehtml.controllers;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.tiw.tiw_purehtml.beans.AlbumHome;
import org.tiw.tiw_purehtml.beans.Image;
import org.tiw.tiw_purehtml.beans.User;
import org.tiw.tiw_purehtml.dao.AlbumDAO;
import org.tiw.tiw_purehtml.dao.ImageDAO;
import org.tiw.tiw_purehtml.utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
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

@WebServlet(name = "Home", value = "/home")
public class HomePage extends HttpServlet {

    public HomePage() {
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
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession();
        User loggedUser = (User) s.getAttribute("user");
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());
        String path = "/home.html";

        if (loggedUser == null) {
            ctx.setVariable("errorNotLoggedIn", "User must be logged in");
            path = "/index.html";
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        AlbumDAO albumDAO = new AlbumDAO(connection);

        List<AlbumHome> userAlbums = new ArrayList<>();
        List<AlbumHome> sharedAlbums = new ArrayList<>();

        try {
            userAlbums = albumDAO.getAlbumForHome(loggedUser.getId(), true);
            sharedAlbums = albumDAO.getAlbumForHome(loggedUser.getId(), false);

        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception when retrieving albums");
            return;
        }
        ctx.setVariable("userAlbums", userAlbums);
        ctx.setVariable("sharedAlbums", sharedAlbums);

        ImageDAO imageDAO = new ImageDAO(connection);

        List<Image> userImageList = new ArrayList<>();

        try {
            userImageList = imageDAO.getImagesByAuthorId(loggedUser.getId());
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception while retrieving user images");
        }
        ctx.setVariable("userImageList", userImageList);
        templateEngine.process(path, ctx, resp.getWriter());
    }
}
