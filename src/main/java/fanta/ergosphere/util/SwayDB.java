package fanta.ergosphere.util;

import fanta.ergosphere.main.Parameters;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swaydb.java.MultiMap;
import swaydb.java.persistent.PersistentMultiMap;
import static swaydb.java.serializers.Default.stringSerializer;

public final class SwayDB {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwayDB.class);

    private static final SwayDB INSTANCE = new SwayDB();

    private final MultiMap<String, String, String, Void> DB;

    private SwayDB() {
        LOGGER.info("Using \"" + Parameters.getStorage() + "\" for data storage.");
        DB = PersistentMultiMap.functionsOff(Paths.get(Parameters.getStorage() + "/db"), stringSerializer(), stringSerializer(), stringSerializer()).get();
        LOGGER.info("Started database system.");
    }

    public static MultiMap<String, String, String, Void> getTable(String name) {
        return INSTANCE.DB.child(name);
    }
    
    public static void shutdown() {
        INSTANCE.DB.close();
    }

    public static void dump() {
        INSTANCE.DB.children().forEach(x -> x.keys().forEach(y -> LOGGER.info(x.mapKey() + ": " + y + " -> " + x.get(y))));
    }
}