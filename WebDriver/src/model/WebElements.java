package model;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Screen;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import config.StaticUtilities;

public class WebElements {

	public static Logger executionLog;
	public static Logger reportLog;
	public static Logger TestexecutionLog; // bhavika
	public static int fail = 0;
	public static int pass = 0;
	public static int flag = 0;
	public static int shell = 0;
	List<String> list;
	String locatorType, locatorString, actionParameters;

	public static Boolean failedFlag = false;
	public WebDriver wd;

	public WebElements() {

	}

	public void openWindow(String browserName) throws Exception {

		// executionLog.debug("----------- Test suite execution starting on "
		// + browserName + "-----------\n");

		String filePath = new File("").getAbsolutePath();

		switch (browserName.toUpperCase()) {
		case "CHROME":
			try {

				ChromeOptions options = new ChromeOptions();
				DesiredCapabilities capabilities = new DesiredCapabilities();
				capabilities.setCapability(ChromeOptions.CAPABILITY, options);
				capabilities.setCapability("disable-restore-session-state", true);
				options.addArguments(
						"--ignore-certificate-errors --no-sandbox --disable-extensions-file-access-check --disable-extensions-http-throttling --disable-infobars --enable-automation --start-maximized --disable-local-storage");

				Map<String, Object> prefs = new LinkedHashMap<>();
				prefs.put("credentials_enable_service", Boolean.valueOf(false));
				prefs.put("profile.password_manager_enabled", Boolean.valueOf(false));
				prefs.put("profile.default_content_settings.cookies", 2);
				options.setExperimentalOption("prefs", prefs);
				options.addExtensions(new File(filePath + "\\Resources\\extension.crx"));
				filePath = filePath.concat("\\Resources\\chromedriver.exe");
				System.setProperty("webdriver.chrome.driver", filePath);

				wd = new ChromeDriver(capabilities);
				wd.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

				Thread.sleep(2000);

			} catch (WebDriverException e) {
				executionLog.debug(e.getMessage());
				executionLog.debug("Closing TestSuite Execution, as Browser was not opened.");
				takeScreenShot();
			}
			break;

		case "FIREFOX":
			try {
				wd = new FirefoxDriver();
				Thread.sleep(2000);
			} catch (WebDriverException e) {
				executionLog.debug(e.getMessage());
				executionLog.debug("Closing TestSuite Execution, as Browser was not opened.");
				takeScreenShot();
			}
			break;

		case "IE":
			filePath = filePath.concat("\\Resources\\IEDriverServer.exe");
			try {
				System.setProperty("webdriver.ie.driver", filePath);
				wd = new InternetExplorerDriver();
				Thread.sleep(2000);
			} catch (WebDriverException e) {
				executionLog.debug(e.getMessage());
				executionLog.debug("Closing TestSuite Execution, as Browser was not opened.");
				takeScreenShot();
			}
			break;
		default:
			executionLog.debug("Couldn't launch specified browser.");
			wd = null;
			break;
		}
	}

	@SuppressWarnings("unused")
	public String removeQuotes(String temp) {

		if (temp.isEmpty()) {

			// System.out.println("Received String is EMPTY");
			return "";
		}
		if (temp == null) {

			// System.out.println("Received String is NULL");
			return "";
		}
		if (temp.contains("\"\"")) {

			temp = temp.replaceAll("\"\"", "\"");
		}
		if (temp.charAt(0) == '"') {

			temp = temp.replaceFirst("\"", "");
		}
		// System.out.println("Return Value: (" + temp + ")");
		return temp;
	}

	public void setBasicDetails(List<String> list) {

		// list = StaticUtilities.stepList;

		// System.out.println("Inside Set Basic Details");
		locatorType = removeQuotes(list.get(StaticUtilities.FUNCTION_LOCATOR_TYPE_INDEX)).trim();
		locatorString = removeQuotes(list.get(StaticUtilities.FUNCTION_LOCATOR_STRING_INDEX)).trim();
		actionParameters = removeQuotes(list.get(StaticUtilities.FUNCTION_ARGUMENTS_INDEX)).trim();
		actionParameters = actionParameters.replace("CR_LF", (new Character((char) 10)).toString());
		failedFlag = false;

		// if (executionLog.isDebugEnabled()) {
		//
		// executionLog.debug("Locator Type: (" + locatorType
		// + ") Locator String: (" + locatorString
		// + ") Action Parameters: (" + actionParameters + ")");
		// }
	}

