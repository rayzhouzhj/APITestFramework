package com.rayzhouzhj.test.demo1;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.rayzhouzhj.framework.annotations.Categories;

import io.restassured.path.json.JsonPath;

public class DataProviderTest {

	@Test(groups={"test", "DataProvider"}, dataProvider="provideData")
	@Categories(values={"DataProvider", "Demo"})
	public void testDataProvider(String user, String email) throws InterruptedException
	{
		Thread.sleep(5000);
		System.out.println("User: " + user + " email: " + email);
	}
	
	@DataProvider(parallel=true)
	public Object[][] provideData() throws Exception
	{
		JsonPath data = JsonPath.from(new File("./data/testdata.json"));
		List<Map<String, String>> datalist = data.getList("api_data");
		
		String[][] dataArray = new String[datalist.size()][];
		for(int i = 0; i < dataArray.length; i++)
		{
			dataArray[i] = new String[]{datalist.get(i).get("user"), datalist.get(i).get("email")};
		}
		
		return dataArray;
	}
}
