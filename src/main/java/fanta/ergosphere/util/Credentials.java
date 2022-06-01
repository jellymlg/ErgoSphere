package fanta.ergosphere.util;

import fanta.ergosphere.main.Manager;
import static fanta.ergosphere.util.General.SHA256;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swaydb.java.MultiMap;

public final class Credentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(Credentials.class);
    
    private static final MultiMap<String, String, String, Void> STORAGE = SwayDB.getTable(LOGGER.getName());

    public static final int USER_MIN_LEN = 5, PASS_MIN_LEN = 5;

    private static final String USERNAME = "USER";
    private static final String PASSWORD = "PASS";

    public static boolean storeCredentials(final String username, final String password) {
        if(username.length() < USER_MIN_LEN) {
            LOGGER.error("Can't set username \"" + username + "\", because it's less than " + USER_MIN_LEN + " characters long.");
            return false;
        }
        if(password.length() < PASS_MIN_LEN) {
            LOGGER.error("Can't set password \"" + password + "\", because it's less than " + PASS_MIN_LEN + " characters long.");
            return false;
        }

        STORAGE.put(USERNAME, username);
        STORAGE.put(PASSWORD, hash(password, ""));

        return true;
    }
    
    public static boolean isCorrect(final String username, final String password) {
        final Optional<String> u = STORAGE.get(USERNAME);
        final Optional<String> p = STORAGE.get(PASSWORD);
        if(u.isEmpty() || p.isEmpty()) {
            LOGGER.error("Could not retreive username/password from database.");
            Manager.shutdown();
            return false;
        }
        return u.get().equals(username) && hash(password, "").equals(p.get());
    }

    public static String getUsername() {
        return STORAGE.get(USERNAME).orElse("");
    }

    private static String hash(final String data, final String salt) {
        SHA256.reset();
        return General.bytesToHex(SHA256.digest((data + salt).getBytes(UTF_8)));
    }
}