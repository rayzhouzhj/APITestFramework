package com.rayzhouzhj.framework.utils;

import com.rayzhouzhj.framework.testng.listeners.RetryAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ConfigFileManager - Read config file statically into configFileMap
 */
public class ConfigFileReader {
    private static final Logger frameworkLogger = LoggerFactory.getLogger(ConfigFileReader.class);
    private static Map<String, String> configFileMap = new HashMap<>();
    private static Properties prop = new Properties();
    private static ConfigFileReader instance;

    private ConfigFileReader(String configFile) throws IOException {
        FileInputStream inputStream = new FileInputStream(configFile);
        prop.load(inputStream);
    }

    public static ConfigFileReader getInstance() {
        if (instance == null) {
            String configFile = "config.properties";
            try {
                if (System.getenv().containsKey("CONFIG_FILE")) {
                    configFile = System.getenv().get("CONFIG_FILE");
                    frameworkLogger.info("Using config file from " + configFile);
                }

                instance = new ConfigFileReader(configFile);
                Enumeration<?> keys = prop.propertyNames();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    configFileMap.put(key, prop.getProperty(key));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public String getProperty(String object) {
        return configFileMap.get(object);
    }

    public String getProperty(String key, String value) {
        return configFileMap.getOrDefault(key, value);
    }
}
