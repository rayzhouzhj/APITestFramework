package com.rayzhouzhj.framework.report;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.testng.IInvokedMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.rayzhouzhj.framework.annotations.Author;

/**
 * ReportManager - Handles all Reporting activities e.g communication with ExtentManager, etc
 */
public class ReportManager 
{
	private static ReportManager manager = new ReportManager();
	public ThreadLocal<ExtentTest> parentTestClass = new ThreadLocal<>();
	public ThreadLocal<ExtentTest> currentTestMethod = new ThreadLocal<>();
	public ThreadLocal<ITestResult> testResult = new ThreadLocal<>();

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

	public ExtentTest createParentNodeExtent(String className, String classDescription) throws Exception 
	{
		ExtentTest parent = ExtentTestManager.createTest(className, classDescription);
		parentTestClass.set(parent);

		return parent;
	}

	public void setAuthorName(IInvokedMethod invokedMethod) throws Exception 
	{
		String authorName;
		String dataProvider = null;
		ArrayList<String> listeners = new ArrayList<>();
		Method method = invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
		String description = method.getAnnotation(Test.class).description();
		Object dataParameter = invokedMethod.getTestResult().getParameters();

		if (((Object[]) dataParameter).length > 0)
		{
			dataProvider = (String) ((Object[]) dataParameter)[0];
		}


		ExtentTestDescription methodDescription = new ExtentTestDescription(invokedMethod, description);
		boolean authorNamePresent = methodDescription.isAuthorNamePresent();
		String descriptionMethodName = methodDescription.getDescriptionMethodName();
		String category = invokedMethod.getTestMethod().getXmlTest().getParameter("browser");

		String testName = dataProvider == null ? descriptionMethodName : descriptionMethodName + "[" + dataProvider + "]";
		if (authorNamePresent)
		{
			authorName = method.getAnnotation(Author.class).name();
			Collections.addAll(listeners, authorName.split("\\s*,\\s*"));
			ExtentTest child = parentTestClass.get().createNode(testName, category).assignAuthor(String.valueOf(listeners));
			child.assignCategory(category);
			currentTestMethod.set(child);
		} 
		else 
		{
			ExtentTest child = parentTestClass.get().createNode(testName, category);
			child.assignCategory(category);
			currentTestMethod.set(child);
		}
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
