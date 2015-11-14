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

    private final List<WebElement> continents;

    private final List<WebElement> countries;

    public EmbeddedRepeatPage(WebDriver driver) {
        continents = driver.findElements(By.xpath("//li[@class='continent']/span[@class='title']"));

        countries = driver.findElements(By.xpath("//li[@class='country']"));
    }

    public List<String> getContinents() {
        return extractStrings(continents);
    }

    public List<String> getCountries() {
        return extractStrings(countries);
    }

    private static List<String> extractStrings(List<WebElement> continents1) {
        List<String> items = new ArrayList<String>();
        for (WebElement e : continents1) {
            items.add(e.getText());
        }
        return items;
    }

    public static EmbeddedRepeatPage open(WebDriver driver) {
        driver.get(AcceptanceTest.baseUrl() + "/embedded-repeat");
        return PageFactory.initElements(driver, EmbeddedRepeatPage.class);
    }

}
