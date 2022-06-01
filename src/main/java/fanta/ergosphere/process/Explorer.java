package fanta.ergosphere.process;

import fanta.ergosphere.main.Parameters;
import fanta.ergosphere.util.Docker;
import fanta.ergosphere.util.General;
import fanta.ergosphere.util.General.Command;
import fanta.ergosphere.util.GitUtil;
import java.io.IOException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Explorer extends App {

    private static final Logger LOGGER = LoggerFactory.getLogger(Explorer.class);

    private static final String BACKEND = "explorer_backend";
    private static final String FRONTEND = "explorer_frontend";

    protected Explorer() {
        super("ergoplatform/explorer-backend", BACKEND, LOGGER,
                new Command(Docker.startInFolder, General.getDirName(LOGGER) + "/" + BACKEND),
                new Command(Docker.startInFolder, General.getDirName(LOGGER) + "/" + FRONTEND));
        addContainer(FRONTEND);
    }

    @Override
    public JSONObject getStatus() {
        return new JSONObject().put("backend", Docker.stat(BACKEND)).put("frontend", Docker.stat(FRONTEND));
    }

    @Override
    protected void init() {
        initStarted();
        try {
            GitUtil.downloadLatest("ergoplatform/explorer-backend", "explorer/" + BACKEND);
            General.replaceLines(LOGGER, "explorer/" + BACKEND + "/modules/chain-grabber/src/main/resources/application.conf",   "master-nodes", "master-nodes = [\"" + Node.LOCAL + "\"]");
            General.replaceLines(LOGGER, "explorer/" + BACKEND + "/modules/explorer-api/src/main/resources/application.conf",    "http.host",    "http.host = \"" + Parameters.getAddress() + "\"");
            General.replaceLines(LOGGER, "explorer/" + BACKEND + "/modules/utx-broadcaster/src/main/resources/application.conf", "master-nodes", "master-nodes = [\"" + Node.LOCAL + "\"]");
            General.replaceLines(LOGGER, "explorer/" + BACKEND + "/modules/utx-tracker/src/main/resources/application.conf",     "master-nodes", "master-nodes = [\"" + Node.LOCAL + "\"]");
            General.run("sbt assembly", General.getDirName(LOGGER) + "/backend");

            GitUtil.downloadLatest("ergoplatform/explorer-frontend", "explorer/" + FRONTEND);
            General.replaceLines(LOGGER, "explorer/" + FRONTEND + "/src/config/environment.default.ts", "apiUrl",     "  apiUrl: \'http://" + Parameters.getAddress() + ":8080/api/v0\',");
            General.replaceLines(LOGGER, "explorer/" + FRONTEND + "/src/config/environment.default.ts", "apiBaseUrl", "  apiBaseUrl: \'http://" + Parameters.getAddress() + ":8080\',");
            General.replaceLines(LOGGER, "explorer/" + FRONTEND + "/src/config/environment.default.ts", "url: \'http://localhost:3000\',", "      url: \'http://" + Parameters.getAddress() + ":3000\',");
            General.replaceLines(LOGGER, "explorer/" + FRONTEND + "/src/config/environment.prod.ts",    "url:", "      url: \'http://" + Parameters.getAddress() + ":3000\',");
            General.createFile(LOGGER,   "explorer/" + FRONTEND + "/Dockerfile",
                "FROM node:13.12.0-alpine\n" +
                "WORKDIR /app\n" +
                "ENV PATH /app/node_modules/.bin:$PATH\n" +
                "COPY package.json ./\n" +
                "RUN npm install --silent\n" +
                "RUN npm install react-scripts@3.4.1 -g --silent\n" +
                "COPY . ./\n" +
                "CMD [\"npm\", \"start\"]");
            General.createFile(LOGGER, "explorer/frontend/docker-compose.yml",
                "version: '3.7'\n" +
                "\n" +
                "services:\n" +
                "\n" +
                "  " + FRONTEND + ":\n" +
                "    container_name: " + FRONTEND + "\n" +
                "    build:\n" +
                "      context: .\n" +
                "      dockerfile: Dockerfile\n" +
                "    volumes:\n" +
                "      - '.:/app'\n" +
                "      - '/app/node_modules'\n" +
                "    ports:\n" +
                "      - 3000:3000");
        }catch(IOException e) {
            LOGGER.error("Could not initialize: " + e.getMessage());
            return;
        }
        initOK();
    }

    @Override
    public String getAppName() {
        return "Ergo explorer";
    }

    @Override
    public String getDescription() {
        return "Ergo blockchain explorer";
    }

    @Override
    public String getLink() {
        return isRunning() ? "http://" + Parameters.getAddress() + ":3000" : "";
    }

    @Override
    protected void processLog(String log) {}

    @Override
    protected App reset() {
        return new Explorer();
    }
}