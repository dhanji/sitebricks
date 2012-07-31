package com.google.sitebricks.acceptance.page;


import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.example.SitebricksConfig;

public class ConversionPage {
	private WebDriver driver;

	public ConversionPage(WebDriver driver) {
		this.driver = driver;
	}
	
	public boolean hasDate(Date date)	{
		SimpleDateFormat sdf = new SimpleDateFormat(SitebricksConfig.DEFAULT_DATE_TIME_FORMAT);
		String target = sdf.format(date);

	    return driver.findElement(By.id("boundDate"))
	        .getText()
	        .contains(target);
	}

	public boolean hasCalendar(Calendar calendar)	{
		SimpleDateFormat sdf = new SimpleDateFormat(SitebricksConfig.DEFAULT_DATE_TIME_FORMAT);
		String target = sdf.format(calendar.getTime());

		String node = driver.getPageSource();
	    return  driver.findElement(By.id("boundCalendar"))
	    	.getText()
	        .contains(target);
	}

	public boolean hasMessage(String message)	{
	    return driver.findElement(By.id("boundText"))
	        .getText()
	        .contains(message);
	}

	public boolean hasDouble(Double dbl)	{
	    return driver.findElement(By.id("boundDouble"))
	        .getText()
	        .contains(dbl.toString());
	}
	
	public static ConversionPage open(WebDriver driver, Date date, Calendar calendar, String dateFormat, String msg, Double dbl) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		StringBuilder sb = new StringBuilder ();
		
		if (date != null)	{
			if (sb.length() > 0)
				sb.append("&");
			sb.append ("date=").append(encode(sdf.format(date)));
		}
		
		if (calendar != null)	{
			if (sb.length() > 0)
				sb.append("&");
			sb.append ("calendar=").append(encode(sdf.format(calendar.getTime())));
		}
		
		if (msg != null)	{
			if (sb.length() > 0)
				sb.append("&");
			sb.append ("message=").append(encode(msg));
		}

		if (msg != null)	{
			if (sb.length() > 0)
				sb.append("&");
			sb.append ("dbl=").append(encode(dbl.toString()));
		}
			
		sb.insert(0,"/conversion?").insert(0, AcceptanceTest.baseUrl());
		driver.get(sb.toString());
		return PageFactory.initElements(driver, ConversionPage.class);
	}

	private static String encode(String s){
		try	{
			return URLEncoder.encode(s,"UTF-8");
		}
		catch(Exception e){
			return s;
		}
	}
}
