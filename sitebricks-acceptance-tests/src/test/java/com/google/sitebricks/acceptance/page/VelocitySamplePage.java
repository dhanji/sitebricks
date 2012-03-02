package com.google.sitebricks.acceptance.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.example.VelocitySample;

public class VelocitySamplePage {
    private final WebDriver driver;

    public VelocitySamplePage(WebDriver driver) {
        this.driver = driver;
    }

    public static VelocitySamplePage open(WebDriver driver, String url) {
        driver.get(AcceptanceTest.BASE_URL + url);
        return PageFactory.initElements(driver, VelocitySamplePage.class);
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public boolean hasMessage() {
        System.out.println(driver.getPageSource());
        return driver.getPageSource().contains(VelocitySample.MSG);
    }

    public String getContent() {
        return driver.getPageSource();
    }
}
