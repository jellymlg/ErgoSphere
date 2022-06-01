package fanta.ergosphere.util;

import fanta.ergosphere.main.Manager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Docker {

    private static final Logger LOGGER = LoggerFactory.getLogger(Docker.class);

    public static final String stopInFolder =  "docker-compose stop";
    public static final String startInFolder = "docker-compose up -d";

    public static void download(String containerName) {
        LOGGER.info("Pulling " + containerName + " ...");
        try {
            boolean success = false;
            String s;
            Process p = Runtime.getRuntime().exec("docker pull " + containerName);
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while((s = b.readLine()) != null) {
                if(s.contains("Downloaded") || s.contains("up to date")) {
                    success = true;
                    LOGGER.info("Pulled " + containerName);
                }
            }
            if(!success) {
                LOGGER.error("Failed to pull " + containerName);
                Manager.shutdown();
            }
            b.close();
            p.destroy();
        }catch(IOException e) {
            LOGGER.error(e.getMessage());
            Manager.shutdown();
        }
    }

    public static void stop(String name) {
        try {
            Runtime.getRuntime().exec("docker stop " + name);
        }catch(IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static JSONObject stat(String name) {
        try {
            BufferedReader b = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("docker stats --no-stream --format \"{{ json . }}\"").getInputStream()));
            while(b.ready()) {
                final JSONObject jo = new JSONObject(b.readLine());
                if(jo.getString("Name").contains(name)) {
                    b.close();
                    return jo;
                }
            }
        }catch(IOException e) {
            LOGGER.error(e.getMessage());
        }
        return new JSONObject();
    }
}