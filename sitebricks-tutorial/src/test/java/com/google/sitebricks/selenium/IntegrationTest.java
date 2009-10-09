package com.google.sitebricks.selenium;

import com.thoughtworks.selenium.SeleneseTestNgHelper;

public class IntegrationTest extends SeleneseTestNgHelper {
  public void testIntegration() throws Exception {
		verifyTrue(selenium.isTextPresent("Hello from Sitebricks!"));
		selenium.click("link=/google-sitebricks-tutorial ---> org.mortbay.jetty.plugin.Jetty6PluginWebAppContext@1bc6533{/google-sitebricks-tutorial,C:\\projects\\google-sitebricks\\google-sitebricks-tutorial\\src\\main\\webapp}");
		selenium.waitForPageToLoad("30000");
	}
}
