# APITestFramework

API test framework with ExtentReport + TestNG

[![](https://jitpack.io/v/rayzhouzhj/APITestFramework.svg)](https://jitpack.io/#rayzhouzhj/APITestFramework)

## Description:
Test framework for Web testing integrated with TestNG and Extent Report.

### Test Entry Point [WebRunner.java](https://github.com/scmp-contributor/WebTestFramework/blob/master/src/test/java/com/github/test/demo/WebRunner.java)
```java
public class APIRunner {
	@Test
	public static void testAPI() throws Exception {
		List<String> testPackages = new ArrayList<>();
		testPackages.add("com.rayzhouzhj.test.demo1");
		testPackages.add("com.rayzhouzhj.test");
		TestExecutor executor = new TestExecutor(testPackages);

		boolean hasFailures = executor.execute();

		Assert.assertFalse(hasFailures, "Testcases execution failed.");
	}
}

```

### How To Start The Test
```bash
URL=<your testing url> mvn clean test -Dtest=APIRunner
# To override the configs from config.properties, e.g. overriding INCLUDE_GROUPS
BASE_URL=<your testing url> INCLUDE_GROUPS=<your runtime include groups> mvn clean test -Dtest=APIRunner
```

#### Config below properties to setup the test framework([config.properties](https://github.com/rayzhouzhj/APITestFramework/blob/master/config.properties)):
```properties
####################### FRAMEWORK ###########################################
THREAD_COUNT=3
DATAPROVIDER_THREAD_COUNT=2
MAX_RETRY_COUNT=0

######################## TEST ###############################################
INCLUDE_GROUPS=test
EXCLUDE_GROUPS=groups_to_exclude
BASE_URL=<your api base url>
```
## Changelog
*1.1.3*
- **[ENHANCEMENTS]**
    - Enhance framework structure
    - Added logback for logging
- **[DEPENDENCY UPDATES]**
    - added `JSONassert`1.5.1 (custom library)
    - added `lombok` 1.18.16
    - added `slf4j-api` 1.7.30
    - added `logback-classic` 1.2.3