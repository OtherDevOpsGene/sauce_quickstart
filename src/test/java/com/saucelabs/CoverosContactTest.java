package com.saucelabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.ConcurrentParameterized;
import com.saucelabs.junit.SauceOnDemandTestWatcher;

/**
 * Demonstrates how to write a JUnit test that runs tests against Sauce Labs
 * using multiple browsers in parallel.
 * <p/>
 * The test also includes the {@link SauceOnDemandTestWatcher} which will invoke
 * the Sauce REST API to mark the test as passed or failed.
 * 
 * @author Ross Rowe
 */
@RunWith(ConcurrentParameterized.class)
public class CoverosContactTest implements SauceOnDemandSessionIdProvider {

	/**
	 * URL for a starting point.
	 */
	public String baseUrl = "https://www.coveros.com/";

	/**
	 * Constructs a {@link SauceOnDemandAuthentication} instance using the
	 * supplied user name/access key. To use the authentication supplied by
	 * environment variables or from an external file, use the no-arg
	 * {@link SauceOnDemandAuthentication} constructor.
	 */
	public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication();

	/**
	 * JUnit Rule which will mark the Sauce Job as passed/failed when the test
	 * succeeds or fails.
	 */
	@Rule
	public SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(
			this, authentication);

	/**
	 * Represents the browser to be used as part of the test run.
	 */
	private String browser;
	/**
	 * Represents the operating system to be used as part of the test run.
	 */
	private String os;
	/**
	 * Represents the version of the browser to be used as part of the test run.
	 */
	private String version;
	/**
	 * Instance variable which contains the Sauce Job Id.
	 */
	private String sessionId;

	/**
	 * The {@link WebDriver} instance which is used to perform browser
	 * interactions with.
	 */
	private WebDriver driver;

	/**
	 * Constructs a new instance of the test. The constructor requires three
	 * string parameters, which represent the operating system, version and
	 * browser to be used when launching a Sauce VM. The order of the parameters
	 * should be the same as that of the elements within the
	 * {@link #browsersStrings()} method.
	 * 
	 * @param os
	 * @param version
	 * @param browser
	 */
	public CoverosContactTest(String os, String version, String browser) {
		super();
		this.os = os;
		this.version = version;
		this.browser = browser;
	}

	/**
	 * @return a LinkedList containing String arrays representing the browser
	 *         combinations the test should be run against. The values in the
	 *         String array are used as part of the invocation of the test
	 *         constructor
	 */
	@ConcurrentParameterized.Parameters
	public static LinkedList<String[]> browsersStrings() {
		LinkedList<String[]> browsers = new LinkedList<String[]>();
		browsers.add(new String[] { "Windows 8.1", "38.0", "firefox" });
		//browsers.add(new String[] { "OSX 10.8", "6", "safari" });
		return browsers;
	}

	/**
	 * Constructs a new {@link RemoteWebDriver} instance which is configured to
	 * use the capabilities defined by the {@link #browser}, {@link #version}
	 * and {@link #os} instance variables, and which is configured to run
	 * against ondemand.saucelabs.com, using the username and access key
	 * populated by the {@link #authentication} instance.
	 * 
	 * @throws Exception
	 *             if an error occurs during the creation of the
	 *             {@link RemoteWebDriver} instance.
	 */
	@Before
	public void setUp() throws Exception {

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);
		if (version != null) {
			capabilities.setCapability(CapabilityType.VERSION, version);
		}
		capabilities.setCapability(CapabilityType.PLATFORM, os);
		capabilities.setCapability("name", "Coveros Contact Form Test");
		this.driver = new RemoteWebDriver(new URL("http://"
				+ authentication.getUsername() + ":"
				+ authentication.getAccessKey()
				+ "@ondemand.saucelabs.com:80/wd/hub"), capabilities);
		this.sessionId = (((RemoteWebDriver) driver).getSessionId()).toString();

	}

	/*
	 * As exported by the Selenium IDE.
	 */
	private boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	/*
	 * As exported by the Selenium IDE.
	 */
	@Test
	public void testCoverosContact() throws Exception {
		driver.get(baseUrl + "/");
		driver.findElement(By.xpath("(//a[contains(text(),'Research')])[2]"))
				.click();
		driver.findElement(By.xpath("(//a[contains(text(),'Contact')])[2]"))
				.click();
		driver.findElement(By.id("gumm-contact-name-529df396718f6")).clear();
		driver.findElement(By.id("gumm-contact-name-529df396718f6")).sendKeys(
				"Gene Gotimer");
		driver.findElement(By.id("gumm-contact-email-529df396718f6")).clear();
		driver.findElement(By.id("gumm-contact-email-529df396718f6")).sendKeys(
				"gene.gotimer@coveros.com");
		driver.findElement(By.id("gumm-contact-message-529df396718f6")).clear();
		driver.findElement(By.id("gumm-contact-message-529df396718f6"))
				.sendKeys("Testing at StarCanada.");
		driver.findElement(By.cssSelector("input.gumm-contact-submit")).click();
		for (int second = 0;; second++) {
			if (second >= 60)
				fail("timeout");
			try {
				if (isElementPresent(By.cssSelector("p.email-sent")))
					break;
			} catch (Exception e) {
			}
			Thread.sleep(1000);
		}

		assertEquals("Your email has been sent.",
				driver.findElement(By.cssSelector("p.email-sent")).getText());
	}

	/**
	 * Closes the {@link WebDriver} session.
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		driver.quit();
	}

	/**
	 * 
	 * @return the value of the Sauce Job id.
	 */
	@Override
	public String getSessionId() {
		return sessionId;
	}
}
