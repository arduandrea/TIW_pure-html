package org.tiw.tiw_purehtml.controllers;

import org.thymeleaf.TemplateEngine;
import org.tiw.tiw_purehtml.utils.ConnectionHandler;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;

@WebServlet(name = "Logout page", value = "/logout")
public class LogoutPage extends HttpServlet {

    public LogoutPage() {
        super();
    }

    @Override
    public void init() throws UnavailableException {
        Connection connection = ConnectionHandler.getConnection(getServletContext());
        TemplateEngine templateEngine = ConnectionHandler.getTemplateEngine(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            httpSession.invalidate();
        }
        String path = getServletContext().getContextPath() + "/index.html";
        req.getSession().removeAttribute("user");
        resp.sendRedirect(path);
    }
}
