package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;

import config.StaticUtilities;

//import com.opencsv.CSVReader;

public class Executor {

	public static Logger executionLog;
	public static Logger reportLog;
	public WebElements webObject;
	// public WebDriver wd;
	static Executor mainExecutor;
	private Map<String, Integer> functionMap = new HashMap<String, Integer>();

	static final SimpleDateFormat sd = new SimpleDateFormat("dd-MMM-YYYY_hh_mm");
	static final String fileDate = sd.format(new Date());

	public static void main(String[] args) {

		Date date = new Date();

		mainExecutor = new Executor();
		mainExecutor.webObject = new WebElements();

		try {

			StaticUtilities.setLogger();
			executionLog = StaticUtilities.webDriverMainExecutionLog;
			WebElements.executionLog = StaticUtilities.webDriverMainExecutionLog;
			WebElements.reportLog = StaticUtilities.webDriverExecutionReport;
			WebElements.TestexecutionLog = StaticUtilities.webDriverMainExecutionLog;

			WebElements.reportLog.info("\t----------- Test suite execution started -----------\n\n");

			mainExecutor.webObject.openWindow("CHROME");
			// mainExecutor.webObject.openWindow("FIREFOX");
			mainExecutor.prepareFunctionMap();
			mainExecutor.testSuiteExecutor();
			mainExecutor.webObject.wd.close();
			mainExecutor.webObject.wd.quit();
			// Close chrome driver here.
			// mainExecutor.wd.close();
			// mainExecutor.wd.quit();
			WebElements.reportLog.info("\n\n\t----------- Test suite execution Completed -----------\n\n");

			Date date2 = new Date();
			WebElements.reportLog.info("\n\nTest start time:\t" + date.toString());
			WebElements.reportLog.info("\nTest End time:\t\t" + date2.toString());

			int totalTcs = WebElements.pass + WebElements.fail;

			WebElements.reportLog.info("\n\nTotal Test-cases:\t" + totalTcs + "\n\nPass:\t" + WebElements.pass
					+ "\nFail:\t" + WebElements.fail + "\n");
			WebElements.reportLog.debug("----------- Test suite execution completed -----------\n");

			File reportFile = new File(StaticUtilities.REPORT_FILEPATH + "\\webDriverReport.log");
			File executionFile = new File(StaticUtilities.REPORT_FILEPATH + "\\mainWebDriverExecution.log");
			FileUtils.copyFile(reportFile,
					new File(StaticUtilities.REPORT_FILEPATH + "Test_Report_" + fileDate.toString() + ".log"));
			FileUtils.copyFile(executionFile,
					new File(StaticUtilities.REPORT_FILEPATH + "Test_Execution_" + fileDate.toString() + ".log"));

		} catch (Exception e) {
			executionLog.error("Exception Occured while creating Web Driver. Exiting Program execution");
			e.printStackTrace();
		}
	}

