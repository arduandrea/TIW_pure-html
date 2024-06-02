package org.tiw.tiw_purehtml.controllers;

import org.apache.commons.lang3.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.tiw.tiw_purehtml.beans.User;
import org.tiw.tiw_purehtml.dao.UserDAO;
import org.tiw.tiw_purehtml.utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "Login page", value = "/login")
public class LoginPage extends HttpServlet {

    public LoginPage() {
        super();
    }

    private TemplateEngine templateEngine;
    private Connection connection;

    @Override
    public void init() throws UnavailableException {
        connection = ConnectionHandler.getConnection(getServletContext());
        templateEngine = ConnectionHandler.getTemplateEngine(getServletContext());
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDAO userDao = new UserDAO(connection);

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());

        User user = null;
        String path = "/index";


        String username;
        String password;
        username = StringEscapeUtils.escapeJava(req.getParameter("loginUsername"));
        password = StringEscapeUtils.escapeJava(req.getParameter("loginPassword"));

        if (username == null || username.isEmpty()) {
            ctx.setVariable("errorMessage", "Username must be not null");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }
        if (password == null || password.isEmpty()) {
            ctx.setVariable("errorMessage", "Password must be not null");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        try {
            user = userDao.checkCredentials(username, password);
        } catch (SQLException e) {
            ctx.setVariable("errorMessage", "Error while checking credentials");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        if (user == null) {
            ctx.setVariable("errorMessage", "Incorrect username or password");
            templateEngine.process(path, ctx, resp.getWriter());
        } else {
            req.getSession().setAttribute("user", user);
            ctx.setVariable("errorMessage", "");
            resp.sendRedirect("./home");
        }
    }


    @Override
    public void destroy() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
