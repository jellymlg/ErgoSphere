package fanta.ergosphere.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JettyServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    private Server server;
    
    public JettyServer() {}

    public void start(String address, int port) throws Exception {
        int maxThreads = 20;
        int minThreads = 5;
        int idleTimeout = 120;
        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

        server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setHost(address);
        connector.setPort(port);
        server.setConnectors(new Connector[] {connector});

        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);

        servletHandler.addServletWithMapping(StatusServlet.class, "/status");
        servletHandler.addServletWithMapping(InitServlet.class, "/init");
        servletHandler.addServletWithMapping(AuthServlet.class, "/auth");
        servletHandler.addServletWithMapping(DoServlet.class, "/do");
        servletHandler.addServletWithMapping(ShutdownServlet.class, "/shutdown");
        servletHandler.addServletWithMapping(FileServlet.class, "/*");

        server.start();
    }

    public void stop() {
        try {
            if(server != null) server.stop();
        }catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}