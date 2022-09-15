package util;

import exceptions.ConfigNotFoundException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesProvider {

    public static Map<String, String> configurationProperties = new HashMap<>();

    public static void setup() {
        loadConfigs("src/main/resources/bot.properties");
    }

    private static void loadConfigs(String configFile) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(configFile));
        } catch (IOException e) {
            throw new ConfigNotFoundException(String.format("Config \"%s\" not found", configFile));
        }
        properties.forEach((key, value) -> {
            configurationProperties.put(key.toString().trim(), value.toString().trim());
        });
    }
}
