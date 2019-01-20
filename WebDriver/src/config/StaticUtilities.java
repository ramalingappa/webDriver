package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class StaticUtilities {

	public static final char DEFAULT_SEPARATOR = ',';
	public static final char DEFAULT_QUOTE = '"';

	public static final String TESTSUITE_CSV = "TestSuite\\testSuite.csv";
	public static final String FUNCTION_CSV = "TestSuite\\functionList.csv";
	public static final String SCREENSHOT_FILEPATH = "Report\\ScreeenShots\\";
	public static final String REPORT_FILEPATH = "Report\\";
	public static final String IMAGE_FILEPATH = "Resources\\Images\\";
	public static final String SCRIPT_FILEPATH = "Resources\\Scripts\\";
	public static final String PTX_MULTIMEDIA_FILEPATH = "Resources\\Multimedia\\";

	public static volatile Logger webDriverElementExecutionLog;
	public static volatile Logger webDriverMainExecutionLog;
	public static volatile Logger webDriverExecutionReport;

	public static final short RETRY_COUNT = 1;
	public static final short SUITE_PARSE_SIZE = 3000;
	public static final long SET_SPEED = 100;

	public static final short FUNCTION_TC_NAME_INDEX = 0;
	public static final short FUNCTION_STEP_DESCRIPTION_INDEX = 1;
	public static final short FUNCTION_SET_SPEED_INDEX = 2;
	public static final short FUNCTION_ACTION_NAME_INDEX = 3;
	public static final short FUNCTION_LOCATOR_ORM_INDEX = 4;
	public static final short FUNCTION_LOCATOR_TYPE_INDEX = 5;
	public static final short FUNCTION_LOCATOR_STRING_INDEX = 6;
	public static final short FUNCTION_ARGUMENTS_INDEX = 7;
	public static final short FUNCTION_ADDITIONAL_ARGS_INDEX = 8;

	public static void setLogger() {

		Properties logProperties;
		String LOG_PROPERTIES_FILE;
		String filePath = new File("").getAbsolutePath();
		LOG_PROPERTIES_FILE = filePath.concat("\\javaLogger.properties");
		logProperties = new Properties();

		try {
			logProperties.load(new FileInputStream(LOG_PROPERTIES_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PropertyConfigurator.configure(logProperties);

		StaticUtilities.webDriverElementExecutionLog = Logger.getLogger("webElementExecution_Log");
		StaticUtilities.webDriverMainExecutionLog = Logger.getLogger("mainExecutor_Log");
		StaticUtilities.webDriverExecutionReport = Logger.getLogger("webDriverReport_Log");

	}
}
