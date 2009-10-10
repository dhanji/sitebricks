package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.example.HelloWorld;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class HelloWorldPage {

    private WebDriver driver;

    public HelloWorldPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean hasHelloWorldMessage() {
        //TODO ugh! stupid xpath doesn't work =(
        return driver.getPageSource().contains(HelloWorld.HELLO_MSG);
    }

    public static HelloWorldPage open(WebDriver driver) {
        driver.get(AcceptanceTest.BASE_URL + "/hello");
        return PageFactory.initElements(driver, HelloWorldPage.class);
    }
}