	public void stepExecutor(List<String> stepList) {

		try {
			Thread.sleep(Integer.parseInt(stepList.get(StaticUtilities.FUNCTION_SET_SPEED_INDEX)));
		} catch (InterruptedException e1) {
			System.out.println("Interupt Error");
			e1.printStackTrace();
		} catch (NumberFormatException e) {
			// System.out.println("Illegal Integer. String Value: ("+
			// stepList.get(StaticUtilities.FUNCTION_SET_SPEED_INDEX) + ")");
			e.printStackTrace();
		}

		String actionName = stepList.get(StaticUtilities.FUNCTION_ACTION_NAME_INDEX).trim();

		// System.out.println("Action Name: (" + actionName + ")");

		if (actionName.contains("_FN")) {

			int lastIndex = Integer.MAX_VALUE;
			int startIndex = 0;
			if (!stepList.get(StaticUtilities.FUNCTION_ARGUMENTS_INDEX).isEmpty()) {

				String[] indices = stepList.get(StaticUtilities.FUNCTION_ARGUMENTS_INDEX).split(":");
				startIndex = Integer.parseInt(indices[0].trim());
				lastIndex = Integer.parseInt(indices[1].trim());
			}
			// if (executionLog.isDebugEnabled()) {
			//
			// executionLog.debug("\tStep Includes Function to be executed. Hence switching
			// to function call");
			// }
			functionExecutor(actionName, startIndex, lastIndex);
			return;
		}

		try {

			Method thisMethod = webObject.getClass().getMethod(actionName, List.class);
			try {

				Boolean result = (Boolean) thisMethod.invoke(webObject, stepList);
				// System.out.println("Result: " + result);
				if (result) {

					// executionLog.info("Step was marked as failed");

					if (stepList.get(StaticUtilities.FUNCTION_TC_NAME_INDEX).equalsIgnoreCase("passOnError")) {

						webObject.flag--;
					}
					if (!stepList.get(StaticUtilities.FUNCTION_ARGUMENTS_INDEX).isEmpty()) {

						if (stepList.get(StaticUtilities.FUNCTION_ARGUMENTS_INDEX).contains("_FN")) {

							executionLog.info("\tExecuting Exception Handling Functionality. Function Name ("
									+ stepList.get(StaticUtilities.FUNCTION_ARGUMENTS_INDEX));
							int lastIndex = Integer.MAX_VALUE;
							int startIndex = 0;
							if ((stepList.size() > StaticUtilities.FUNCTION_ADDITIONAL_ARGS_INDEX)
									&& (!stepList.get(StaticUtilities.FUNCTION_ADDITIONAL_ARGS_INDEX).isEmpty())) {

								String[] indices = stepList.get(StaticUtilities.FUNCTION_ADDITIONAL_ARGS_INDEX)
										.split(":");
								startIndex = Integer.parseInt(indices[0]);
								lastIndex = Integer.parseInt(indices[1]);
							}
							// if (executionLog.isDebugEnabled()) {
							//
							// executionLog.debug("Step Includes Function to be executed. Hence switching to
							// function call");
							// }
							functionExecutor(stepList.get(StaticUtilities.FUNCTION_ARGUMENTS_INDEX), startIndex,
									lastIndex);
						}
					}
				}
			} catch (IllegalAccessException | IllegalArgumentException e) {

				executionLog.error("Exception Occurred while executing method. Method Name: " + actionName);
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				executionLog.error("Exception Occurred while invoking method. Method Name: " + actionName);
				executionLog.error(e.getTargetException());
				System.out.println(e.getCause());
				e.printStackTrace();
			}
		} catch (NoSuchMethodException | SecurityException e) {

			executionLog.error(
					"\tException Occurred while searching for method. Seems like no such method exists. Method Name: (\""
							+ actionName + "\")");
			e.printStackTrace();
		}
	}

	public void testSuiteExecutor() {

		short currentLineIndex = 0;
		int stepCount = 0;

		while (true) {

			ArrayList<List<String>> stepList = parseTestSuite(currentLineIndex, StaticUtilities.SUITE_PARSE_SIZE);

			for (List<String> list : stepList) {

				stepCount++;

				if (list.get(StaticUtilities.FUNCTION_TC_NAME_INDEX).equalsIgnoreCase("SKIP")) {

					// executionLog.info("Step No. ("+ stepCount+
					// ") has been marked as SKIP. Hence, ignoring step");
					continue;
				}
				executionLog.debug("Test: " + (stepCount + 1) + " "
						+ list.get(StaticUtilities.FUNCTION_STEP_DESCRIPTION_INDEX) + "\t\t");
				System.out.println((stepCount + 1) + "\t " + list.get(StaticUtilities.FUNCTION_STEP_DESCRIPTION_INDEX));
				// executionLog.info("Step No. (" + stepCount+
				// ") Execution has been started");
				// if (executionLog.isDebugEnabled()) {
				//
				// executionLog.debug("Step No. (" + stepCount+
				// ") Execution has been started with data (" + list+ ")");
				// }
				stepExecutor(list);
			}

			System.out.println("Arraylist TestSuite Parsed Size (" + stepList.size() + ") and Suite Parse Size ("
					+ StaticUtilities.SUITE_PARSE_SIZE + ")");
			executionLog.info("Arraylist TestSuite Parsed Size (" + stepList.size() + ") and Suite Parse Size ("
					+ StaticUtilities.SUITE_PARSE_SIZE + ")");

			if (stepList.size() == StaticUtilities.SUITE_PARSE_SIZE) {
				currentLineIndex += (short) stepList.size();
				stepList.clear();
				continue;
			} else {
				break;
			}
		}

	}

