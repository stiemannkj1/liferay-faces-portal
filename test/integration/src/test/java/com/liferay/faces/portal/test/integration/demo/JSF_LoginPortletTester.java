/**
 * Copyright (c) 2000-2017 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.faces.portal.test.integration.demo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.faces.portal.test.integration.PortalTestUtil;
import com.liferay.faces.test.selenium.browser.BrowserDriver;
import com.liferay.faces.test.selenium.browser.BrowserDriverManagingTesterBase;
import com.liferay.faces.test.selenium.browser.TestUtil;
import com.liferay.faces.test.selenium.browser.WaitingAsserter;


/**
 * @author  Vernon Singleton
 * @author  Philip White
 */
public class JSF_LoginPortletTester extends BrowserDriverManagingTesterBase {

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(JSF_LoginPortletTester.class);

	@After
	public void reset() {

		// Sign out and set up for the next test.
		signOut();
		super.doSetUp();
	}

	@Test
	public void runJSF_LoginPortletTester() {

		BrowserDriver browserDriver = getBrowserDriver();
		WaitingAsserter waitingAsserter = getWaitingAsserter();
		testJSF_Login(browserDriver, waitingAsserter, false);
		signOut();
		testJSF_Login(browserDriver, waitingAsserter, true);
	}

	@Before
	public void signOut() {

		BrowserDriver browserDriver = getBrowserDriver();
		browserDriver.navigateWindowTo(TestUtil.DEFAULT_BASE_URL + "/c/portal/logout");
		browserDriver.clearBrowserCookies();
	}

	public void testJSF_Login(BrowserDriver browserDriver, WaitingAsserter waitingAsserter, boolean testRememberMe) {

		// Navigate the browser to the portal page that contains the jsf-sign-in portlet.
		String jsfLoginPageURL = PortalTestUtil.getGuestPageURL("jsf-sign-in");
		browserDriver.navigateWindowTo(jsfLoginPageURL);
		String emailFieldXpath = "//input[contains(@id,':handle')]";
		browserDriver.waitForElementEnabled(emailFieldXpath);

		if (testRememberMe) {
			browserDriver.clickElement("//div[contains(@id,'rememberMe')]/input");
		}

		// Wait for a displayed element to start the test.
		browserDriver.waitForElementDisplayed(emailFieldXpath);
		waitingAsserter.assertElementDisplayed(emailFieldXpath);

		// Clear the *Email Address* field.
		browserDriver.clearElement(emailFieldXpath);

		// Enter "test@liferay.com" into the *Email Address* field.
		browserDriver.sendKeysToElement(emailFieldXpath, "test@liferay.com");

		// Enter "invalid_password" into the *Password* feild.
		String passwordFieldXpath = "//input[contains(@id,':password')]";
		browserDriver.sendKeysToElement(passwordFieldXpath, "invalid_password");

		// Click the *Sign In* button.
		String signInButtonXpath = "//input[@type='submit' and @value='Sign In']";
		browserDriver.clickElementAndWaitForRerender(signInButtonXpath);

		// Verify that the "Authentication failed" error message is displayed.
		String messageErrorXpath = "//form[@method='post']/ul/li";
		waitingAsserter.assertTextPresentInElement("Authentication failed", messageErrorXpath);

		// Verify that the email field value still contains "test@liferay.com".
		waitingAsserter.assertTextPresentInElementValue("test@liferay.com", emailFieldXpath);

		// Verify that the password field value is empty.

		ExpectedCondition<Boolean> passwordValueEmptyCondition = ExpectedConditions.attributeToBe(By.xpath(
					passwordFieldXpath), "value", "");
		waitingAsserter.assertTrue(passwordValueEmptyCondition);

		// Enter "test" into the *Password* feild.
		browserDriver.sendKeysToElement(passwordFieldXpath, "test");

		// Click the *Sign In* button.
		browserDriver.clickElement(signInButtonXpath);

		// Verify that the 'Sign In' was successful.
		waitingAsserter.assertTextPresentInElement("You are signed in",
			"//div[contains(@class,'liferay-faces-bridge-body')]");

		//J-
		// Close the browser.
		// Reopen the browser.
		//J+

		// TECHNICAL NOTE: BrowserDriver removes all cookies on closing, so simulate the browser closing by removing all
		// cookies that do not have an expiry (non-persistent cookies). For more details, see here:
		// https://stackoverflow.com/questions/3869821/how-do-i-create-a-persistent-vs-a-non-persistent-cookie.
		Set<Cookie> browserCookies = browserDriver.getBrowserCookies();
		WebDriver webDriver = browserDriver.getWebDriver();

		for (Cookie cookie : browserCookies) {

			if (cookie.getExpiry() == null) {
				webDriver.manage().deleteCookie(cookie);
			}
		}

		// Navigate the browser to the portal page that contains the jsf-sign-in portlet.
		browserDriver.navigateWindowTo(jsfLoginPageURL);

		if (testRememberMe) {

			// Verify that the 'Sign In' was successful.
			waitingAsserter.assertTextPresentInElement("You are signed in",
				"//div[contains(@class,'liferay-faces-bridge-body')]");
		}
		else {

			// Verify that you are not signed in.
			waitingAsserter = getWaitingAsserter();
			waitingAsserter.assertTextNotPresentInElement("You are signed in",
				"//div[contains(@class,'liferay-faces-bridge-body')]", false);
		}
	}

	@Override
	protected void doSetUp() {
		// Avoid signing in in the initial setup.
	}
}
