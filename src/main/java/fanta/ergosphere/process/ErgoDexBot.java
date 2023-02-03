package fanta.ergosphere.process;

import fanta.ergosphere.main.Info;
import fanta.ergosphere.util.Docker;
import fanta.ergosphere.util.General;
import fanta.ergosphere.util.General.Command;
import fanta.ergosphere.util.GitUtil;
import java.io.File;
import java.io.IOException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ErgoDexBot extends App {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErgoDexBot.class);

    private static final String ADDRESS = "ADDRESS";

    private static final String PREFIX = (Info.isWindows() ? "powershell" : "sh") + " ";
    private static final String DIR    = General.getDirName(LOGGER) + "/dex";

    protected ErgoDexBot() {
        super("ergolabs/ergo-dex-backend", "dex", LOGGER, new Command(PREFIX + (Info.isWindows() ? "\"" + new File(DIR + "/run.ps1").getAbsolutePath() + "\"" : "run"), DIR));
    }

    @Override
    public JSONObject getStatus() {
        return Docker.stat("dex");
    }

    @Override
    protected void init() {
        initStarted();
        try {
            GitUtil.downloadLatest("ergolabs/ergo-dex-backend", "ergodexbot/dex");
            if(getFromDB(ADDRESS).isEmpty()) putToDB(ADDRESS, ProcessMonitor.INSTANCE.getNewAddress());
            General.createFile(LOGGER, "dex/config.env", "JAVA_TOOL_OPTIONS=\"-Dnetwork.node-uri=" + Node.LOCAL + "/ -Dexchange.reward-address=" + getFromDB(ADDRESS) + "\"");
            LOGGER.info("Building ErgoDexBot. (There won't be a finish message, so wait until the CPU is NOT at 100% and the \"ergodexbot\" folder is ~1GB)");
            General.run(new Command(PREFIX + (Info.isWindows() ? "\"" + new File(DIR + "/build.ps1").getAbsolutePath() + "\"" : "build"), DIR));
        }catch(IOException e) {
            LOGGER.error("Could not initialize: " + e.getMessage());
            return;
        }
        initOK();
    }

    @Override
    public String getAppName() {
        return "ErgoDex offchain";
    }

    @Override
    public String getDescription() {
        return "Offchain services facilitating ErgoDEX functioning";
    }

    @Override
    public String getLink() {
        return "";
    }

    @Override
    protected App reset() {
        return new ErgoDexBot();
    }
}