	public Boolean navigate(List<String> list) {

		// List<String> list = StaticUtilities.stepList;
		failedFlag = false;
		try {

			wd.get(list.get(StaticUtilities.FUNCTION_ARGUMENTS_INDEX));
			// wd.get("https://wdswifi.stgpttplus.com/WebDispatcher/idmui/index.html#/login");
			wd.manage().window().maximize();
		} catch (WebDriverException e) {
			executionLog.debug(e.getMessage());
			failedFlag = true;
		}
		return failedFlag;
	}

	public Boolean clickHold(List<String> list) {

		setBasicDetails(list);
		By locator;
		WebElement element;
		locator = locatorValue(locatorType, locatorString);
		element = wd.findElement(locator);

		try {
			Actions action = new Actions(wd);
			action.clickAndHold(element).build().perform();
			Thread.sleep(10000);
			action.moveToElement(element).release();
			Thread.sleep(1000);
		} catch (WebDriverException | InterruptedException e) {
			flag++;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
			failedFlag = true;
		}
		return failedFlag;
	}

	public Boolean sendKeys(List<String> list) throws Exception {

		setBasicDetails(list);
		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		try {
			element = wd.findElement(locator);
			executionLog.debug("Sending values provided:\t" + actionParameters);
			element.clear();
			element.sendKeys(actionParameters);
		} catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean verifyElementPresent(List<String> list) throws Exception {

		setBasicDetails(list);
		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		try {

			element = wd.findElement(locator);
			String obtainedValue = element.getText();
			if (obtainedValue.equals(actionParameters)) {
				executionLog.debug(
						"Page Value (" + obtainedValue + ") matched with the one Provided (" + actionParameters + ")");
				// TestexecutionLog.debug("Page Value (" + obtainedValue+
				// ") matched with the one Provided (" + actionParameters +
				// ")");
			} else {
				takeScreenShot();
				flag++;
				executionLog.debug("Page Value (" + obtainedValue + ") did not match with the one provided ("
						+ actionParameters + ")");
				// TestexecutionLog.debug("Page Value (" + obtainedValue+
				// ") did not match with the one Provided (" + actionParameters
				// + ")");
			}
		} catch (WebDriverException e) {
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			flag++;
			takeScreenShot();
			failedFlag = true;
		}
		return failedFlag;
	}

	public Boolean waitForClickElementPresent(List<String> list) {

		WebDriverWait wait = new WebDriverWait(wd, 100);
		setBasicDetails(list);

		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		try {
			element = wait.until(ExpectedConditions.elementToBeClickable(locator));

			if (wd.findElements(locator).size() == 0) {
				executionLog.debug(element + " not loaded yet");
				System.out.println(element + " not loaded yet");
			}
		} catch (WebDriverException e) {
			flag++;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
			failedFlag = true;
		}
		return failedFlag;
	}

	public Boolean waitForElementPresent(List<String> list) throws Exception {

		WebDriverWait wait = new WebDriverWait(wd, 100);
		setBasicDetails(list);

		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		try {

			element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

			if (wd.findElements(locator).size() == 0) {

				executionLog.debug(element + " not loaded yet");
			}
		} catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		}
		return failedFlag;
	}

