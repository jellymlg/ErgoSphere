package fanta.ergosphere.process;

import fanta.ergosphere.main.Parameters;
import fanta.ergosphere.util.Docker;
import fanta.ergosphere.util.General;
import fanta.ergosphere.util.General.Command;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Mixer extends App {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mixer.class);

    private static final String USER = "DBUSER";
    private static final String PASS = "DBPASS";

    protected Mixer() {
        super("ergoMixer/ergoMixBack", "ergo_mixer", LOGGER, new Command("docker run --rm -p " + Parameters.getAddress() + ":9000:9000 "
                                                      + "--name \"ergo_mixer\" "
                                                      + "-v " + General.getPathOf(LOGGER) + ":/home/ergo/ergoMixer "
                                                      + "-v " + General.getPathOf(LOGGER) + "/config.conf:/home/ergo/mixer/application.conf "
                                                      + "ergomixer/mixer:latest", General.getDirName(LOGGER)));
    }

    @Override
    public JSONObject getStatus() {
        return new JSONObject();
    }

    @Override
    protected void init() {
        initStarted();
        try {
            Docker.download("ergomixer/mixer");
            if(Files.notExists(Paths.get(getDirName()))) new File(getDirName()).mkdir();
            PrintWriter p = new PrintWriter(getDirName() + "\\config.conf");
            putToDB(USER, General.random(15));
            putToDB(PASS, General.random(15));
            p.println("play.http.secret.key = \"QCY?tAnfk?aZ?iwrNwnxIlR6CTf:G3gf:90Latabg@5241AB`R5W:1uDFN];Ik@n\"\n" +
                    "http.port = 9000\n" +
                    "\n" +
                    "play: {\n" +
                    "  http {\n" +
                    "        filters=\"filters.CorsFilters\",\n" +
                    "        fileMimeTypes = ${play.http.fileMimeTypes} \"\"\"\n" +
                    "                 wasm=application/wasm\n" +
                    "                \"\"\"\n" +
                    "       }\n" +
                    "  filters {\n" +
                    "    hosts {\n" +
                    "      # Allow requests to example.com, its subdomains, and localhost:9000.\n" +
                    "      allowed = [\"localhost\", \"127.0.0.1\", \"" + Parameters.getAddress() + "\"]\n" +
                    "    }\n" +
                    "    cors {\n" +
                    "      pathPrefixes = [\"/\"]\n" +
                    "      allowedOrigins = null,\n" +
                    "      allowedHttpMethods = [\"GET\", \"POST\"]\n" +
                    "      allowedHttpHeaders = null\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n" +
                    "\n" +
                    "networkType = \"mainnet\"\n" +
                    "explorerBackend = \"https://api.ergoplatform.com\"\n" +    //TODO require local?
                    "explorerFrontend = \"https://explorer.ergoplatform.com\"\n" +
                    "nodes = [\"" + Parameters.getAddress() + ":9053\", \"213.239.193.208:9053\", \"159.65.11.55:9053\", \"165.227.26.175:16042\", \"159.89.116.15:11088\"]\n" +
                    "\n" +
                    "// database info, will be saved in home directory. this is where all secrets are saved. make sure its safe.\n" +
                    "database = {\n" +
                    "  // whether to prune done mixes\n" +
                    "  // this is mostly for performance reasons, we recommend you to enable the pruning\n" +
                    "  prune = false\n" +
                    "  // number of confirmations to wait for the withdrawn transaction, before pruning a mix\n" +
                    "  // so for example, if set to 720, mixes that have been withdrawn and confirmed for 720 blocks (around 1 day) will be prunned\n" +
                    "  pruneAfter = 720\n" +
                    "\n" +
                    "  user = \"" + getFromDB(USER) + "\"\n" +
                    "  pass = \"" + getFromDB(PASS) + "\"\n" +
                    "}\n" +
                    "\n" +
                    "jobInterval = 180 // interval between mixing boxes, no need to be less than 2 min! around 4 mins seems to be smoothest.\n" +
                    "statisticJobsInterval = 3600 // period between updating statistics. Doesn't need to be small!\n" +
                    "numConfirmation = 3 // increasing this will cause slower mixes but more confidence about avoiding rare cases (forks). better to avoid setting it less than 2!\n" +
                    "maxOutputs = 10 // maximum number of outputs in one txs.\n" +
                    "maxInputs = 6 // maximum number of inputs in one txs.\n" +
                    "hopRounds = 0 // number of hops when withdrawing a box from mix");
            p.close();
        }catch(IOException e) {
            LOGGER.error("Could not initialize: " + e.getMessage());
            return;
        }
        initOK();
    }

    @Override
    public String getAppName() {
        return "ErgoMixer";
    }

    @Override
    public String getDescription() {
        return "ErgoMixer is a web application for mixing ergs and tokens based on Ergo platform";
    }

    @Override
    public String getLink() {
        return isRunning() ? "http://" + Parameters.getAddress() + ":9000" : "";
    }

    @Override
    protected App reset() {
        return new Mixer();
    }
}