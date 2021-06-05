package com.rayzhouzhj.framework.test.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.rayzhouzhj.framework.context.RunTimeContext;
import com.rayzhouzhj.framework.report.ReportManager;

import io.restassured.response.Response;

public class TestLogger {
    private static final Logger frameworkLogger = LoggerFactory.getLogger(TestLogger.class);
    public void logJson(String json) {
        logJson(json, null);
    }

    public void logJson(String json, String fileName) {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        int classIndex = 0;
        for (int i = 0; i < stElements.length; i++) {
            if (stElements[i].getClassName().equals("sun.reflect.NativeMethodAccessorImpl")) {
                classIndex = i - 1;
                break;
            }
        }
        String filePath = RunTimeContext.getInstance().getLogPath("json", stElements[classIndex].getClassName(), stElements[classIndex].getMethodName());
        if (fileName == null) {
            filePath = filePath + File.separator + RunTimeContext.currentDateAndTime() + ".json";
        } else {
            filePath = filePath + File.separator + fileName + ".json";
        }

        FileWriter fw;
        try {
            fw = new FileWriter(filePath);
            fw.write(json);
            fw.flush();

            ReportManager.getInstance().logInfo("<a target='_blank' href='" + filePath + "'> JSON Response </a>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logInfo(String message) {
        frameworkLogger.info(message);
        ReportManager.getInstance().logInfo(message);
    }

    public void logPass(String message) {
        frameworkLogger.info(message);
        ReportManager.getInstance().logPass(message);
    }

    public void logFail(String message) {
        frameworkLogger.error(message);
        ReportManager.getInstance().logFail(message);
    }

    public void logFatalError(String message) {
        frameworkLogger.error(message);
        Assert.fail(message);
    }
}
