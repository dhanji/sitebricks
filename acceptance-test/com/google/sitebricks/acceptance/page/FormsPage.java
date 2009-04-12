package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class FormsPage {

    private WebDriver driver;

    public FormsPage(WebDriver driver) {
        this.driver = driver;
    }

    public void enterText(String text) {
        driver.findElement(By.name("text"))
                .sendKeys(text);

        driver.findElement(By.id("send"))
                .submit();
    }

    public boolean hasBoundText(String someText) {
        return driver.findElement(By.xpath("//div[@class='entry']/p"))
                .getText()
                .contains(someText);
    }

    public static FormsPage open(WebDriver driver) {
        driver.get(AcceptanceTest.BASE_URL + "/forms");
        return PageFactory.initElements(driver, FormsPage.class);
    }
}