package com.rayzhouzhj.framework.executor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.rayzhouzhj.framework.utils.ConfigFileKeys;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import com.rayzhouzhj.framework.context.RunTimeContext;
import com.rayzhouzhj.framework.utils.Figlet;


public class TestExecutor {
    private static final Logger frameworkLogger = LoggerFactory.getLogger(TestExecutor.class);
    private final RunTimeContext context;
    private List<String> packages;
    private List<String> groupsInclude = new ArrayList<>();
    private List<String> groupsExclude = new ArrayList<>();

    /**
     * Constructor with package list
     *
     * @param packages TestCases under the package and sub-packages will be lookup and executed
     * @throws IOException
     */
    public TestExecutor(List<String> packages) throws IOException {
        this.packages = packages;
        context = RunTimeContext.getInstance();
        initGroups();
    }

    private void initGroups() {
        if (context.getProperty(ConfigFileKeys.INCLUDE_GROUPS) != null) {
            Collections.addAll(groupsInclude, context.getProperty(ConfigFileKeys.INCLUDE_GROUPS).split("\\s*,\\s*"));
        }

        if (context.getProperty(ConfigFileKeys.EXCLUDE_GROUPS) != null) {
            Collections.addAll(groupsExclude, context.getProperty(ConfigFileKeys.EXCLUDE_GROUPS).split("\\s*,\\s*"));
        }
    }

    /**
     * Execute test
     *
     * @return execution status: true for pass, false for failed
     * @throws Exception
     */
    public boolean execute() throws Exception {
        URL testClassUrl = null;
        List<URL> testClassUrls = new ArrayList<>();
        String testClassPackagePath = "file:" + System.getProperty("user.dir") + File.separator + "target" + File.separator + "test-classes" + File.separator;

        // Add URL for each test package
        for (int i = 0; i < packages.size(); i++) {
            testClassUrl = new URL(testClassPackagePath + packages.get(i).replaceAll("\\.", "/"));
            testClassUrls.add(testClassUrl);
        }

        // Find test class by annotation: org.testng.annotations.Test.class
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(testClassUrls).setScanners(new MethodAnnotationsScanner()));
        Set<Method> resources = reflections.getMethodsAnnotatedWith(org.testng.annotations.Test.class);

        Map<String, List<Method>> classMethodMap = createTestsMap(resources);

        ExecutorService executor = Executors.newCachedThreadPool();
        List<FutureTask<Boolean>> list = new ArrayList<>();

        XmlSuite suite = constructXmlSuite(classMethodMap);
        String suiteFile = writeTestNGFile(suite, "api_testsuite");

        FutureTask<Boolean> futureTask = new FutureTask<>(new TestExecutorService(suiteFile));
        list.add(futureTask);

        executor.submit(futureTask);

        // Wait for the test completion
        while (true) {
            boolean isDone = true;
            for (FutureTask<Boolean> task : list) {
                isDone = isDone && task.isDone();
            }

            if (isDone) {
                // Shutdown executor service
                executor.shutdown();
                break;
            } else {
                TimeUnit.SECONDS.sleep(1);
            }
        }

        boolean hasFailure = false;
        //  Get the result
        for (FutureTask<Boolean> result : list) {
            hasFailure = hasFailure || result.get();
        }


        Figlet.print("Test Completed");

