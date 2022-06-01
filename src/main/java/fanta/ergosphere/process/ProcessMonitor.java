package fanta.ergosphere.process;

import fanta.ergosphere.main.Info;
import fanta.ergosphere.util.Credentials;
import java.io.IOException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fanta.ergosphere.util.General;
import fanta.ergosphere.util.SwayDB;
import org.json.JSONArray;

public final class ProcessMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class);

    public static final ProcessMonitor INSTANCE = new ProcessMonitor();

    private static enum Apps {
        NODE(new Node()),
        EXPLORER(new Explorer()),
        MIXER(new Mixer()),
        EDXBOT(new ErgoDexBot());

        private App app;
        private Apps(App a) {
            app = a;
        }
        protected App get() {
            return app;
        }
        protected void reset() {
            app = app.reset();
        }
    }

    private ProcessMonitor() {}

    public void start() {
        LOGGER.info("Started process monitor.");
        for(Apps x : Apps.values()) if(x.get().isAutoStarted()) x.get().start();
    }

    public JSONObject getStatus() {
        final JSONObject status = new JSONObject();
        status.put("price", General.getJSONObject("https://api.coingecko.com/api/v3/simple/price?ids=ergo&vs_currencies=USD").getJSONObject("ergo").getFloat("usd"));
        status.put("user", Credentials.getUsername());
        status.put("node", Apps.NODE.get().getStatus().put("stats", Apps.NODE.get().getDockerStats()).toMap());
        status.put("system", new JSONObject().put("memUsed", Info.getUsedMem_MB())  .put("memFull", Info.getFullMem_MB())
                                             .put("hddUsed", Info.getUsedSpace_GB()).put("hddFull", Info.getFullSpace_GB())
                                             .put("cpu",     Info.getCPULoad())     .put("proc",    Info.getProcessCount()));
        final JSONArray apps = new JSONArray();
        for(Apps x : Apps.values()) if(!x.toString().equals("NODE")) apps.put(new JSONObject()
                .put("name",   x.get().getAppName())
                .put("desc",   x.get().getDescription())
                .put("status", x.get().getAppState().code())
                .put("link",   x.get().getLink()));
        status.put("apps", apps);
        return status;
    }

    public void shutdown() {
        try {
            for(Apps x : Apps.values()) x.get().shutdown();
        }catch(IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void initDefaults(int maxMem, String name) {
        SwayDB.getTable(Node.class.getName()).put(Node.MEMORY, "" + maxMem);
        SwayDB.getTable(Node.class.getName()).put(Node.NAME, name);
        Apps.NODE.get().init();
        Apps.NODE.get().setAutoStart(true);
        start();
    }

    protected String getNewAddress() throws IOException {
        return ((Node) Apps.NODE.get()).getNewAddress();
    }

    protected String getNodeAddress() {
        return ((Node) Apps.NODE.get()).getLink();
    }

    public String getAPIkey() {
        return ((Node) Apps.NODE.get()).getAPIkey();
    }

    public void doService(AppState as, String service) throws IOException {
        for(Apps x : Apps.values()) if(x.get().getAppName().equals(service)) switch(as) {
            case INSTALL   : x.get().init();               return;
            case START     : x.get().start();              return;
            case STOP      : x.get().shutdown();x.reset(); return;
        }
    }
}