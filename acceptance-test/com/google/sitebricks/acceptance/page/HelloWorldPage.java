package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class HelloWorldPage {
    static final String HELLO_WORLD_MSG = String.format("Message : %s",
        "Hello from warp-sitebricks!");

    private WebDriver driver;

    public HelloWorldPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean hasHelloWorldMessage() {
        //TODO ugh! stupid xpath doesn't work =(
        return driver.getPageSource().contains(HELLO_WORLD_MSG);
    }

    public static HelloWorldPage open(WebDriver driver) {
        driver.get(AcceptanceTest.BASE_URL + "/hello");
        return PageFactory.initElements(driver, HelloWorldPage.class);
    }
}