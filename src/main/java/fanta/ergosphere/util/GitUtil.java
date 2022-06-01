package fanta.ergosphere.util;

import fanta.ergosphere.main.Manager;
import fanta.ergosphere.main.Parameters;
import java.io.File;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GitUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitUtil.class);

    public static final String REPO = "https://github.com/";
    
    public static void downloadLatest(String repoName, String targetDir) {
        try {
            final File f = new File(Parameters.getStorage() + "/" + targetDir);
            if(!f.exists()) f.mkdirs();
            Git.cloneRepository().setURI(REPO + repoName + ".git").setDirectory(f).call().close();
        }catch(GitAPIException e) {
            LOGGER.error(e.getMessage());
            Manager.shutdown();
        }
        LOGGER.info("Successfully downloaded \"" + REPO + repoName + ".git\" to \"" + targetDir + "\"");
    }
}