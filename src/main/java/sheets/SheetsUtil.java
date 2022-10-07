package sheets;

import bot.enums.TestType;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import dto.TestQuestion;
import mapper.TestQuestionMapper;
import sheets.authorization.GoogleAuthorizeProvider;

import java.io.IOException;
import java.util.*;

public class SheetsUtil {

    private static final String SPREADSHEET_ID = "1xXJgahgs3noBCc2SiMelmdqgjLll6MOY1AzhpaqloUE";

    public static Map<String, List<String>> categories = new HashMap<>(); //category, list of test codes
    public static Map<String, List<TestQuestion>> tests = new HashMap<>(); //test code, test

    private static final String TESTS_LIST_NAME = "tests";
    private static final String LATEST_COLUMN_TO_GET = "Z";
    private static final int HOW_MANY_ROWS_TO_GET = 500;

    private static final String SYMBOL_TEST_LINE = "1";
    private static final String SYMBOL_HEADER_LINE = "2";
    private static final String SYMBOL_QUESTION_LINE = "3";
    private static final String SYMBOL_END_LINE = "4";

    private static Sheets sheet = GoogleAuthorizeProvider.getSheet();


    public static void setup() {
        getCategories();
        getTests();
    }

    private static void getCategories() {
        List<String> ranges = Arrays.asList(TESTS_LIST_NAME + "!A1:"+ LATEST_COLUMN_TO_GET + HOW_MANY_ROWS_TO_GET);
        BatchGetValuesResponse readResult = getResponseFromSheet(ranges);

        List<List<Object>> l = readResult.getValueRanges().get(0).getValues();
        for (int i = 0; i < l.size(); i++) {
            List<String> list = new ArrayList<>();
            String category = l.get(i).get(0).toString();
            for (int j = 0; j < l.get(i).size(); j++) {
                if (j == 0) //first column is category
                    continue;
                if (l.get(i).get(j) != null) {
                    list.add(l.get(i).get(j).toString());
                }
            }
            categories.put(category, list);
        }
    }

    public static void getTests() {
        List<String> ranges = new ArrayList<>();

        //create list to request all tests from sheet
        Set<String> setCategories = categories.keySet();
        for (String s : setCategories) {
            ranges.add(s + "!A1:" + LATEST_COLUMN_TO_GET + HOW_MANY_ROWS_TO_GET);
        }

        //get raw data from sheet
        BatchGetValuesResponse readResult = getResponseFromSheet(ranges);

        TestQuestionMapper testQuestionMapper = new TestQuestionMapper();
        List<TestQuestion> test = new ArrayList<>();
        String testCode = "";
        String testTask = "";
        String testName = "";
        String testType = "";
        String article = "";
        String answerWriting = "";
        TestType testTypeMapped = null;

        List<ValueRange> valueRanges = readResult.getValueRanges();
        for (ValueRange vr: valueRanges) { //iterate by lists from sheet
            List<List<Object>> l = vr.getValues();
            for (int i = 0; i < l.size(); i++) {
                Map<String, String> optionMap = new HashMap<>();
                if (l.get(i).size() > 0) {
                    if (l.get(i).get(0).toString().equals(SYMBOL_TEST_LINE)) {  //this line is test parameters
                        testCode = l.get(i).get(1).toString();
                        testTask = l.get(i).get(2).toString();
                        testName = l.get(i).get(3).toString();
                        testType = l.get(i).get(4).toString();
                        testTypeMapped = testQuestionMapper.mapTestType(testType);
                    } else if (l.get(i).get(0).toString().equals(SYMBOL_HEADER_LINE)) {
                        continue;
                    } else if (l.get(i).get(0).toString().equals(SYMBOL_QUESTION_LINE)) {
                        article = l.get(i).get(1).toString();
                        if (testTypeMapped == TestType.normalWriting || testTypeMapped == TestType.articleWriting) //if answer should be written then no need to map options
                            answerWriting = l.get(i).get(3).toString();
                        else {
                            for (int k = 4; k < l.get(i).size(); k++) {
                                optionMap.put(l.get(i - 1).get(k).toString(), l.get(i).get(k).toString());
                            }
                        }
                        test.add(testQuestionMapper.mapQuestion(testType, testName, testTask, article, l.get(i).get(2).toString(), l.get(i).get(3).toString(), answerWriting, optionMap));
                    } else if (l.get(i).get(0).toString().equals(SYMBOL_END_LINE)) {
                        tests.put(testCode, test);
                        test = new ArrayList<>();
                    }
                }
            }
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
