package fanta.ergosphere.main;

import fanta.ergosphere.server.JettyServer;
import fanta.ergosphere.process.ProcessMonitor;
import fanta.ergosphere.util.General;
import fanta.ergosphere.util.SwayDB;
import fanta.ergosphere.util.SwayDB.DB;
import fanta.ergosphere.util.SwayDB.Table;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;

public final class Manager {

    public static final Logger LOGGER = LoggerFactory.getLogger(Manager.class);

    private static final JettyServer SERVER = new JettyServer();

    private static String ADDRESS = General.getLocalIP(), STORAGE_DIR = "data/";
    private static int PORT = 80;

    private static final DB STORAGE = SwayDB.getTable(Table.MANAGER);

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
        ArgumentParser parser = ArgumentParsers.newFor("ErgoSphere").build()
            .defaultHelp(true)
            .description("A personal Ergo ecosystem.");
        parser.addArgument("-a", "--address").setDefault(ADDRESS).help("IP address to bind to.");
        parser.addArgument("-p", "--port").setDefault(PORT).type(Integer.class).help("Port to bind to.");
        parser.addArgument("-s", "--storage").setDefault(STORAGE_DIR).help("Directory to store Ergo apps in.");
        LOGGER.info("Starting with arguments: " + Arrays.toString(args));

        try {
            Namespace parsedArgs = parser.parseArgs(args);
            ADDRESS = parsedArgs.getString("address");
            PORT = parsedArgs.getInt("port");
            STORAGE_DIR = parsedArgs.getString("storage");

            InetAddress addr = InetAddress.getByName(ADDRESS);
            if(!addr.getHostAddress().equals(ADDRESS) || addr instanceof Inet4Address)
                throw new Exception("Invalid address specified.");

            if(PORT <= 0 || PORT >= 65536)
                throw new Exception("Specified port out of range.");

            File f = new File(STORAGE_DIR);
            if(f.exists()) {
                if(!f.isDirectory()) throw new Exception("Specified storage directory exists, and is not a directory.");
            }else {
                Manager.LOGGER.info("The specified storage directory does not exits, creating...");
                new File(f.getPath() + "/db").mkdirs();
                if(!f.mkdir()) throw new Exception("Could not create storage directory.");
            }

            SERVER.start(ADDRESS, PORT);
            if(isInitialized()) ProcessMonitor.INSTANCE.start();
        }catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static String getAddress() {
        return ADDRESS;
    }

    public static int getPort() {
        return PORT;
    }

    public static String getStorage() {
        return STORAGE_DIR;
    }

    public static void shutdown(boolean killApps) {
        SERVER.stop();
        if(killApps) ProcessMonitor.INSTANCE.shutdown();
        SwayDB.shutdown();
        LOGGER.info("Shutting down");
        System.exit(0);
    }

    public static void shutdown() {
        shutdown(true);
    }

    public static boolean isInitialized() {
        return STORAGE.get("INIT").isPresent();
    }
}