package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Martins Barinskis (martins.barinskis@gmail.com)
 */
public class EmbeddedRepeatPage {

    private final List<WebElement> elements;

    public EmbeddedRepeatPage(WebDriver driver) {
        elements = driver.findElements(By.xpath("//li[@class='item']"));
    }

    public List<String> getItems() {
        List<String> items = new ArrayList<String>();
        for (WebElement e : elements) {
            items.add(e.getText());
        }
        return items;
    }

    public static EmbeddedRepeatPage open(WebDriver driver) {
        driver.get(AcceptanceTest.baseUrl() + "/embedded-repeat");
        return PageFactory.initElements(driver, EmbeddedRepeatPage.class);
    }

}
