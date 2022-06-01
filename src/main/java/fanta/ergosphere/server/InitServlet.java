package fanta.ergosphere.server;

import fanta.ergosphere.main.Info;
import fanta.ergosphere.main.Manager;
import fanta.ergosphere.process.ProcessMonitor;
import fanta.ergosphere.util.Credentials;
import fanta.ergosphere.util.General;
import fanta.ergosphere.util.SwayDB;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InitServlet extends HttpServlet {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(InitServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if(Manager.isInitialized()) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", "/");
            return;
        }

        if(request.getParameter("mem") != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("{\"memory\":\"" + Info.getAvailMem_MB() + "\"}");
            return;
        }

        String url = "web" + request.getRequestURI().replace(request.getServletPath(), "");
        if(FileServlet.class.getClassLoader().getResource(url) == null) {
            response.setStatus(404);
            return;
        }
        if(url.equals("web")) url += "/init.html";

        LOGGER.info(request.getHeader("Host") + " GET " + url + " [" + request.getHeader("User-Agent") + "]");

        InputStream resource = InitServlet.class.getClassLoader().getResourceAsStream(url);
        response.getOutputStream().write(resource.readAllBytes());
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if(Manager.isInitialized()) {
            response.setStatus(404);
            return;
        }

        final String in = request.getReader().readLine();
        if(General.isBadReq(LOGGER, request, response, in)) return;

        final JSONObject data = new JSONObject(in);
        if(data.getInt("memory") < Info.getAvailMem_MB() && Credentials.storeCredentials(data.getString("username"), data.getString("password"))) {
            SwayDB.getTable(Manager.LOGGER.getName()).put("INIT", "OK");
            ProcessMonitor.INSTANCE.initDefaults(data.getInt("memory"), data.getString("username"));
            response.getWriter().println("{\"status\":\"OK\"}");
        }else response.getWriter().println("{\"status\":\"Error\"}");
    }
}