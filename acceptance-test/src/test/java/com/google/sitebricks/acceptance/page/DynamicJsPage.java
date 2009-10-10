package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.example.DynamicJs;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class DynamicJsPage {

    private WebDriver driver;

    public DynamicJsPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean hasDynamicText() {
        return driver.getPageSource().contains(DynamicJs.A_MESSAGE);
    }

    public static DynamicJsPage open(WebDriver driver) {
        driver.get(AcceptanceTest.BASE_URL + "/dynamic.js");
        return PageFactory.initElements(driver, DynamicJsPage.class);
    }
}