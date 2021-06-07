package com.rayzhouzhj.framework.testng.listeners;

import java.lang.reflect.Method;

import com.rayzhouzhj.framework.context.RunTimeContext;
import com.rayzhouzhj.framework.testng.model.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.rayzhouzhj.framework.annotations.ClassDescription;
import com.rayzhouzhj.framework.report.ExtentManager;
import com.rayzhouzhj.framework.report.ReportManager;

import static com.rayzhouzhj.framework.utils.constants.TEST_INFO_OBJECT;

public final class InvokedMethodListener implements IInvokedMethodListener {
    private static final Logger frameworkLogger = LoggerFactory.getLogger(InvokedMethodListener.class);
    public InvokedMethodListener() throws Exception {

    }

    private void resetReporter(IInvokedMethod method, ITestResult testResult) {
        Method refMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
        String className = refMethod.getDeclaringClass().getSimpleName();

        // Create test node for test class in test report
        try {
            String testDescription = "";
            if (testResult.getTestClass().getClass().isAnnotationPresent(ClassDescription.class)) {
                testDescription = getClass().getAnnotation(ClassDescription.class).value();
            }

            // Create test
            ReportManager.getInstance().addTest(className, testDescription, method);
            ReportManager.getInstance().setTestResult(testResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Before each method invocation
     * Initialize Report Manager
     */
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        // Clear all runtime variables
        RunTimeContext.getInstance().clearRunTimeVariables();

        TestInfo testInfo = new TestInfo(method, testResult);
        // Save TestInfo to runtime memory
        RunTimeContext.getInstance().setTestLevelVariables(TEST_INFO_OBJECT, testInfo);

        // Skip beforeInvocation if current method is not with Annotation Test, or
        // Current Test need to be skipped
        if (!testInfo.isTestMethod()) {
            throw new SkipException("Skipped Test - " + testInfo.getTestName());
        }

        frameworkLogger.info("Start running test [" + testInfo.getMethodName() + "]");
        resetReporter(method, testResult);

    }

    /**
     * After each method invocation
     * Update test result to report manager
     */
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        TestInfo testInfo = (TestInfo) RunTimeContext.getInstance().getTestLevelVariables(TEST_INFO_OBJECT);
        // Skip beforeInvocation if current method is not with Annotation Test, or
        // Current Test need to be skipped
        if (!testInfo.isTestMethod()) {
            return;
        }

        Method refMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
        String methodName = refMethod.getName();

        frameworkLogger.info("Completed running test [" + methodName + "]");

        try {
            if (testResult.getStatus() == ITestResult.SUCCESS || testResult.getStatus() == ITestResult.FAILURE) {
                ReportManager.getInstance().endLogTestResults(testResult);
                ExtentManager.getExtent().flush();
            } else if (testResult.getStatus() == ITestResult.SKIP) {
                ExtentManager.getExtent().flush();

                // Remove previous log data for retry test
                ReportManager.getInstance().removeTest();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
