package com.rayzhouzhj.test.demo2;

import org.testng.annotations.Test;

import com.rayzhouzhj.framework.test.utils.TestLogger;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.*;

public class APITest2 
{

	TestLogger logger = new TestLogger();
	
	@Test(groups={"test"})
	public void testAPI3() throws InterruptedException
	{
		
		Response resp = RestAssured.given().
				accept("application/json")
				.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36")
				.when()
				.get("https://api.github.com/users/rayzhouzhj");
		
		logger.logJson(resp, "json response");
		
		resp.then()
		.statusCode(200)
        .body("name", equalTo("Ray Zhou"));
	}
	
	@Test(groups={"test", "groups_to_exclude"})
	public void testAPI4() throws InterruptedException
	{
		
		Response resp = RestAssured.given().
				accept("application/json")
				.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36")
				.when()
				.get("https://api.github.com/users/rayzhouzhj");
		
		logger.logJson(resp, "json response");
		
		resp.then()
		.statusCode(200)
        .body("name", equalTo("Ray Zhou"));
	}
}
