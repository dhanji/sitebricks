package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.By;
import org.openqa.selenium.support.How;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.ArrayList;
import java.util.List;

public class JspValidatingPage {
  @FindBy(how = How.XPATH, using = "//div[@class='errors'][1]/ul")
  private WebElement constraintViolations;

  private WebDriver driver;

  public JspValidatingPage(WebDriver driver) {
    this.driver = driver;
  }

  public List<String> getValidationViolations() {
    List<String> items = new ArrayList<String>();
    for (WebElement li : constraintViolations.findElements(By.tagName("li"))) {
      items.add(li.getText().trim());
    }
    return items;
  }

  public static JspValidatingPage open(WebDriver driver) {
    driver.get(AcceptanceTest.baseUrl() + "/jspvalidating");
    driver.findElement(By.id("submit")).click();
    return PageFactory.initElements(driver, JspValidatingPage.class);
  }

}
