package org.hiatusuk.selectorLint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class PagesTest {

    // OurWebDriverWrapper driver = new OurWebDriverWrapper(new FirefoxDriver());
    OurWebDriverWrapper driver = new OurWebDriverWrapper( new HtmlUnitDriver(true) );

    @Test
    public void testGmailPage() {
        driver.get(new File("src/test/resources/gmail.html").toURI().toString());
        testElement(driver, By.xpath("//*[@id=\":kj\"]/span"),
                  /* ==> */ By.cssSelector(""));

        // //*[@id=":ki"]/td[5]/div[1]/span
        // div[role='main'] table tr:nth-child(1) td:nth-child(5) div:nth-child(1) span
        // //div[@role='main']//table//tr[1]/td[5]/div[1]/span
        
    }

    @Test
    public void testHtml5Page() {
        driver.get(new File("src/test/resources/HTML5_test_page.html").toURI().toString());
        testElement(driver, By.cssSelector("#forms__action > p:nth-child(2) > input[type=\"submit\"]:nth-child(1)"),
                  /* ==> */ By.cssSelector("input[value='<input type=submit>']"));

        testElement(driver, By.xpath("//*[@id=\"forms__action\"]/p[1]/input[4]"),
                  /* ==> */ By.cssSelector("input[value='<input disabled>']"));

        testElement(driver, By.cssSelector("#forms__checkbox > ul > li:nth-child(1) > label"),
                  /* ==> */ By.cssSelector("label[for='checkbox1']"));

        testElement(driver, By.cssSelector("#select > optgroup > option:nth-child(2)"),
                  /* ==> */ By.xpath("//option[text()='Option Two']"));

        testElement(driver, By.cssSelector("#text__inline > div > p:nth-child(17) > var:nth-child(2)"),
                  /* ==> */ By.xpath("//var[text()='x']"));

        testElement(driver, By.cssSelector("#text__tables > table > tbody > tr:nth-child(3) > td:nth-child(4)"),
                  /* ==> */ By.xpath("//td[text()='Special Table Cell']"));

        testElement(driver, By.cssSelector("#text__headings > div > h2"),
                  /* ==> */ By.cssSelector("#text__headings h2"));
    }

    @Test
    public void testGithubPage() {
        // driver.get("https://the-internet.herokuapp.com/dynamic_content?with_content=static");
        driver.get(new File("src/test/resources/github.html").toURI().toString());

        testNoChange(driver, By.cssSelector("head"));
        testNoChange(driver, By.cssSelector("BODY"));
        testNoChange(driver, By.tagName("body"));
        testNoChange(driver, By.id("dashboard")); // Don't waste people's time recommending By.cssSelector("#dashboard")
        testNoChange(driver, By.cssSelector("#dashboard")); // Don't waste people's time recommending By.id("dashboard")

        testElement(driver, By.cssSelector("BODY.logged-in"), By.tagName("body"));

        testElement(driver, By.xpath("//body/div[2]/div/article"), By.tagName("article"));

        testNoChange(driver, By.id("okId"));
        testNoChange(driver, By.cssSelector("#okId"));
        testNoChange(driver, By.cssSelector("p#okId"));
        testNoChange(driver, By.xpath("//*[@id=\"okId\"]"));
        testNoChange(driver, By.xpath("//p[@id=\"okId\"]"));

        testElement(driver, By.id("id-23467233"), By.cssSelector("p[niceattr='niceValue']"));
        testElement(driver, By.cssSelector("p[id*='id-23467233']"), By.cssSelector("p[niceattr='niceValue']"));
        testElement(driver, By.id("id-ad4ad45e"), By.cssSelector("p[class='goodSemanticClass']"));

        // FIXME 'title' better than 'href'
        testElement(driver, By.cssSelector("#dashboard > div.news.column.two-thirds > div:nth-child(2) > div > div > div.title > a:nth-child(3)"),
                  /* ==> */ By.cssSelector("a[href='/cbeust/AnkoMaterialSamples']"), By.cssSelector("a[title='cbeust/AnkoMaterialSamples']"));

        testElement(driver, By.cssSelector("body > table > tbody > tr > td:nth-child(5) > span"),
                  /* ==> */ By.className("special"));

        testElement(driver, By.xpath("/html/body/table/tbody/tr/td[5]/span"),
                  /* ==> */ By.className("special"));

        testElement(driver, By.cssSelector("body > table > tbody > tr > td:nth-child(6) > span"),
                  /* ==> */ By.cssSelector("span[foo='bar']"));

        testElement(driver, By.xpath("/html/body/table/tbody/tr/td[6]/span"),
                  /* ==> */ By.cssSelector("span[foo='bar']"));

        testElement(driver, By.cssSelector("#hier > div > ul > li:nth-child(1)"),
                  /* ==> */ By.cssSelector("#hier li[data-attr='AA']"));
        testElement(driver, By.cssSelector("#hier > div > ul > li:nth-child(2)"),
                  /* ==> */ By.cssSelector("li[data-attr='MyItem']"));

        testElement(driver, By.cssSelector("#dashboard > div.news.column.two-thirds > div:nth-child(10) > div > div > div.title"),
                  /* ==> */ By.cssSelector("#dashboard div:nth-child(10) div.title"));
    }

    @AfterSuite
    public void shutDown() {
        driver.quit();
    }

    private void testNoChange( final OurWebDriverWrapper wd, final By by) {
        final WebElement original = wd.findElement(by);

        final List<By> newBys = wd.getImprovedSelector(original, by);
        // final List<By> o = Lists.newArrayList(by);

        // assertThat( newBys.toArray( new By[0] ), is( new By[]{by}) );
        assertThat(newBys, empty());
    }

    private void testElement( final OurWebDriverWrapper wd, final By by, By... expectedBys) {
        final WebElement original = wd.findElement(by);
        final List<By> expectations = Lists.newArrayList(expectedBys);

        assertThat( wd.getImprovedSelector(original, by), is(expectations));
    }
}
