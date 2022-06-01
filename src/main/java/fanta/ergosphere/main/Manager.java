package fanta.ergosphere.main;

import fanta.ergosphere.server.JettyServer;
import fanta.ergosphere.process.ProcessMonitor;
import fanta.ergosphere.util.SwayDB;
import swaydb.java.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

public final class Manager {

    public static final Logger LOGGER = LoggerFactory.getLogger(Manager.class);

    private static final JettyServer SERVER = new JettyServer();

    private static final MultiMap<String, String, String, Void> STORAGE = SwayDB.getTable(LOGGER.getName());

    public static void main(String[] args) {

        if(Info.getAvailMem_MB() <= 500) {
            LOGGER.error("ErgoSphere cannot run with less than 500 MiB of free memory.");
            System.exit(0);
        }
        if(!Info.isAdmin()) {
            LOGGER.error("Run ErgoSphere with administrator rights.");
            System.exit(0);
        }
        if(Info.isUnknown()) {
            LOGGER.error("Unknown OS. Only Windows, Mac and Linux distributions are supported.");
            System.exit(0);
        }

        Dependencies.init();
        Parameters.init(args);
        LOGGER.info("Starting with arguments: " + Arrays.toString(args));

        try {
            SERVER.start(Parameters.getAddress(), Parameters.getPort());
            if(isInitialized()) ProcessMonitor.INSTANCE.start();
        }catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void shutdown() {
        SERVER.stop();
        ProcessMonitor.INSTANCE.shutdown();
        SwayDB.shutdown();
        LOGGER.info("Shutting down");
        System.exit(0);
    }

    public static boolean isInitialized() {
        return STORAGE.get("INIT").isPresent();
    }
}