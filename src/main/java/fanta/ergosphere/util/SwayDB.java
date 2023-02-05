package fanta.ergosphere.util;

import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fanta.ergosphere.main.Manager;
import swaydb.java.MultiMap;
import swaydb.java.persistent.PersistentMultiMap;
import static swaydb.java.serializers.Default.stringSerializer;

public final class SwayDB {

    public static final class DB {
        private final MultiMap<String, String, String, Void> table;
        DB(MultiMap<String, String, String, Void> table) {
            this.table = table;
        }
        public Optional<String> get(String key) {
            return table.get(key);
        }
        public void put(String key, String value) {
            table.put(key, value);
        }
    }

    public enum Table {
        MANAGER("manager"),
        CREDENTIALS("credentials"),
        NODE("node");
        final String name;
        private Table(String name) {
            this.name = name;
        }
        public static Table fromLogger(Logger logger) {
            return valueOf(Table.class, logger.getName().toUpperCase());
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SwayDB.class);

    private static final SwayDB INSTANCE = new SwayDB();

    private final MultiMap<String, String, String, Void> DB;

    private SwayDB() {
        LOGGER.info("Using \"" + Manager.getStorage() + "\" for data storage.");
        DB = PersistentMultiMap.functionsOff(Paths.get(Manager.getStorage() + "/db"), stringSerializer(), stringSerializer(), stringSerializer()).get();
        LOGGER.info("Started database system.");
    }

    public static DB getTable(Table table) {
        return new DB(INSTANCE.DB.child(table.name));
    }

    public static void shutdown() {
        INSTANCE.DB.close();
    }

    public static void dump() {
        INSTANCE.DB.children().forEach(x -> x.keys().forEach(y -> LOGGER.info(x.mapKey() + ": " + y + " -> " + x.get(y))));
    }
}