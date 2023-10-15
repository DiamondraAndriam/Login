package test;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;

import session.*;

public class TestServlet extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PgHttpSession pg = new PgHttpSession(request.getSession());
        //pg.setAttribute("connection", true);
        PrintWriter out = response.getWriter();
        out.print(pg.getId());
        /*RequestDispatcher dispat = request.getRequestDispatcher("/pages/Bienvenu.jsp");
        dispat.forward(request, response);*/
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
