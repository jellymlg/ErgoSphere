package fanta.ergosphere.server;

import fanta.ergosphere.main.Info;
import fanta.ergosphere.main.Manager;
import fanta.ergosphere.process.ProcessMonitor;
import fanta.ergosphere.util.Credentials;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Base64;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String url = "web" + request.getRequestURI().replace(request.getServletPath(), "");
        if(FileServlet.class.getClassLoader().getResource(url) == null) {
            response.setStatus(404);
            return;
        }
        if(url.equals("web/")) url += "index.html";

        if(!Manager.isInitialized() && url.contains("index.html")) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", "/init");
            return;
        }

        if(url.contains("index.html")) {
            if(request.getHeader("Authorization") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader(HttpHeader.WWW_AUTHENTICATE.asString(), "Basic realm=\"Log in to ErgoSphere.\"");
                return;
            }else {
                final String[] data = new String(Base64.getDecoder().decode(request.getHeader("Authorization").split(" ")[1]), UTF_8).split(":");
                if(!Credentials.isCorrect(data[0], data[1])) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        }

        LOGGER.info(request.getHeader("Host") + " GET " + url + " [" + request.getHeader("User-Agent") + "]");

        if(request.getParameter("key") != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("{\"key\":\"" + ProcessMonitor.INSTANCE.getAPIkey() + "\"}");
            return;
        }

        InputStream resource = InitServlet.class.getClassLoader().getResourceAsStream(url);
        if(url.endsWith("svg")) response.setContentType("image/svg+xml");
        response.getOutputStream().write(resource.readAllBytes());
    }
}