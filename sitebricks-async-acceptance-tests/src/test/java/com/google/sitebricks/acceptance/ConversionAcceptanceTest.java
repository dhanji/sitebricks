package com.google.sitebricks.acceptance;

import java.util.Calendar;
import java.util.Date;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.google.sitebricks.acceptance.page.ConversionPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.example.SitebricksConfig;

@Test(suiteName = AcceptanceTest.SUITE)
public class ConversionAcceptanceTest {

	public void hasConvertedTypes()	{
		String inboundDateFormat = SitebricksConfig.DEFAULT_DATE_TIME_FORMAT;
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		String msg = "This is a test msg";
		Double dbl = 2.2;
		
	    WebDriver driver = AcceptanceTest.createWebDriver();
	    ConversionPage page = ConversionPage.open(driver, date, calendar, inboundDateFormat, msg, dbl);
	    
	    assert page.hasCalendar(calendar) : "Calendar not bound correctly";
	    assert page.hasDate(date) : "Date not bound correctly";
	    assert page.hasDouble(dbl) : "Double nto bound correctly";
	    assert page.hasMessage(msg) : "String not bound correctly";
	}
}
