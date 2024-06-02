package org.tiw.tiw_purehtml.controllers;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.tiw.tiw_purehtml.beans.User;
import org.tiw.tiw_purehtml.dao.UserDAO;
import org.tiw.tiw_purehtml.utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "Signup", value ="/signup")
public class SignupPage extends HttpServlet {

    public SignupPage() {
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username;
        String password;
        String confirmPassword;
        String email;

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());
        String path = "/signup";

        username = StringEscapeUtils.escapeJava(req.getParameter("username"));

        if (username == null || username.isEmpty()) {
            ctx.setVariable("errorMessage", "Username must be not null");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        email = StringEscapeUtils.escapeJava(req.getParameter("email"));

        if (email == null || email.isEmpty()) {
            ctx.setVariable("errorMessage", "Email must be not null");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        //Check if the email is a valid email

        if (!EmailValidator.getInstance()
                .isValid(email)) {
            ctx.setVariable("errorMessage", "Email must be a valid email");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }


        password = StringEscapeUtils.escapeJava(req.getParameter("password"));

        if (password == null || password.isEmpty()) {
            ctx.setVariable("errorMessage", "Password must be not null");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        confirmPassword = StringEscapeUtils.escapeJava(req.getParameter("confirmPassword"));

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            ctx.setVariable("errorMessage", "Confirm password must be not null");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        if (!password.equals(confirmPassword)) {
            ctx.setVariable("errorMessage", "Password and confirm password must match");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        UserDAO userDao = new UserDAO(connection);

        try {
            if (userDao.isUserPresent(username)) {
                ctx.setVariable("errorMessage", "Username already in use");
                templateEngine.process(path, ctx, resp.getWriter());
                return;
            }
        } catch (SQLException e) {
            ctx.setVariable("errorMessage", "Internal server error, retry later");
            templateEngine.process(path, ctx, resp.getWriter());
            return;
        }

        User user = null;
        try {
            user = userDao.createUser(username, password, email);
        } catch (SQLException e) {
            ctx.setVariable("errorMessage", "Internal server error, retry later");
            templateEngine.process(path, ctx, resp.getWriter());
        } finally {
            req.getSession().setAttribute("user", user);
            resp.sendRedirect("./home");
        }
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
