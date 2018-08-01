package com.rayzhouzhj.test.runner;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.rayzhouzhj.framework.executor.TestExecutor;

public class APIRunner 
{
    @Test
    public static void testApp() throws Exception 
    {
    	List<String> testPackages = new ArrayList<>();
    	testPackages.add("com.rayzhouzhj.test.demo1");
    	testPackages.add("com.rayzhouzhj.test");
        TestExecutor executor = new TestExecutor(testPackages);
        
        boolean hasFailures = executor.execute();
        
        Assert.assertFalse(hasFailures, "Testcases execution failed.");
    }
}