	public void functionExecutor(String functionName, int startIndex, int lastIndex) {

		int stepCount = startIndex;
		ArrayList<List<String>> functionStepList = parseIndexedFunction(functionName,
				(functionMap.get(functionName) + startIndex), lastIndex);

		for (List<String> list : functionStepList) {

			// executionLog.info("Function (" + functionName+
			// ") with Function Index No. (" + stepCount+
			// ") Execution has been started");
			executionLog.debug(list.get(StaticUtilities.FUNCTION_TC_NAME_INDEX) + ":\t" + stepCount + ":\t"
					+ list.get(StaticUtilities.FUNCTION_STEP_DESCRIPTION_INDEX) + "\t\t");
			System.out.println(list.get(StaticUtilities.FUNCTION_TC_NAME_INDEX) + ":\t" + stepCount + ":\t"
					+ list.get(StaticUtilities.FUNCTION_STEP_DESCRIPTION_INDEX) + "\t\t");

			// if (executionLog.isDebugEnabled()) {
			//
			// executionLog.debug("Function (" + functionName+
			// ") with Function Index No. (" + stepCount+
			// ") Execution has been started with data (" + list+ ")");
			// }
			stepExecutor(list);
			stepCount++;
		}
		return;
	}

	public void prepareFunctionMap() {

		Scanner scanner = null;
		String fnCSV = (StaticUtilities.FUNCTION_CSV);

		executionLog.info("\n------------------------	Test suite Parsing started	------------------------\n");
		executionLog.info("Parsing of (" + fnCSV + ")  File has been started");

		try {

			scanner = new Scanner(new File(fnCSV));
			scanner.nextLine();
			int loopIndex = 1;
			while (scanner.hasNext()) {

				List<String> line = parseLine(scanner.nextLine());
				if (!functionMap.containsKey(line.get(0))) {

					functionMap.put(line.get(0), loopIndex);
				}
				loopIndex++;
			}
		} catch (FileNotFoundException e) {

			executionLog.error("File named (" + fnCSV + ") was not found");
			e.printStackTrace();
		} finally {

			scanner.close();
			executionLog.info("Preparing Function map has been completed.");

			// if (executionLog.isDebugEnabled()) {
			//
			// executionLog.debug("Function Association Data (" + functionMap+
			// ")");
			// }
		}
		return;
	}

	public ArrayList<List<String>> parseTestSuite(int skipIndex, int readCount) {

		Scanner scanner = null;
		String fnCSV = (StaticUtilities.TESTSUITE_CSV);

		// executionLog.debug("\tIndexed Parsing of (" + fnCSV+
		// ") File has been started");
		// executionLog.debug("Indexed Parsing of (" + fnCSV+
		// ") File has been started with from index (" + skipIndex+ ")");
		ArrayList<List<String>> functionList = new ArrayList<List<String>>();

		try {
			scanner = new Scanner(new File(fnCSV));

			for (int loopIndex = 0; loopIndex < skipIndex; loopIndex++) {

				scanner.nextLine();
			}

			// scanner.skip("(?:.*\\r?\\n|\\r){"+ Integer.toString(skipIndex) +
			// "}");
			scanner.nextLine();
			while (scanner.hasNext()) {

				List<String> line = parseLine(scanner.nextLine());
				functionList.add(line);
				readCount--;
				if (readCount == 0) {

					System.out.println("Reached Maximum Read Point");
					break;
				}
			}
		} catch (FileNotFoundException e) {

			executionLog.error("File named (" + fnCSV + ") was not found");
			e.printStackTrace();
		} finally {

			scanner.close();
			executionLog.info("Test Suite Parsing has been completed.");
			executionLog
					.info("\n\n------------------------	Test suite Parsing Completed	------------------------\n\n");
			executionLog
					.info("\n\n------------------------	Test suite execution started	------------------------\n\n");

		}

		return functionList;
	}

