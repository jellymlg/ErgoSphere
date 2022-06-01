package fanta.ergosphere.server;

import fanta.ergosphere.main.Manager;
import fanta.ergosphere.process.AppState;
import fanta.ergosphere.process.ProcessMonitor;
import fanta.ergosphere.util.General;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!Manager.isInitialized()) {
            response.setStatus(404);
            return;
        }

        final String in = request.getReader().readLine();
        if(General.isBadReq(LOGGER, request, response, in)) return;

        final JSONObject data = new JSONObject(in);
        final String app = data.getString("service");
        switch(data.getString("exec")) {
            case "install": ProcessMonitor.INSTANCE.doService(AppState.INSTALL, app); break;
            case "start"  : ProcessMonitor.INSTANCE.doService(AppState.START  , app); break;
            case "stop"   : ProcessMonitor.INSTANCE.doService(AppState.STOP   , app); break;
        }
    }
}