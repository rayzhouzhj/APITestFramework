package com.rayzhouzhj.framework.testng.listeners;

import java.lang.reflect.Method;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import com.rayzhouzhj.framework.annotations.ClassDescription;
import com.rayzhouzhj.framework.report.ExtentManager;
import com.rayzhouzhj.framework.report.ReportManager;

public final class InvokedMethodListener implements IInvokedMethodListener
{
	public InvokedMethodListener() throws Exception 
	{

	}

	private void resetReporter(IInvokedMethod method, ITestResult testResult)
	{
		Method refMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
		String className = refMethod.getDeclaringClass().getSimpleName();

		// Create test node for test class in test report
		try 
		{
			String testDescription = "";
			if (testResult.getTestClass().getClass().isAnnotationPresent(ClassDescription.class)) 
			{
				testDescription = getClass().getAnnotation(ClassDescription.class).value();
			}

			// Create test
			ReportManager.getInstance().addTest(className, testDescription, method);
			ReportManager.getInstance().setTestResult(testResult);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * Before each method invocation
	 * Initialize Report Manager
	 */
	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) 
	{
		Method refMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
		String methodName = refMethod.getName();

		// Skip beforeInvocation if current method is not with Annotation Test
		if(refMethod.getAnnotation(Test.class) == null)
		{
			return;
		}

		System.out.println("[INFO] Start running test [" + methodName + "]");
		resetReporter(method, testResult);
		
	}

	/**
	 * After each method invocation
	 * Update test result to report manager
	 */
	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) 
	{
		Method refMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
		String methodName = refMethod.getName();

		System.out.println("[INFO] Completed running test [" + methodName + "]");

		// Skip afterInvocation if current method is not with Annotation Test
		if(refMethod.getAnnotation(Test.class) == null)
		{
			return;
		}

		try 
		{
			if (testResult.getStatus() == ITestResult.SUCCESS || testResult.getStatus() == ITestResult.FAILURE) 
			{
				ReportManager.getInstance().endLogTestResults(testResult);
				ExtentManager.getExtent().flush();
			}
			else if (testResult.getStatus() == ITestResult.SKIP) 
			{
				ExtentManager.getExtent().flush();

				// Remove previous log data for retry test
				ReportManager.getInstance().removeTest();
			}

		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}

	}
}
