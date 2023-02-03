package fanta.ergosphere.process;

import fanta.ergosphere.main.Info;
import fanta.ergosphere.main.Manager;
import fanta.ergosphere.util.Docker;
import fanta.ergosphere.util.General;
import fanta.ergosphere.util.General.Command;
import fanta.ergosphere.util.SwayDB;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONObject;
import org.slf4j.Logger;
import swaydb.java.MultiMap;

public abstract class App {

    private final Logger LOGGER;

    private final MultiMap<String, String, String, Void> STORAGE;

    private final String REPO;
    private final Command COMMAND;
    private final Command[] EXTRA;

    private Process[] PROCESS;

    protected static final String INIT = "INIT";
    
    private final ArrayList<String> containers = new ArrayList<String>();
    
    private static final String AUTOSTART = "AUTOSTART";

    private AppState state;

    protected App(String gitName, Logger logger, Command... commands) {
        this(gitName, "", logger, commands);
    }
    
    protected App(String gitName, String containerName, Logger logger, Command... commands) {
        COMMAND = commands[0];
        if(commands.length > 1) {
            Command[] tmp = new Command[commands.length - 1];
            System.arraycopy(commands, 1, tmp, 0, commands.length - 1);
            EXTRA = tmp;
        }else {
            EXTRA = new Command[0];
        }
        REPO = gitName;
        LOGGER = logger;
        STORAGE = SwayDB.getTable(logger.getName());
        containers.add(containerName);
        state = isInitialized() ? AppState.START : AppState.INSTALL;
    }

    public final void start() {
        if(!isInitialized()) {
            LOGGER.error("Not initialized.");
            Manager.shutdown();
            return;
        }
        state = AppState.STARTING;
        try {
            PROCESS = new Process[1 + EXTRA.length];
            PROCESS[0] = General.run(COMMAND);
            for(int i = 1; i < PROCESS.length; i++) PROCESS[i] = General.run(EXTRA[i - 1]);
            LOGGER.info("Started.");
            state = AppState.STOP;
        }catch(IOException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("Stopped" + (state.equals(AppState.STOPPING) ? "." : " active monitoring."));
    }

    protected final void shutdown() throws IOException {
        state = AppState.STOPPING;
        if(PROCESS != null) for(Process p : PROCESS) p.destroy();
        for(String name : containers) Docker.stop(name);
        if(new File(COMMAND.dir).exists()) General.run(new Command(Docker.stopInFolder, COMMAND.dir));
        for(Command c : EXTRA) if(new File(c.dir).exists()) General.run(new Command(Docker.stopInFolder, c.dir));
        state = AppState.START;
    }

    protected final void addContainer(String name) {
        containers.add(name);
    }

    protected final boolean isRunning() {
        return PROCESS == null ? false : Arrays.stream(PROCESS).allMatch(x -> x.isAlive());
    }

    protected final String getReponame() {
        return REPO;
    }

    protected final String getDirName() {
        return General.getDirName(LOGGER);
    }

    protected final void putToDB(String key, String val) {
        STORAGE.put(key, val);
    }
    
    protected final String getFromDB(String key) {
        return STORAGE.get(key).orElse("");
    }

    protected final int getUsedMem_MiB() {
        return Arrays.stream(PROCESS).mapToInt(x -> General.byteToMB(Info.getProcess(x.pid()).getVirtualSize())).sum();
    }

    protected final boolean isInitialized() {
        return getFromDB(INIT).equals("true");
    }

    protected final void initStarted() {
        state = AppState.INSTALLING;
    }

    protected final void initOK() {
        if(!isInitialized()) {
            STORAGE.put(INIT, "true");
            state = AppState.START;
        }
    }

    protected final JSONObject getDockerStats() {
        final JSONObject jo = new JSONObject();
        for(String name : containers) jo.put(name, Docker.stat(name));
        return jo;
    }

    protected final void setAutoStart(boolean b) {
        putToDB(AUTOSTART, "" + b);
    }

    protected final boolean isAutoStarted() {
        return getFromDB(AUTOSTART).equals("true");
    }

    protected final AppState getAppState() {
        return state;
    }

    public abstract JSONObject getStatus();
    public abstract String getDescription();
    public abstract String getAppName();
    protected abstract String getLink();
    protected abstract void init();
    protected abstract App reset();
}