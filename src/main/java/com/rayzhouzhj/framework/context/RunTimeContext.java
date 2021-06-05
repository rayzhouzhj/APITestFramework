package com.rayzhouzhj.framework.context;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import com.rayzhouzhj.framework.testng.listeners.InvokedMethodListener;
import com.rayzhouzhj.framework.utils.ConfigFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunTimeContext {
    private static final Logger frameworkLogger = LoggerFactory.getLogger(RunTimeContext.class);
    private static RunTimeContext instance;
    private ThreadLocal<HashMap<String, Object>> testLevelVariables = new ThreadLocal<>();

    private RunTimeContext() {

    }

    public static synchronized RunTimeContext getInstance() {
        if (instance == null) {
            instance = new RunTimeContext();
        }

        return instance;
    }

    public boolean isParallelExecution() {
        return "parallel".equalsIgnoreCase(this.getProperty("RUNNER", "").trim());
    }

    public String getProperty(String name) {
        return this.getProperty(name, null);
    }

    public String getProperty(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            value = ConfigFileReader.getInstance().getProperty(key, defaultValue);
        }

        return value;
    }

    public synchronized String getLogPath(String category, String className, String methodName) {
        String path = System.getProperty("user.dir")
                + File.separator + "target"
                + File.separator + category
                + File.separator + className
                + File.separator + methodName;

        File file = new File(path);
        if (!file.exists()) {
            if (file.mkdirs()) {
                frameworkLogger.info("Directory [" + path + "] is created!");
            } else {
                frameworkLogger.info("Failed to create directory!");
            }
        }

        return path;
    }

    public static String currentDateAndTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
        return now.truncatedTo(ChronoUnit.SECONDS).format(dtf).replace(":", "-");
    }

    public Object getTestLevelVariables(String name) {
        return this.testLevelVariables.get().getOrDefault(name, null);
    }

    public void setTestLevelVariables(String name, Object data) {
        if (this.testLevelVariables.get() == null) {
            this.testLevelVariables.set(new HashMap<>());
        }

        this.testLevelVariables.get().put(name, data);
    }

    public void clearRunTimeVariables() {
        if (this.testLevelVariables.get() != null) {
            this.testLevelVariables.get().clear();
        }
    }
}
