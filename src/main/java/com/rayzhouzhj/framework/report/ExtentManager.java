package com.rayzhouzhj.framework.report;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentXReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.rayzhouzhj.framework.context.RunTimeContext;

public class ExtentManager {
    private static ExtentReports extent;
    private static String filePath = System.getProperty("user.dir") + File.separator + "target" + File.separator + "APITestReport.html";

    public synchronized static ExtentReports getExtent() {
        if (extent == null) {
            extent = new ExtentReports();
            extent.attachReporter(getHtmlReporter());
            if (System.getenv("ExtentX") != null && System.getenv("ExtentX").equalsIgnoreCase("true")) {
                extent.attachReporter(getExtentXReporter());
            }

            String build = RunTimeContext.getInstance().getProperty("BuildNumber");
            if (build == null) build = "";

            extent.setSystemInfo("Build", build);

            List<Status> statusHierarchy = Arrays.asList(
                    Status.FATAL,
                    Status.FAIL,
                    Status.ERROR,
                    Status.WARNING,
                    Status.PASS,
                    Status.SKIP,
                    Status.DEBUG,
                    Status.INFO
            );

            extent.config().statusConfigurator().setStatusHierarchy(statusHierarchy);
        }

        return extent;
    }

    private static ExtentHtmlReporter getHtmlReporter() {
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(filePath);
        // make the charts visible on report open
        htmlReporter.config().setChartVisibilityOnOpen(true);

        // report title
        htmlReporter.config().setDocumentTitle("API Test Report");
        htmlReporter.config().setReportName("API Test Report");
        htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
        htmlReporter.config().setTheme(Theme.STANDARD);

        return htmlReporter;
    }

    private static ExtentXReporter getExtentXReporter() {
        String host = RunTimeContext.getInstance().getProperty("MONGODB_SERVER");
        Integer port = Integer.parseInt(RunTimeContext.getInstance().getProperty("MONGODB_PORT"));
        ExtentXReporter extentx = new ExtentXReporter(host, port);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        String product = RunTimeContext.getInstance().getProperty("Product");
        String buildNum = RunTimeContext.getInstance().getProperty("BuildNumber");
        String projectName = (product == null) ? "API_Test" : product;
        String reportName = (buildNum == null) ? formatter.format(LocalDateTime.now()) : buildNum;

        // project name
        extentx.config().setProjectName(projectName);
        // report or build name
        extentx.config().setReportName(reportName);

        // server URL
        // ! must provide this to be able to upload snapshots
        String url = host + ":" + port;
        if (!url.isEmpty()) {
            extentx.config().setServerUrl(url);
        }

        return extentx;
    }
}
