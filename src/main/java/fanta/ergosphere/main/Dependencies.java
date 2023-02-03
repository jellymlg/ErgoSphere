package fanta.ergosphere.main;

import fanta.ergosphere.util.Docker;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public final class Dependencies {

    private static final String LOCATE = Info.isWindows() ? "where " : "which ";

    private static final String[] DEPENDENCY = {
        "docker",
        "docker-compose",
        "sbt"
    };

    protected static final void init() {
        Manager.LOGGER.info("Checking dependecies...");
        for(String dep : DEPENDENCY) {
            try {
                Process p = Runtime.getRuntime().exec(LOCATE + dep);
                BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
                final String s = b.readLine();
                if(s == null || !(s.contains("\\") || s.contains("/"))) throw new IOException();
                b.close();
                p.destroy();
                if(dep.equals("docker")) {
                    BufferedReader tmp = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("docker version").getInputStream()));
                    String in;
                    while((in = tmp.readLine()) != null) {
                        if(in.contains("Server")) {
                            tmp.close();
                            return;
                        }
                    }
                    throw new IOException();
                }
            }catch(IOException e) {
                Manager.LOGGER.error("Dependency \"" + dep + "\" not found or not running.");
                Manager.shutdown(false);
                return;
            }
        }
        Docker.download("ergoplatform/ergo");
        Manager.LOGGER.info("Dependecies OK");
    }
}