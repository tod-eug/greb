package sheets;

import bot.enums.TestType;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import dto.Test;
import dto.TestQuestion;
import mapper.TestMapper;
import mapper.TestQuestionMapper;
import sheets.authorization.GoogleAuthorizeProvider;

import java.io.IOException;
import java.util.*;

public class SheetsUtil {

    private static final String SPREADSHEET_ID = "1xXJgahgs3noBCc2SiMelmdqgjLll6MOY1AzhpaqloUE";

    private static final Map<String, List<Test>> tests = new HashMap<>(); //String category, list of tests

    private static final String TESTS_LIST_NAME = "tests";
    private static final String LATEST_COLUMN_TO_GET = "Z";
    private static final int HOW_MANY_ROWS_TO_GET = 500;

    private static final String SYMBOL_CATEGORY_LINE = "0";
    private static final String SYMBOL_TEST_LINE = "1";
    private static final String SYMBOL_HEADER_LINE = "2";
    private static final String SYMBOL_QUESTION_LINE = "3";
    private static final String SYMBOL_END_LINE = "4";

    private static Sheets sheet = GoogleAuthorizeProvider.getSheet();

    public static void setup() {
        loadCategories();
    }

    public static Map<String, List<Test>> getTests() {
        return tests;
    }

    public static List<TestQuestion> getTest(String category, String testCode) {
        Test result = null;
        List<Test> test = tests.get(category);
        for (Test t: test) {
            if (t.getCode().equals(testCode)) {
                result = t;
                break;
            }
        }
        return result.getTestQuestion();
    }

    private static void loadCategories() {
        List<String> categoryRanges = Arrays.asList(TESTS_LIST_NAME + "!A1:"+ LATEST_COLUMN_TO_GET + HOW_MANY_ROWS_TO_GET);
        BatchGetValuesResponse readCategories = getResponseFromSheet(categoryRanges);

        Set<String> categories = new HashSet<>();
        List<List<Object>> l = readCategories.getValueRanges().get(0).getValues();
        for (int i = 0; i < l.size(); i++) {
            categories.add(l.get(i).get(0).toString());
        }

        List<String> testsRanges = new ArrayList<>();
        //create list to request all tests from sheet
        for (String s : categories) {
            testsRanges.add(s + "!A1:" + LATEST_COLUMN_TO_GET + HOW_MANY_ROWS_TO_GET);
        }

        //get raw data from sheet and parse result
        BatchGetValuesResponse readTests = getResponseFromSheet(testsRanges);
        parseTests(readTests.getValueRanges());
    }

    private static void parseTests(List<ValueRange> valueRanges) {
        TestMapper testMapper = new TestMapper();
        TestQuestionMapper testQuestionMapper = new TestQuestionMapper();
        List<Test> testsList = new ArrayList<>();
        List<TestQuestion> testQuestionsList = new ArrayList<>();
        String testCode = "";
        String testTask = "";
        String testName = "";
        String testType = "";
        String article = "";
        String answerWriting = "";
        TestType testTypeMapped = null;

        for (ValueRange vr: valueRanges) { //iterate by lists from sheet
            List<List<Object>> l = vr.getValues();
            String sheetCategory = "";
            int headerLineNumber = 0;
            for (int i = 0; i < l.size(); i++) {
                Map<String, String> optionMap = new HashMap<>();
                if (l.get(i).size() > 0) {
                    if (l.get(i).get(0).toString().equals(SYMBOL_CATEGORY_LINE)) {  //this line is category name
                        sheetCategory = l.get(i).get(1).toString();
                    } else if (l.get(i).get(0).toString().equals(SYMBOL_TEST_LINE)) {  //this line is test parameters
                        testCode = l.get(i).get(1).toString();
                        testTask = l.get(i).get(2).toString();
                        testName = l.get(i).get(3).toString();
                        testType = l.get(i).get(4).toString();
                        testTypeMapped = testQuestionMapper.mapTestType(testType);
                    } else if (l.get(i).get(0).toString().equals(SYMBOL_HEADER_LINE)) { //this line is header of the table
                        headerLineNumber = i;
                        continue;
                    } else if (l.get(i).get(0).toString().equals(SYMBOL_QUESTION_LINE)) { //this line is question line
                        article = l.get(i).get(1).toString();
                        if (testTypeMapped == TestType.normalWriting || testTypeMapped == TestType.articleWriting) //if answer should be written then no need to map options
                            answerWriting = l.get(i).get(3).toString();
                        else {
                            for (int k = 4; k < l.get(i).size(); k++) {
                                optionMap.put(l.get(headerLineNumber).get(k).toString(), l.get(i).get(k).toString());
                            }
                        }
                        testQuestionsList.add(testQuestionMapper.mapQuestion(testType, testName, testTask, article, l.get(i).get(2).toString(), l.get(i).get(3).toString(), answerWriting, optionMap));
                    } else if (l.get(i).get(0).toString().equals(SYMBOL_END_LINE)) { //this line is end of the test line, collect the results and create a test
                        testsList.add(testMapper.mapTest(testCode, testName, testType, testTask, testQuestionsList));
                        testQuestionsList = new ArrayList<>();
                    }
                }
            }
            tests.put(sheetCategory, testsList);
            testsList = new ArrayList<>();
        }
    }

    private static BatchGetValuesResponse getResponseFromSheet(List<String> ranges) {
        BatchGetValuesResponse readResult;
        try {
            readResult = sheet.spreadsheets().values().batchGet(SPREADSHEET_ID).setRanges(ranges).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return readResult;
    }
}
