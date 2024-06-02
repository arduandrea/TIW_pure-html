package org.tiw.tiw_purehtml.controllers;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.tiw.tiw_purehtml.beans.User;
import org.tiw.tiw_purehtml.dao.CommentDAO;
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

@WebServlet(name = "CommentPage", value = "/comment")
public class CommentPage extends HttpServlet {
    public CommentPage () {
        super();
    }

    private Connection connection = null;
    private TemplateEngine templateEngine = null;


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
        } catch (Exception e) {
            ctx.setVariable("errorMessage", "Invalid image id");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        String comment = req.getParameter("commentText");
        if (comment == null || comment.isEmpty()) {
            ctx.setVariable("errorMessage", "Comment must be not null or empty");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        CommentDAO commentDao = new CommentDAO(connection);
        try {
            commentDao.addComment(loggedUser.getId(), imageId, comment);
        } catch (Exception e) {
            ctx.setVariable("errorMessage", "Error while uploading the comment on the database");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }
        resp.sendRedirect(req.getServletContext().getContextPath() + "/image?imageId=" + imageId);
    }
}
