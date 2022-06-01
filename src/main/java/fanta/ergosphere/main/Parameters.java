package fanta.ergosphere.main;

import fanta.ergosphere.util.General;
import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Parameters {
    private static String ADDRESS = General.getLocalIP(), PORT = "80", STORAGE = "data/";
    public static String getAddress() {
        return ADDRESS;
    }
    public static int getPort() {
        return Integer.parseInt(PORT);
    }
    public static String getStorage() {
        return STORAGE;
    }
    protected static void init(String[] param) {
        for(String s : param) {
            String[] sa = s.split("=");
            if(sa.length != 2) badArg("Unknown argument: " + s);
            switch(sa[0]) {
                case "-h":
                case "-help":
                    printHelp();
                    Manager.shutdown();
                    break;
                case "-address":
                    if(checkAddr(sa[1])) ADDRESS = sa[1]; else badArg("Incorrect argument: " + sa[1]);
                    break;
                case "-port":
                    if(checkPort(sa[1]))PORT = sa[1]; else badArg("Incorrect argument: " + sa[1]);
                    break;
                case "-storage":
                    if(checkStorage(sa[1])) STORAGE = new File(sa[1]).getPath(); else badArg("Incorrect argument: " + sa[1]);
                    break;
                default:
                    badArg("Unknown argument: " + s);
            }
        }
    }
    private static void printHelp() {
        System.out.println("Usage: java -jar ErgoSphere.jar -address=<bind address> -port=<bind port> -storage=<data storage dir>");
        System.out.println("Running without arguments uses default values: -address=localhost -port=80 -storage=data/");
    }
    private static void badArg(String msg) {
        System.err.println(msg);
        printHelp();
        Manager.shutdown();
    }
    private static boolean checkAddr(String a) {
        try {
            InetAddress i = InetAddress.getByName(a);
            return i.getHostAddress().equals(a) && i instanceof Inet4Address;
        }catch(UnknownHostException e) {
            return false;
        }
    }
    private static boolean checkPort(String p) {
        return p.matches("[0-9]+") && Integer.parseInt(p) > 0 && Integer.parseInt(p) < 65536;
    }
    private static boolean checkStorage(String s) {
        File f = new File(s);
        if(f.exists()) {
            if(!f.isDirectory()) return false;
        }else {
            Manager.LOGGER.info("The specified storage directory does not exits, creating...");System.out.println(f.getPath());
            new File(f.getPath() + "/db").mkdirs();
            if(!f.mkdir()) return false;
        }
        return true;
    }
}