package com.google.sitebricks.selenium;

import com.thoughtworks.selenium.*;
import org.testng.annotations.*;
import static org.testng.Assert.*;
import java.util.regex.Pattern;

public class IntegrationTest extends SeleneseTestNgHelper {
	@Test public void testIntegration() throws Exception {
		verifyTrue(selenium.isTextPresent("Hello from Sitebricks!"));
		selenium.click("link=/google-sitebricks-tutorial ---> org.mortbay.jetty.plugin.Jetty6PluginWebAppContext@1bc6533{/google-sitebricks-tutorial,C:\\projects\\google-sitebricks\\google-sitebricks-tutorial\\src\\main\\webapp}");
		selenium.waitForPageToLoad("30000");
	}
}
