package client;
import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final Properties properties = new Properties();
    private static boolean loaded = false;


    /**
     * Loads the config file once. If not found, uses defaults.
     */
    static void loadConfig() {
        if (!loaded) {
            File configFile = new File(CONFIG_FILE_NAME);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            loaded = true;
        }
    }

    /**
     * Persists the current properties to the config file.
     */
    private static void saveConfig() {
        File configFile = new File(CONFIG_FILE_NAME);
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, "User Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the language code from the config.
     * Defaults to "en" if not set.
     *
     * @return the language code from the configuration or "en" as default.
     */

    public static String getLanguage() {
        loadConfig();
        return properties.getProperty("language", "en");
    }

    /**
     * Sets the language code and saves it to the configuration file.
     *
     * @param langCode the new language code to be set in the configuration.
     */
    public static void setLanguage(String langCode) {
        loadConfig();
        properties.setProperty("language", langCode);
        saveConfig();
    }
}