	public void takeScreenShot() {

		SimpleDateFormat sd = new SimpleDateFormat("_dd_M_yy_hh_mm_ss");

		String date3 = sd.format(new Date());
		String simpleDate = date3.toString();
		String screenShotPath = new File("").getAbsolutePath();

		try {
			File scrFile = ((TakesScreenshot) wd).getScreenshotAs(OutputType.FILE);

			if (scrFile == null) {
				executionLog.debug("Couldn't take screen shot");
			} else {
				String filePath = StaticUtilities.SCREENSHOT_FILEPATH + Executor.fileDate.toString();
				try {
					FileUtils.copyFile(scrFile, new File(filePath + "\\screenShot" + simpleDate + ".png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				executionLog.debug("Please refer screenshot:\t" + screenShotPath + "\\" + filePath + "\\screenShot"
						+ simpleDate + ".png");
			}
		} catch (WebDriverException e) {
			executionLog.debug(e.getMessage());
		}
	}

	public Boolean mouseRightClick(List<String> list) {
		By locator;
		WebElement element = null;
		WebDriverWait wait = new WebDriverWait(wd, 100);
		setBasicDetails(list);
		try {
			locator = locatorValue(locatorType, locatorString);
			element = wd.findElement(locator);
			element = wait.until(ExpectedConditions.elementToBeClickable(locator));
			Actions action = new Actions(wd);
			action.moveToElement(element);
			action.contextClick(element).build().perform();
		} catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean click(List<String> list) {

		setBasicDetails(list);
		WebElement element = null;
		WebDriverWait wait = new WebDriverWait(wd, 30);
		wd.switchTo().activeElement();

		// System.out.println("Switched to Active Window");
		short retryCount = 0;
		Boolean retryFlag = true;

		while ((retryFlag) && (retryCount <= StaticUtilities.RETRY_COUNT)) {

			retryFlag = false;
			retryCount++;

			// System.out.println("Inside Click Function (While Loop). Retry Count: "+
			// retryCount);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {

				System.err.println("Exception while sleeping");
				e1.printStackTrace();
			}
			try {

				By locator = locatorValue(locatorType, locatorString);
				if (locator == null)
					executionLog.error("Locator Null\n\n");

				element = wd.findElement(locator);
				element = wait.until(ExpectedConditions.elementToBeClickable(locator));
				element.click();
			} catch (WebDriverException e) {
				executionLog.error(
						"Exception obtained while performing series of click operation. Retry Count: " + retryCount);
				executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",selector:"
						+ locatorString + "}");

				takeScreenShot();
				retryFlag = true;
			}
		}
		if (retryFlag) {
			flag++;
			failedFlag = true;
		}
		if (retryCount > (StaticUtilities.RETRY_COUNT)) {
			flag++;

			// WebElement dialog = wd.findElement(By.className("modal-dialog"));
			// dialog.getText();
			// if (dialog.getText() != null)
			// System.out.println("\n\nClosing by title unexpected pop-up\n\n");
			// wd.switchTo().activeElement();

			if (wd.findElements(By.xpath("//*[@id=\"wd-modal-header\"]/div/div[1]/button")).size() != 0) {
				takeScreenShot();
				wd.findElement(By.xpath("//*[@id=\"wd-modal-header\"]/div/div[1]/button")).click();
				executionLog.debug("Closed unexpected pop-up");
			}
			failedFlag = true;
		}
		// System.out.println("Click Function Return Statement. Failed Flag: "+
		// failedFlag);
		return failedFlag;
	}

	public Boolean showDesktop() {

		Robot robot = null;
		try {
			robot = new Robot();
			robot.keyPress(KeyEvent.VK_WINDOWS);
			robot.keyPress(KeyEvent.VK_D);
			robot.keyRelease(KeyEvent.VK_D);
			robot.keyRelease(KeyEvent.VK_WINDOWS);
		} catch (AWTException e) {
			executionLog.error(e.getMessage());
			e.printStackTrace();
			failedFlag = true;
		}
		return failedFlag;
	}

	public Boolean maximizeWindow() {

		try {
			wd.manage().window().maximize();
		} catch (WebDriverException e) {
			executionLog.error(e.getMessage());
			failedFlag = true;
		}
		return failedFlag;
	}

	public Boolean invokeAutoIT(List<String> list) {

		// showDesktop(list);
		// wd.manage().window().maximize();
		setBasicDetails(list);
		String filePath = new File("").getAbsolutePath();

		filePath = filePath.concat("\\" + StaticUtilities.PTX_MULTIMEDIA_FILEPATH);
		try {
			new ProcessBuilder(StaticUtilities.SCRIPT_FILEPATH + locatorString, filePath + actionParameters).start();
		} catch (IOException e) {
			flag++;
			executionLog.debug(e.getMessage());
			takeScreenShot();
			failedFlag = true;
		}
		return failedFlag;
	}

	public Boolean endOfTestCase(List<String> list) throws InterruptedException {

		String testCaseId = list.get(StaticUtilities.FUNCTION_TC_NAME_INDEX)
				+ list.get(StaticUtilities.FUNCTION_STEP_DESCRIPTION_INDEX);

		if (flag != 0) {
			if (shell != 0) {
				executionLog.debug("\n--Shell_" + testCaseId + "--::\t FAILED");
				reportLog.info("\n--Shell_" + testCaseId + "--::\t FAILED");
			} else {
				executionLog.debug("\n--" + testCaseId + "--::\t\tFAILED");
				reportLog.info("\n--" + testCaseId + "--::\t\tFAILED");
			}
			fail++;
			flag = 0;
			shell = 0;
		} else {
			pass++;
			executionLog.debug(testCaseId + "::\t PASSED");
			reportLog.info("\n" + testCaseId + "::\t\tPASSED");
		}
		executionLog
				.debug("\n\n------------------------ End of Test\t\t" + testCaseId + " ------------------------\n\n");
		Thread.sleep(1000);
		return false;
	}

	public By locatorValue(String locatorType, String value) {

		By by;
		switch (locatorType) {
		case "id":
			by = By.id(value);
			break;
		case "name":
			by = By.name(value);
			break;
		case "xpath":
			by = By.xpath(value);
			break;
		case "linkText":
			by = By.linkText(value);
			break;
		case "css":
			by = By.cssSelector(value);
			break;
		default:
			by = null;
			break;
		}
		return by;
	}

	// Bhavika
	public Boolean selectValue(List<String> list) {
		setBasicDetails(list);
		By locator;
		locator = locatorValue(locatorType, locatorString);
		try {
			Select dropdown = new Select(wd.findElement(locator));
			dropdown.selectByVisibleText(actionParameters);
		}

		catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean clickImage(List<String> list) {

		setBasicDetails(list);
		String imageFilePath = new File("").getAbsolutePath();
		String image = StaticUtilities.IMAGE_FILEPATH + actionParameters;

		short retryCount = 0;
		Boolean retryFlag = true;

		Screen s = new Screen();
		while ((retryFlag) && (retryCount < StaticUtilities.RETRY_COUNT)) {

			retryFlag = false;
			retryCount++;

			try {
				if (s.exists(image) != null) {
					s.wait(image);
					s.click(image);
					executionLog.debug("Clicked on Image:\t" + imageFilePath + "\\" + image);
					// TestexecutionLog.debug("Clicked on Image:\t"+image);
				} else {
					flag++;
					failedFlag = true;
					executionLog.debug("Couldn't find Image:\t" + imageFilePath + "\\" + image);
					takeScreenShot();
					// TestexecutionLog.debug("Couldn't find Image:\t"+image);
				}
			} catch (FindFailed e) {
				executionLog.error("Couldn't find image on screen");
				// TestexecutionLog.debug("Couldn't find image on screen");
				executionLog.debug(e.getMessage());
				takeScreenShot();
				retryFlag = true;

			}

			catch (WebDriverException e) {
				executionLog.error(
						"Exception obtained while performing series of click operation. Retry Count: " + retryCount);
				executionLog.debug(e.getMessage());
				takeScreenShot();
				retryFlag = true;

			}
		}

		if (retryCount > (StaticUtilities.RETRY_COUNT)) {
			flag++;
			failedFlag = true;
			executionLog.debug("Couldn't find element:\t" + imageFilePath + "\\" + image);
		}
		return failedFlag;
	}

	public Boolean doubleClickImage(List<String> list) {

		setBasicDetails(list);
		String imageFilePath = new File("").getAbsolutePath();
		String image = StaticUtilities.IMAGE_FILEPATH + actionParameters;

		short retryCount = 0;
		Boolean retryFlag = true;

		Screen s = new Screen();
		while ((retryFlag) && (retryCount < StaticUtilities.RETRY_COUNT)) {

			retryFlag = false;
			retryCount++;

			try {
				if (s.exists(image) != null) {
					s.wait(image);
					s.doubleClick(image);
					executionLog.debug("Clicked on Image:\t" + imageFilePath + "\\" + image);
					// TestexecutionLog.debug("Clicked on Image:\t"+image);
				} else {
					flag++;
					failedFlag = true;
					executionLog.debug("Couldn't find element:\t" + imageFilePath + "\\" + image);
					takeScreenShot();
					// TestexecutionLog.debug("Couldn't find Image:\t"+image);
				}
			} catch (FindFailed e) {
				executionLog.error("Couldn't find image on screen");
				// TestexecutionLog.debug("Couldn't find image on screen");
				executionLog.debug(e.getMessage());
				takeScreenShot();
				retryFlag = true;

			}

			catch (WebDriverException e) {
				executionLog.error(
						"Exception obtained while performing series of click operation. Retry Count: " + retryCount);
				executionLog.debug(e.getMessage());
				takeScreenShot();
				retryFlag = true;

			}
		}

		if (retryCount > (StaticUtilities.RETRY_COUNT)) {
			flag++;
			failedFlag = true;
			executionLog.debug("Couldn't find element:\t" + imageFilePath + "\\" + image);
		}
		return failedFlag;
	}

	public Boolean mouseHoverImage(List<String> list) {

		setBasicDetails(list);
		String imageFilePath = new File("").getAbsolutePath();
		String image = StaticUtilities.IMAGE_FILEPATH + actionParameters;

		short retryCount = 0;
		Boolean retryFlag = true;

		Screen s = new Screen();
		while ((retryFlag) && (retryCount < StaticUtilities.RETRY_COUNT)) {

			retryFlag = false;
			retryCount++;

			try {
				// final Mouse mouse = new DesktopMouse();
				// ScreenRegion s = new DesktopScreenRegion();
				//
				// Target target = new ImageTarget(new File(image));
				// r = s.wait(target, 5000);
				// s.find(target);
				//
				// final Mouse mouse = new DesktopMouse();
				// mouse.drop(r.getCenter());
				//

				if (s.exists(image) != null) {
					s.wait(image);
					s.doubleClick(image);
					executionLog.debug("Clicked on Image:\t" + imageFilePath + "\\" + image);
					// TestexecutionLog.debug("Clicked on Image:\t"+image);
				} else {
					flag++;
					failedFlag = true;
					executionLog.debug("Couldn't find element:\t" + imageFilePath + "\\" + image);
					takeScreenShot();
					// TestexecutionLog.debug("Couldn't find Image:\t"+image);
				}
			} catch (FindFailed e) {
				executionLog.error("Couldn't find image on screen");
				// TestexecutionLog.debug("Couldn't find image on screen");
				executionLog.debug(e.getMessage());
				takeScreenShot();
				retryFlag = true;

			}

			catch (WebDriverException e) {
				executionLog.error(
						"Exception obtained while performing series of click operation. Retry Count: " + retryCount);
				executionLog.debug(e.getMessage());
				takeScreenShot();
				retryFlag = true;

			}
		}

		if (retryCount > (StaticUtilities.RETRY_COUNT)) {
			flag++;
			failedFlag = true;
			executionLog.debug("Couldn't find element:\t" + imageFilePath + "\\" + image);
		}
		return failedFlag;
	}

	public Boolean verifyElementNotPresent(List<String> list) {

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		setBasicDetails(list);

		By locator;
		locator = locatorValue(locatorType, locatorString);

		if (wd.findElements(locator).size() == 0) {
			executionLog.debug("Element is not present");
			// TestexecutionLog.debug("Element is not present");
		} else {
			takeScreenShot();
			flag++;
			failedFlag = true;
			executionLog.debug("Element is present");
			// TestexecutionLog.debug("Element is present");
		}
		return failedFlag;
	}

	public Boolean verifyPTXState(List<String> list) throws StringIndexOutOfBoundsException, Exception {

		setBasicDetails(list);
		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		try {
			element = wd.findElement(locator);
			String actualValue = element.getText();

			String obtainedValue = actualValue.substring(actualValue.lastIndexOf(' ') + 1);

			// System.out.println("Obtained substring:\t"+obtainedValue);

			if (obtainedValue.equals(actionParameters)) {
				executionLog.debug(
						"Page Value (" + obtainedValue + ") matched with the one Provided (" + actionParameters + ")");
				// TestexecutionLog.debug("Page Value (" + obtainedValue +
				// ") matched with the one Provided (" + actionParameters +
				// ")");
			} else {
				takeScreenShot();
				flag++;
				failedFlag = true;
				executionLog.debug("Page Value (" + obtainedValue + ") did not match with the one provided ("
						+ actionParameters + ")");
				// TestexecutionLog.debug("Page Value (" + obtainedValue +
				// ") did not match with the one provided (" + actionParameters
				// + ")");
			}
		} catch (StringIndexOutOfBoundsException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			executionLog.debug("Page Value did not match with the one provided (" + actionParameters + ")");
			// TestexecutionLog.debug("Page Value did not match with the one provided ("
			// + actionParameters + ")");
			takeScreenShot();
		} catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			executionLog.debug(e.getMessage());
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean verifyPTXMessage(List<String> list) {

		setBasicDetails(list);
		By locator;
		WebElement element;
		locator = locatorValue(locatorType, locatorString);

		try {
			element = wd.findElement(locator);
			String actualValue = element.getText();
			String obtainedValue = actualValue.substring(0, actionParameters.length());

			// String
			// obtainedValue=actualValue.substring(actualValue.length()-verifyValue.length(),
			// actualValue.length());

			if (obtainedValue.equals(actionParameters)) {
				executionLog.debug(
						"Page Value (" + obtainedValue + ") matched with the one Provided (" + actionParameters + ")");
				// TestexecutionLog.debug("Page Value (" + obtainedValue +
				// ") matched with the one Provided (" + actionParameters +
				// ")");
			} else {
				takeScreenShot();
				flag++;
				failedFlag = true;
				executionLog.debug("Page Value (" + obtainedValue + ") did not match with the one provided ("
						+ actionParameters + ")");
				// TestexecutionLog.debug("Page Value (" + obtainedValue +
				// ") did not match with the one provided (" + actionParameters
				// + ")");
			}
		} catch (StringIndexOutOfBoundsException e) {
			flag++;
			failedFlag = true;
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			executionLog.debug("Page Value did not match with the one provided (" + actionParameters + ")");
			// TestexecutionLog.debug("Page Value did not match with the one provided ("
			// + actionParameters + ")");
			takeScreenShot();
		} catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			executionLog.debug(e.getMessage());
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean verifyImagePresent(List<String> list) {

		setBasicDetails(list);
		String imageFilePath = new File("").getAbsolutePath();
		String image = StaticUtilities.IMAGE_FILEPATH + actionParameters;

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		Screen s = new Screen();

		try {
			if (s.exists(image) != null)
				executionLog.debug("Provided images has been found on screen:\t" + imageFilePath + "\\" + image);
			else {
				flag++;
				failedFlag = true;
				executionLog.debug("Couldn't find provided images on screen:\t" + imageFilePath + "\\" + image);
				takeScreenShot();
			}
		} catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			executionLog.debug(e.getMessage());
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean verifyEnabled(List<String> list) {

		setBasicDetails(list);
		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		try {
			element = wd.findElement(locator);

			if (element.isEnabled())
				executionLog.debug("Provided element is Enabled");
			else {
				takeScreenShot();
				flag++;
				failedFlag = true;
				executionLog.debug("Provided element is not Enabled");
			}
		}

		catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean verifyDisabled(List<String> list) {

		setBasicDetails(list);
		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		try {
			element = wd.findElement(locator);

			if (!element.isEnabled())
				executionLog.debug("Provided element is Disabled");
			else {
				takeScreenShot();
				flag++;
				failedFlag = true;
				executionLog.debug("Provided element is Enabled");
			}
		}

		catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean verifyNotSelected(List<String> list) {

		setBasicDetails(list);
		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		try {
			element = wd.findElement(locator);

			if (!element.isSelected()) {
				executionLog.debug("Check box is not selected");
			} else {
				takeScreenShot();
				flag++;
				failedFlag = true;
				executionLog.debug("Check box is selected");
			}
		}

		catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean verifyOptions(List<String> list) {

		setBasicDetails(list);
		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		String[] verifyValue = actionParameters.split(",");

		try {
			int count = 0;
			element = wd.findElement(locator);
			Select select = new Select(element);
			List<WebElement> options = select.getOptions();

			for (int i = 0; i < verifyValue.length; i++) {
				if (options.get(i).getText().equals(verifyValue[i])) {
					executionLog.debug("Page Value (" + options.get(i).getText() + ") matched with the one Provided ("
							+ verifyValue[i] + ")");
					count++;
				} else {
					flag++;
					failedFlag = true;
					executionLog.debug("Page Value (" + options.get(i).getText()
							+ ") did not match with the one Provided (" + verifyValue[i] + ")");
				}
			}

			if (count == verifyValue.length)
				executionLog.debug("Provided options has been matched with page value");
			else {
				flag++;
				failedFlag = true;
				executionLog.debug("Provided options did not match with page value");
			}
		} catch (StringIndexOutOfBoundsException e) {
			flag++;
			failedFlag = true;
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		} catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean verifySelected(List<String> list) {
		setBasicDetails(list);
		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		try {
			element = wd.findElement(locator);
			if (element.isSelected())
				executionLog.debug("Check box is selected");

			else {
				takeScreenShot();
				flag++;
				failedFlag = true;
				executionLog.debug("Check box is not selected");
			}
		} catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean verifyToolTip(List<String> list) {

		setBasicDetails(list);
		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		try {
			Actions action = new Actions(wd);
			element = wd.findElement(locator);
			action.moveToElement(element).build().perform();
			String obtainedValue = element.getAttribute("title");

			if (obtainedValue.equals(actionParameters)) {

				executionLog.debug(
						"Page Value (" + obtainedValue + ") matched with the one Provided (" + actionParameters + ")");
			} else {
				takeScreenShot();
				flag++;
				failedFlag = true;
				executionLog.debug("Page Value (" + obtainedValue + ") did not match with the one provided ("
						+ actionParameters + ")");
			}
		}

		catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean verifyValue(List<String> list) {

		setBasicDetails(list);
		By locator;
		WebElement element = null;
		locator = locatorValue(locatorType, locatorString);

		try {
			Actions action = new Actions(wd);
			element = wd.findElement(locator);
			action.moveToElement(element).build().perform();
			String obtainedValue = element.getAttribute("value");

			if (obtainedValue.equals(actionParameters)) {

				executionLog.debug(
						"Page Value (" + obtainedValue + ") matched with the one Provided (" + actionParameters + ")");
			} else {
				takeScreenShot();
				flag++;
				failedFlag = true;
				executionLog.debug("Page Value (" + obtainedValue + ") did not match with the one provided ("
						+ actionParameters + ")");
			}
		}

		catch (WebDriverException e) {
			flag++;
			failedFlag = true;
			// executionLog.debug(e.getMessage());
			executionLog.error("No such element: Unable to locate element:\t {method:" + locatorType + ",\tselector:"
					+ locatorString + "}");
			takeScreenShot();
		}
		return failedFlag;
	}

	public Boolean invokeShell(List<String> list) {

		setBasicDetails(list);
		String host = "10.0.18.12";
		String user = "kodiak";
		String password = "kodiak";
		String argss = actionParameters.replaceAll(",", " ");

		System.out.println("Script Name: " + locatorString + "\tArgs:\t" + argss);
		String command = "/home/kodiak/automation/webDispatcher/" + locatorString + " " + argss;

		executionLog.debug("Executing script:\t" + command);

		try {
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, 22);
			session.setPassword(password);
			session.setConfig(config);
			session.connect();

			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();
			channel.connect();

			// byte[] tmp = new byte[1024];
			String tmpString = new String();
			int i = 0;
			while (true) {
				while (in.available() > 0) {
					byte[] tmp = new byte[1024];

					i = in.read(tmp, 0, 1024);
					// tmpString += new String(tmp);
					if (i < 0)
						break;
					tmpString += new String(tmp);
				}
				if (channel.isClosed())
					break;
			}
			channel.disconnect();
			session.disconnect();

			executionLog.debug(tmpString.trim());

			if (tmpString.contains("200_OK")) {
				executionLog.debug("Shell Script has been executed with desired result");
			} else {
				flag++;
				failedFlag = true;
				shell++;
				executionLog.debug("Improper Shell Script execution.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return failedFlag;
	}

	public static void getAllEmptyFoldersOfDir(File current) {

		if (current.isDirectory()) {
			File[] files = current.listFiles();
			if (files.length == 0) { // There is no file in this folder - safe
										// to delete
				System.out.println("Removed empty folder: " + current.getAbsolutePath());
				current.delete();

			} else {
				int totalFolderCount = 0;
				int emptyFolderCount = 0;
				for (File f : files) {
					if (f.isDirectory()) {
						totalFolderCount++;
						getAllEmptyFoldersOfDir(f); // safe to delete
						emptyFolderCount++;
					}
				}

				if (totalFolderCount == files.length && emptyFolderCount == totalFolderCount) { // only if
																								// all
																								// folders
																								// are safe
																								// to delete
																								// then this
																								// folder is
																								// also safe
																								// to delete
					System.out.println("Removed all empty folder: " + current.getAbsolutePath());

				}
			}
		}
	}

	// vipul
	public boolean invokeSpectra(List<String> list) {

		System.out.println("entered invokeShell function");
		// wd.manage().window().maximize();
		setBasicDetails(list);
		By locator;
		locator = locatorValue(locatorType, locatorString);
		String host = "10.0.18.12";
		String user = "kodiak";
		String password = "kodiak";
		String argss = actionParameters.replaceAll(",", " ");

		System.out.println("Script Name: " + locatorString + "\tArgs:\t" + argss);
		String command = "/home/kodiak/automation/webDispatcher/" + locatorString + " " + argss;

		executionLog.debug("Executing script:\t" + command);

		try {
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, 22);
			session.setPassword(password);
			session.setConfig(config);
			session.connect();

			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();
			channel.connect();

			// byte[] tmp = new byte[1024];
			// String tmpString = new String();
			// int i = 0;
			// while (true) {
			// while (in.available() > 0) {
			// i = in.read(tmp, 0, 1024);
			// tmpString += new String(tmp);
			// System.out.println("inside in.read fn...");
			// executionLog.debug("i:" + (int) i +"tmp:" +tmp);
			// if (i < 0)
			// break;
			// }
			// if (channel.isClosed())
			// break;
			// }
			// os.write(tmp, 0, i);
			// os.close();
			// Thread.sleep(2000);
			// channel.disconnect();
			// session.disconnect();

			/*
			 * executionLog.debug(tmpString.trim());
			 * 
			 * if (tmpString.contains("200_OK")) { executionLog.debug(
			 * "Shell Script has been executed with desired result"); failedFlag = false; }
			 * else { failedFlag = true; //shell++;
			 * executionLog.debug("Improper Shell Script execution."); }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
		return failedFlag;
	}

	public boolean waitForImage(List<String> list) throws Exception {

		setBasicDetails(list);
		String filePath = new File("").getAbsolutePath();
		String image = filePath.concat("\\Resources\\Images\\" + actionParameters);
		String addParameters = removeQuotes(list.get(StaticUtilities.FUNCTION_ADDITIONAL_ARGS_INDEX));
		int num = Integer.parseInt(addParameters);
		num = num / 1000;
		Thread.sleep(1000);
		Screen s = new Screen();

		try {
			failedFlag = true;
			for (int x = 0; x <= num; x++) {
				if (s.exists(image) != null) {
					// executionLog.debug("inside if");
					// executionLog.debug("x:" + x + "num:" + num + "");
					executionLog.debug("Provided images has been found on screen at:\t" + s.exists(image));
					failedFlag = false;
					break;
				}

				else {
					// executionLog.debug("inside else");
					Thread.sleep(1000);
					// executionLog.debug("x:" + x + "num:" + num + "");
				}
			}

			if (failedFlag == true) {
				flag++;
				executionLog.debug("Couldn't find provided images on screen:\t" + image);
				takeScreenShot();
			}
		}

		catch (WebDriverException e) {
			flag++;
			executionLog.debug(e.getMessage());
			takeScreenShot();
			failedFlag = true;
		}
		return failedFlag;
	}

	public boolean verifyImageNotPresent(List<String> list) throws Exception {

		setBasicDetails(list);
		String filePath = new File("").getAbsolutePath();
		String image = filePath.concat("\\Resources\\Images\\" + actionParameters);

		Thread.sleep(1000);
		Screen s = new Screen();

		try {
			if (s.exists(image) == null) {

				executionLog.debug("SUCCESS: Provided images is not present on screen at:\t" + s.exists(image));
				failedFlag = false;
			} else {
				flag++;
				executionLog.debug("failure: image is present on screen:\t" + image);
				takeScreenShot();
				failedFlag = true;

			}
		} catch (WebDriverException e) {
			flag++;
			executionLog.debug(e.getMessage());
			takeScreenShot();
			failedFlag = true;
		}
		return failedFlag;
	}
}
