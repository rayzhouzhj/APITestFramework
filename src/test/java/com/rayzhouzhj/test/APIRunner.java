package com.rayzhouzhj.test;

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
        TestExecutor parallelThread = new TestExecutor();
        List<String> tests = new ArrayList<>();
//        tests.add("APITest");
        boolean hasFailures = parallelThread.runner("com.rayzhouzhj.test", tests);
        
        Assert.assertFalse(hasFailures, "Testcases execution failed.");
    }
}
