package fanta.ergosphere.process;

import fanta.ergosphere.main.Manager;
import fanta.ergosphere.util.Chain.Block;
import fanta.ergosphere.util.Chain.Box;
import fanta.ergosphere.util.Chain.Tx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fanta.ergosphere.util.Docker;
import fanta.ergosphere.util.General;
import fanta.ergosphere.util.General.Command;
import fanta.ergosphere.util.SwayDB;
import fanta.ergosphere.util.General.Header;
import fanta.ergosphere.util.SwayDB.Table;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public final class Node extends App {

    private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);

    public static final String NAME = "NAME";
    public static final String API_KEY = "APIKEY";
    public static final String MEMORY = "MAXMEM";
    public static final String PASS = "PASS";
    public static final String MNPASS = "MNPASS";
    public static final String SEED = "SEED";

    private volatile boolean LOADED = false;

    private final Header API;

    public static final String LOCAL = "http://" + Manager.getAddress() + ":9053";

    protected Node() {
        super("ergoplatform/ergo", "ergo_node", LOGGER,
  new Command("docker run --rm -p 9030:9030 --name \"ergo_node\" "
            + "-p " + Manager.getAddress() + ":9053:9053 "
            + "-v " + General.getPathOf(LOGGER) + ":/home/ergo/.ergo "
            + "-v " + General.getPathOf(LOGGER) + "/settings.conf:/home/ergo/settings.conf "
            + "-e MAX_HEAP=" + SwayDB.getTable(Table.NODE).get(MEMORY).orElse("1000") + "M "
            + "ergoplatform/ergo --mainnet -c /home/ergo/settings.conf", General.getDirName(LOGGER)));
        if(getFromDB(API_KEY).isEmpty()) putToDB(API_KEY, General.random(15));
        API = new Header("api_key", getFromDB(API_KEY));
    }

    @Override
    public JSONObject getStatus() {//do something later for wallet tab General.getJSONArray("https://api.ergodex.io/v1/amm/markets") <-- /wallet/balances/withUnconfirmed
        if(!isRunning()) return new JSONObject().put("status", 2);
        if(!LOADED) return new JSONObject().put("status", 1).put("balance", 0).put("txs", new JSONArray()).put("blocks", new JSONArray())
                                           .put("fullHeight", -1).put("headersHeight", -1).put("maxPeerHeight", -1).put("fee", 0);

        final JSONObject info = General.getJSONObject(LOCAL + "/info");

        final long balance = General.getJSONObject(LOCAL + "/wallet/balances", new Header("api_key", getFromDB(API_KEY))).getLong("balance");

        final JSONArray wb = General.getJSONArray(LOCAL + "/wallet/boxes?minConfirmations=-1&minInclusionHeight=0", new Header("api_key", getFromDB(API_KEY)));
        final ArrayList<Box> boxes = new ArrayList<Box>();
        wb.forEach(x -> boxes.add(new Box(x)));
        final JSONArray txs = new JSONArray();
        boxes.stream().mapToLong(x -> x.time).distinct().sorted().forEachOrdered(y -> {
            txs.put(new Tx(boxes.stream().filter(z -> z.time == y).toArray(Box[]::new)).toJSON());
        });

        final boolean isFull = info.optInt("fullHeight") != 0;
        final int COUNT = 4, height = isFull ? info.getInt("fullHeight") : info.optInt("headersHeight", COUNT - 1);
        final List<Object> headers = General.getJSONArray(LOCAL + "/blocks?limit=" + COUNT + "&offset=" + Math.max(0, height - COUNT - 1)).toList();
        final JSONArray blocks = new JSONArray();
        for(int i = headers.size() - 1; i >= 0; i--) blocks.put(new Block(headers.get(i), isFull).toJSON());

        final long txFee = Long.parseLong(General.getFromURL(LOCAL + "/transactions/getFee?waitTime=1&txSize=100"));

        return info.put("balance", balance).put("txs", txs).put("status", 0).put("blocks", blocks).put("fee", txFee);
    }

    @Override
    public void init() {
        initStarted();
        Docker.download(getReponame());
        try {
            General.BLAKE2B256.reset();
            General.createFile(LOGGER, "settings.conf",
                    "ergo {" +
                    "\n  node {" +
                    "\n    mining = false" +
                    "\n  }" +
                    "\n}" +
                    "\nscorex {" +
                    "\n  network {" +
                    "\n    nodeName = \""+ getFromDB(NAME) +"\"" +
                    "\n  }" +
                    "\n  restApi {" +
                    "\n    apiKeyHash = \"" + General.bytesToHex(General.BLAKE2B256.digest(getFromDB(API_KEY).getBytes(UTF_8))) + "\"" +
                    "\n  }" +
                    "\n}");
        }catch(IOException e) {
            LOGGER.error(e.getMessage());
            Manager.shutdown();
        }
        initOK();
    }

    protected void processLog(final String log) { // TODO fix this
        if(!LOADED && (log.contains("State database read") || log.contains("New best"))) {
            LOADED = true;
            LOGGER.info("Loaded.");
            if(getFromDB(PASS).isEmpty()) initWallet();
        }
    }

    private void initWallet() {
        final String pass = General.random(15), mnPass = General.random(15);
        try{
            final JSONObject jo = General.post(LOCAL + "/wallet/init", "{\"pass\":\"" + pass + "\",\"mnemonicPass\":\"" + mnPass + "\"}", API);
            if(jo.has("detail")) throw new IOException(jo.getString("detail")); else putToDB(SEED, jo.getString("mnemonic"));
        }catch(IOException e) {
            LOGGER.error("Could not initialize: " + e.getMessage());
            Manager.shutdown();
            return;
        }
        putToDB(PASS, pass);
        putToDB(MNPASS, mnPass);
        LOGGER.info("Initialized wallet");
    }

    @Override
    public String getAppName() {
        return "Ergo node";
    }

    @Override
    public String getDescription() {
        return "Ergo full node";
    }

    @Override
    public String getLink() {
        return isRunning() ? "http://" + Manager.getAddress() + ":9053" : "";
    }

    @Override
    protected App reset() {
        return new Node();
    }

    protected String getNewAddress() throws IOException {
        General.post(LOCAL + "/wallet/unlock", "{\"pass\":\"" + getFromDB(PASS) + "\"}", API);
        final JSONObject jo = General.getJSONObject(LOCAL + "/wallet/deriveNextKey", API);
        General.getJSONObject(LOCAL + "/wallet/lock", API);
        return jo.getString("address");
    }

    protected String getAPIkey() {
        return getFromDB(API_KEY);
    }
}