package com.rayzhouzhj.framework.report;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.rayzhouzhj.framework.testng.model.TestInfo;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.rayzhouzhj.framework.annotations.Authors;
import com.rayzhouzhj.framework.annotations.Categories;

/**
 * ReportManager - Handles all Reporting activities e.g communication with ExtentManager, etc
 */
public class ReportManager 
{
	private static ReportManager manager = new ReportManager();
	public ThreadLocal<ExtentTest> parentTestClass = new ThreadLocal<>();
	public ThreadLocal<ExtentTest> currentTestMethod = new ThreadLocal<>();
	public ThreadLocal<ITestResult> testResult = new ThreadLocal<>();
	private ThreadLocal<TestInfo> testInfo = new ThreadLocal<>();
	private ConcurrentHashMap<String, Boolean> retryMap = new ConcurrentHashMap<>();

	public static ReportManager getInstance()
	{
		return manager;
	}

	private ReportManager() 
	{
	}

	public void removeTest()
	{
		ExtentTestManager.removeTest(currentTestMethod.get());

	}

	public boolean isRetryMethod(String methodName, String className)
	{
		String key = className + ":" + methodName + Thread.currentThread().getId();
		if(!this.retryMap.containsKey(key))
		{
			this.retryMap.put(key, false);
		}

		return this.retryMap.get(key);
	}

	public void setMethodRetryStatus(String methodName, String className, boolean status)
	{
		String key = className + ":" + methodName + Thread.currentThread().getId();
		this.retryMap.put(key, status);
	}

	public void endLogTestResults(ITestResult result) throws IOException, InterruptedException 
	{
		if(result.isSuccess())
		{
			currentTestMethod.get().log(Status.PASS, "Test Passed: " + result.getMethod().getMethodName());
		}
		else
		{
			if (result.getStatus() == ITestResult.FAILURE) 
			{

				// Print exception stack trace if any
				Throwable throwable = result.getThrowable();
				if(throwable != null)
				{
					throwable.printStackTrace();
					currentTestMethod.get().log(Status.FAIL, "<pre>" + result.getThrowable().getMessage() + "</pre>");
				}

			}
		}

		/*
		 * Skip block
		 */
		if (result.getStatus() == ITestResult.SKIP) 
		{
			currentTestMethod.get().log(Status.SKIP, "Test skipped");
		}

		ExtentManager.getExtent().flush();
	}

	public void setTestResult(ITestResult testResult)
	{
		this.testResult.set(testResult);
	}

	public void addTest(String className, String classDescription, IInvokedMethod invokedMethod) throws Exception 
	{
		ExtentTest parent = ExtentTestManager.createTest(className, classDescription);
		parentTestClass.set(parent);

		addTestMethod(invokedMethod);
	}

	private void addTestMethod(IInvokedMethod invokedMethod) throws Exception 
	{
		String dataProviderVars = null;
		Method testMethod = invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
		String testDescription = testMethod.getAnnotation(Test.class).description();
		String testName = "";
		
		// Add description to test name
		if (testDescription.isEmpty())
		{
			testName = invokedMethod.getTestMethod().getMethodName();
		} 
		else 
		{
			testName = invokedMethod.getTestMethod().getMethodName() + "[" + testDescription + "]";
		}
		
		
		// Add parameters to test name
		Object[] testVars = (Object[]) invokedMethod.getTestResult().getParameters();
		if (testVars.length > 0)
		{
			dataProviderVars = "";
			for(int i = 0; i < testVars.length; i++)
			{
				dataProviderVars = dataProviderVars.isEmpty()?  "" + testVars[i] : (dataProviderVars + " " + testVars[i]);
			}
			
			testName =  testName + "[" + dataProviderVars + "]";
		}

		
		ExtentTest child = null;
		if (testMethod.isAnnotationPresent(Authors.class))
		{
			String[] authors = testMethod.getAnnotation(Authors.class).name();
			child = parentTestClass.get().createNode(testName, testDescription).assignAuthor(String.valueOf(authors));
		} 
		else 
		{
			child = parentTestClass.get().createNode(testName, testDescription);
		}

		// Assign Categories
		child.assignCategory(invokedMethod.getTestMethod().getGroups());

		currentTestMethod.set(child);
	}

	public void logInfo(String message)
	{
		this.currentTestMethod.get().log(Status.INFO, message);
	}

	public void logPass(String message)
	{
		this.currentTestMethod.get().log(Status.PASS, message);
	}

	public void logFail(String message)
	{
		try 
		{
			this.currentTestMethod.get().log(Status.FAIL, message);
			this.testResult.get().setStatus(ITestResult.FAILURE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
