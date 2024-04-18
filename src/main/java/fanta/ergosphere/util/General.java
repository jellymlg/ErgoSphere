package fanta.ergosphere.util;

import com.rfksystems.blake2b.security.Blake2bProvider;
import fanta.ergosphere.main.Info;
import fanta.ergosphere.main.Manager;
import fanta.ergosphere.process.Node;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.net.UnknownHostException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Random;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

public final class General {

    public static final class Header {
        protected final String key, value;
        public Header(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static final class Command {
        public final String cmd, dir;
        public final String[] env;
        public Command(String cmd, String dir, String... env) {
            this.cmd = cmd;
            this.dir = dir;
            this.env = env;
        }
    }

    public static final MessageDigest SHA256     = initDigest("SHA-256");
    public static final MessageDigest BLAKE2B256 = initDigest("BLAKE2B-256");
    
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    private static MessageDigest initDigest(final String name) {
        Security.addProvider(new Blake2bProvider());
        try {
            return MessageDigest.getInstance(name);
        }catch(NoSuchAlgorithmException e) {
            Manager.LOGGER.error(e.getMessage());
            Manager.shutdown();
        }
        return null;
    }

    public static void serverInfo(Logger logger, HttpServletRequest request) {
        serverInfo(logger, request, "");
    }
    public static void serverInfo(Logger logger, HttpServletRequest request, String extraMsg) {
        logger.info(request.getHeader("Host") + " GET /status [" + request.getHeader("User-Agent") + "]" + (!extraMsg.isEmpty() ? " " : "") + extraMsg);
    }
    public static void serverWarn(Logger logger, HttpServletRequest request) {
        serverWarn(logger, request, "");
    }
    public static void serverWarn(Logger logger, HttpServletRequest request, String extraMsg) {
        logger.warn(request.getHeader("Host") + " GET /status [" + request.getHeader("User-Agent") + "]" + (!extraMsg.isEmpty() ? " " : "") + extraMsg);
    }
    public static void serverError(Logger logger, HttpServletRequest request) {
        serverError(logger, request, "");
    }
    public static void serverError(Logger logger, HttpServletRequest request, String extraMsg) {
        logger.error(request.getHeader("Host") + " GET /status [" + request.getHeader("User-Agent") + "]" + (!extraMsg.isEmpty() ? " " : "") + extraMsg);
    }

    public static String random(final int length) {
        String tmp = new String(new Random().ints(length, 97, 123).toArray(), 0, length);
        for(int i = 0; i < length; i++) {
            if((int) (Math.random() * 10) % 3 == 0) tmp = tmp.substring(0, i) + ((int) (Math.random() * 10)) + tmp.substring(i + 1, length);
            if(Character.isLetter(tmp.charAt(i)) && (int) (Math.random() * 10) % 2 == 0) tmp = tmp.substring(0, i) + (Character.toUpperCase(tmp.charAt(i))) + tmp.substring(i + 1, length);
        }
        return tmp;
    }

    public static String getLocalIP() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        }catch(UnknownHostException e) {
            Manager.LOGGER.error("Could not get local IP address.");
            Manager.shutdown();
        }
        return null;
    }

    public static String getDirName(Logger l) {
        return Manager.getStorage() + "/" + l.getName().toLowerCase().substring(l.getName().lastIndexOf(".") + 1);
    }

    public static String bytesToHex(byte[] data) {
        byte[] buf = new byte[data.length * 2];
        for(int i = 0; i < data.length; i++) {
          int v = data[i] & 0xFF;
          buf[i * 2] = (byte) hexArray[v >>> 4];
          buf[i * 2 + 1] = (byte) hexArray[v & 0x0F];
        }
        return new String(buf, UTF_8);
    }

    public static int byteToMB(long bytes) {
        return (int) (bytes / 1024L / 1024L);
    }

    public static String getFromURL(String URL, Header... headers) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(URL).openConnection();
            for(Header h : headers) con.setRequestProperty(h.key, h.value);
            con.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String data = "";
            while(in.ready()) data += in.readLine().trim();
            in.close();
            con.disconnect();
            return data;
        }catch(IOException e) {
            Manager.LOGGER.error(e.getMessage());
        }
        return "{}";
    }

    public static JSONArray getJSONArray(String URL, Header... headers) {
        return new JSONArray(getFromURL(URL, headers));
    }

    public static JSONObject getJSONObject(String URL, Header... headers) {
        return new JSONObject(getFromURL(URL, headers));
    }

    public static JSONObject jo(Object o) {
        return (JSONObject) JSONObject.wrap(o);
    }

    public static long getDate(long block) {
        final String id = getJSONArray(Node.LOCAL + "/blocks/at/" + block).getString(0);
        return getJSONObject(Node.LOCAL + "/blocks/blocks/" + id).getJSONObject("header").getLong("timestamp");
    }

    public static String getPathOf(Logger l) {
        return new File(General.getDirName(l)).getAbsolutePath();
    }

    public static String toFolder(String s) {
        return "cd " + (Info.isWindows() ? "/D " : "") + s;
    }

    public static String toFolder(Logger l) {
        return toFolder(getPathOf(l));
    }

    public static Process run(Command c) throws IOException {
        return new ProcessBuilder(c.cmd)
        .directory(new File(c.dir))
        .redirectOutput(Redirect.appendTo(new File(c.dir + "/log.txt")))
        .start();
    }

    public static boolean isBadReq(Logger l, HttpServletRequest request, HttpServletResponse response, String data) throws IOException {
        if((request.getContentType() != null && !request.getContentType().contains("json")) || data.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("{\"status\":\"Bad request\"}");
            serverError(l, request, "Bad request");
            return true;
        }
        return false;
    }

    public static void replaceLines(Logger logger, String file, String filter, String replacement) throws IOException {
        final ArrayList<String> lines = new ArrayList<String>(Files.readAllLines(Paths.get(getDirName(logger) + file), UTF_8));
        for(int i = 0; i < lines.size(); i++) if(lines.get(i).contains(filter)) lines.set(i, replacement);
        Files.write(Paths.get(getDirName(logger) + file), lines, UTF_8);
    }

    public static void createFile(Logger logger, String file, String data) throws IOException {
        new File(getDirName(logger)).mkdirs();
        Files.write(Paths.get(getDirName(logger) + "/" + file), data.getBytes(UTF_8));
    }

    public static JSONObject post(String url, String data, Header... headers) throws IOException {
        final HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setFixedLengthStreamingMode(data.getBytes(UTF_8).length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        for(Header h : headers) http.addRequestProperty(h.key, h.value);
        http.connect();
        http.getOutputStream().write(data.getBytes(UTF_8));
        final String in = new String(http.getInputStream().readAllBytes(), UTF_8);
        http.disconnect();
        return new JSONObject(in.contains("OK") ? "{}" : in);
    }
}