        return hasFailure;
    }

    private XmlSuite constructXmlSuite(Map<String, List<Method>> classMethodMap) {
        // Initialize XML Suite
        XmlSuite suite = new XmlSuite();
        suite.setName("Test Suite");
        suite.setPreserveOrder(true);

        /*
         *  Set parallel mode to METHODS level
         *  Each method will be taken care of by 1 thread
         */
        suite.setThreadCount(Integer.parseInt(context.getProperty(ConfigFileKeys.THREAD_COUNT)));
        suite.setDataProviderThreadCount(Integer.parseInt(context.getProperty(ConfigFileKeys.DATAPROVIDER_THREAD_COUNT)));
        suite.setParallel(ParallelMode.METHODS);
        suite.setVerbose(2);

        // Add listeners
        ArrayList<String> listeners = new ArrayList<>();
        listeners.add(com.rayzhouzhj.framework.testng.listeners.InvokedMethodListener.class.getName());
        listeners.add(com.rayzhouzhj.framework.testng.listeners.RetryListener.class.getName());
        suite.setListeners(listeners);

        // Initialize the XML Test Suite
        XmlTest test = new XmlTest(suite);
        test.setName("API Test");
        test.setIncludedGroups(groupsInclude);
        test.setExcludedGroups(groupsExclude);

        // Add test class and methods
        List<XmlClass> xmlClasses = new ArrayList<>();
        for (String className : classMethodMap.keySet()) {
            XmlClass clazz = new XmlClass();
            clazz.setName(className);

            // Add include methods
            List<XmlInclude> includeMethods = new ArrayList<>();
            for (Method method : classMethodMap.get(className)) {
                XmlInclude includeMethod = new XmlInclude(method.getName());
                includeMethods.add(includeMethod);
            }

            clazz.setIncludedMethods(includeMethods);
            xmlClasses.add(clazz);
        }

        test.setXmlClasses(xmlClasses);

        return suite;
    }

    private String writeTestNGFile(XmlSuite suite, String fileName) {
        // Print out Suite XML
        System.out.println(suite.toXml());
        String suiteXML = System.getProperty("user.dir") + "/target/" + fileName + ".xml";

        try {
            FileWriter writer = new FileWriter(suiteXML);
            writer.write(suite.toXml());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return suiteXML;
    }

    private Map<String, List<Method>> createTestsMap(Set<Method> methods) {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        final String runnerClass = stElements[3].getClassName();
        Map<String, List<Method>> testsMap = new HashMap<>();
        methods.stream().forEach(method ->
        {
            String className = method.getDeclaringClass().getPackage().getName() + "." + method.getDeclaringClass().getSimpleName();

            // Skip runner class
            if (runnerClass.equals(className)) {
                return;
            }

            // Get method list from specific test class
            List<Method> methodsList = testsMap.get(className);

            // If the method list is empty, initialize it and add it to test class map
            if (methodsList == null) {
                methodsList = new ArrayList<>();
                testsMap.put(className, methodsList);
            }

            // If current method is duplicated
            if (methodsList.contains(method)) {
                return;
            }

            // Skip the method with the exclude groups
            // Added this filter because TestNG sometimes does not filter the exclude correctly
            if (method.isAnnotationPresent(Test.class)) {
                boolean isIncluded = false;
                Test test = method.getAnnotation(Test.class);
                String[] groups = test.groups();
                for (String group : groups) {
                    if (this.groupsExclude.contains(group)) {
                        // If no test method is included for the test class
                        if (methodsList.isEmpty()) {
                            // Remove test class from test map
                            testsMap.remove(className);
                        }

                        return;
                    }

                    // If include groups are not specified or current test group is in the include groups
                    if (this.groupsInclude.size() == 0 || this.groupsInclude.contains(group)) {
                        isIncluded = true;
                    }
                }

                // Add method to list
                if (isIncluded) {
                    methodsList.add(method);
                } else {
                    // If no test method is included for the test class
                    if (methodsList.isEmpty()) {
                        // Remove test class from test map
                        testsMap.remove(className);
                    }
                }
            }
        });

        return testsMap;
    }

}

class TestExecutorService implements Callable<Boolean> {
    private String suite;

    public TestExecutorService(String file) {
        suite = file;
    }

    @Override
    public Boolean call() throws Exception {
        List<String> suiteFiles = new ArrayList<>();
        suiteFiles.add(suite);

        TestNG testNG = new TestNG();
        testNG.setTestSuites(suiteFiles);
        testNG.run();

        return testNG.hasFailure();
    }

}
