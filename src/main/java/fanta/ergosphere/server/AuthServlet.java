package fanta.ergosphere.server;

import fanta.ergosphere.util.Credentials;
import fanta.ergosphere.util.General;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String in = request.getReader().readLine();
        if(General.isBadReq(LOGGER, request, response, in)) return;
        final JSONObject json = new JSONObject(in);
        final String u = json.optString("username");
        final String p = json.optString("password");
        if(Credentials.isCorrect(u, p)) {
            response.getWriter().println("{\"status\":\"OK\"}");
            General.serverInfo(LOGGER, request, "Correct credentials");
        }else {
            response.getWriter().println("{\"status\":\"Incorrect credentials\"}");
            General.serverWarn(LOGGER, request, "Incorrect credentials");
        }
    }
}