	public ArrayList<List<String>> parseIndexedFunction(String functionName, int skipIndex, int lineCount) {

		Scanner scanner = null;
		String fnCSV = (StaticUtilities.FUNCTION_CSV);

		// executionLog.debug("Indexed Parsing of (" + fnCSV+
		// ") File has been started");
		// executionLog.debug("Indexed Parsing of (" + fnCSV+
		// ") File has been started with index (" + skipIndex +
		// ") String Value (" + String.valueOf(skipIndex) + ").");

		ArrayList<List<String>> functionList = new ArrayList<List<String>>();
		try {
			scanner = new Scanner(new File(fnCSV));
			for (int loopIndex = 0; loopIndex < skipIndex; loopIndex++) {

				scanner.nextLine();
			}
			// .skip("(?:.*\\r?\\n|\\r){"
			// + String.valueOf(skipIndex) + "}");
			// scanner.nextLine();
			int currentLineCount = 0;
			while (scanner.hasNext()) {

				List<String> line = parseLine(scanner.nextLine());
				if (line.get(StaticUtilities.FUNCTION_TC_NAME_INDEX).equalsIgnoreCase(functionName)) {
					functionList.add(line);
				} else {
					// executionLog.debug("Function Name Parsed ("+
					// line.get(StaticUtilities.FUNCTION_TC_NAME_INDEX)+
					// ") is other than the one specified ("+ functionName +
					// "). Hence terminating the loop");
					break;
				}
				currentLineCount++;
				if (currentLineCount >= lineCount) {

					executionLog.info("Function Parsing has been stopped, since partial execution is selected");
					break;
				}
			}
		} catch (FileNotFoundException e) {

			executionLog.error("File named (" + fnCSV + ") was not found");
			e.printStackTrace();
		} finally {

			scanner.close();
			// executionLog.info("Function Parsing has been completed.");
			//
			// if (executionLog.isDebugEnabled()) {
			//
			// executionLog.debug("Following is the function Details Obtained ("+
			// functionList + ")");
			// }
		}
		return functionList;
	}

	public static List<String> parseLine(String cvsLine) {
		return parseLine(cvsLine, StaticUtilities.DEFAULT_SEPARATOR, StaticUtilities.DEFAULT_QUOTE);
	}

	public static List<String> parseLine(String cvsLine, char separators) {
		return parseLine(cvsLine, separators, StaticUtilities.DEFAULT_QUOTE);
	}

	@SuppressWarnings("null")
	public static List<String> parseLine(String cvsLine, char separators, char customQuote) {

		List<String> result = new ArrayList<>();

		// if empty, return!
		if (cvsLine == null && cvsLine.isEmpty()) {
			return result;
		}

		if (customQuote == ' ') {
			customQuote = StaticUtilities.DEFAULT_QUOTE;
		}

		if (separators == ' ') {
			separators = StaticUtilities.DEFAULT_SEPARATOR;
		}

		StringBuffer curVal = new StringBuffer();
		boolean inQuotes = false;
		boolean startCollectChar = false;
		boolean doubleQuotesInColumn = false;

		char[] chars = cvsLine.toCharArray();

		for (char ch : chars) {

			if (inQuotes) {
				startCollectChar = true;
				if (ch == customQuote) {
					inQuotes = false;
					doubleQuotesInColumn = false;
				} else {

					// Fixed : allow "" in custom quote enclosed
					if (ch == '\"') {
						if (!doubleQuotesInColumn) {
							curVal.append(ch);
							doubleQuotesInColumn = true;
						}
					} else {
						curVal.append(ch);
					}

				}
			} else {
				if (ch == customQuote) {

					inQuotes = true;

					// Fixed : allow "" in empty quote enclosed
					if (chars[0] != '"' && customQuote == '\"') {
						curVal.append('"');
					}

					// double quotes in column will hit this!
					if (startCollectChar) {
						curVal.append('"');
					}

				} else if (ch == separators) {

					result.add(curVal.toString());

					curVal = new StringBuffer();
					startCollectChar = false;

				} else if (ch == '\r') {
					// ignore LF characters
					continue;
				} else if (ch == '\n') {
					// the end, break!
					break;
				} else {
					curVal.append(ch);
				}
			}

		}

		result.add(curVal.toString());

		return result;
	}
}
