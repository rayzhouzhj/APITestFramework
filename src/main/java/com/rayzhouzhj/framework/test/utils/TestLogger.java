package com.rayzhouzhj.framework.test.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.testng.Assert;

import com.rayzhouzhj.framework.context.RunTimeContext;
import com.rayzhouzhj.framework.report.ReportManager;

import io.restassured.response.Response;

public class TestLogger 
{
	public void logJson(Response resp)
	{
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();	    
		String filePath = RunTimeContext.getInstance().getLogPath("json", stElements[2].getClassName(), stElements[2].getMethodName());
		filePath = filePath + File.separator + RunTimeContext.currentDateAndTime() + ".json";
		
		FileWriter fw;
		try
		{
			fw = new FileWriter(new File(filePath));
			fw.write(resp.prettyPrint());
			fw.flush();
			
			ReportManager.getInstance().logInfo("<a target='_blank' href='" + filePath + "'> JSON Response </a>");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void logInfo(String message)
	{
		System.out.println("[INFO] " + message);
		ReportManager.getInstance().logInfo(message);
	}
	
	public void logPass(String message)
	{
		System.out.println("[PASSED] " + message);
		ReportManager.getInstance().logPass(message);
	}
	
	public void logFail(String message)
	{
		System.err.println("[FAILED] " + message);
		ReportManager.getInstance().logFail(message);
	}
	
	public void logFatalError(String message)
	{
		System.err.println("[ERROR] " + message);
		Assert.fail(message);
	}
}
