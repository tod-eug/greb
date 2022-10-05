package sheets;

import bot.SysConstants;
import bot.enums.TestType;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import dto.TestQuestion;
import dto.Test;
import exceptions.ExecutingSheetException;
import mapper.TestQuestionMapper;
import mapper.TestMapper;
import sheets.authorization.GoogleAuthorizeProvider;

import java.io.IOException;
import java.util.*;

public class SheetsUtil {

    private static final String SPREADSHEET_ID = "1xXJgahgs3noBCc2SiMelmdqgjLll6MOY1AzhpaqloUE";

    private static final String TESTS_LIST_NAME = "tests";
    private static final int LINE_USED_FOR_ONE_TEST = 3;
    private static final int SPACE_BETWEEN_TESTS_LINES = 3;
    private static final String LATEST_COLUMN_TO_GET = "Z";
    private static final int HOW_MANY_ROWS_TO_GET = 100;


    private Sheets sheet = GoogleAuthorizeProvider.getSheet();

    public UpdateValuesResponse writeSheet(ValueRange value, String range, String valueInputOption) {
        UpdateValuesResponse result = null;
        try {
            return result = sheet.spreadsheets().values()
                    .update(SPREADSHEET_ID, range, value)
                    .setValueInputOption(valueInputOption)
                    .execute();
        } catch (IOException e) {
            throw new ExecutingSheetException("Error when writing sheet");
        }
    }

    public Map<String, List<Test>> getTestCategories() {
        List<String> ranges = Arrays.asList(TESTS_LIST_NAME + "!A1:"+ LATEST_COLUMN_TO_GET + HOW_MANY_ROWS_TO_GET);
        BatchGetValuesResponse readResult = getResponseFromSheet(ranges);

        TestMapper mapper = new TestMapper();
        Map<String, List<Test>> result = new HashMap<>();
        List<List<Object>> l = readResult.getValueRanges().get(0).getValues();
        for (int i = 0; i < l.size(); i++) {
            List<Test> list = new ArrayList<>();
            if ((i % ( LINE_USED_FOR_ONE_TEST + SPACE_BETWEEN_TESTS_LINES)) == 0 ) {
                String category = l.get(i).get(0).toString();
                if (category.equals(SysConstants.IGNORE_CATEGORY_STRING))
                    break;
                for (int j = 0; j < l.get(i).size() - 1; j++) { //l.get(i).size() - 1 because first column is category
                    if (l.get(i).get(j + 1) != null) {
                        Test test = mapper.mapTest(l.get(i).get(j + 1).toString(), l.get(i + 1).get(j + 1).toString(), l.get(i + 2).get(j + 1).toString());
                        if (!test.getName().equals(""))
                            list.add(test);
                    }
                }
                result.put(category, list);
            }
        }
        return result;
    }

    public List<Test> getTests() {
        List<String> ranges = Arrays.asList(TESTS_LIST_NAME + "!A1:"+ LATEST_COLUMN_TO_GET + HOW_MANY_ROWS_TO_GET);
        BatchGetValuesResponse readResult = getResponseFromSheet(ranges);

        TestMapper mapper = new TestMapper();
        List<Test> result = new ArrayList<>();
        List<List<Object>> l = readResult.getValueRanges().get(0).getValues();
        for (int i = 0; i < l.size(); i++) {
            for (int j = 0; j < l.get(i).size(); j++) {
                if ((i % ( LINE_USED_FOR_ONE_TEST + SPACE_BETWEEN_TESTS_LINES)) == 0 ) {
                    if (l.get(i).get(j) != null) {
                        Test test = mapper.mapTest(l.get(i).get(j).toString(), l.get(i + 1).get(j).toString(), l.get(i + 2).get(j).toString());
                        if (!test.getName().equals(""))
                            result.add(test);
                    }
                }
            }
        }
        return result;
    }

    public List<TestQuestion> getTest(String testCode) {
        List<String> ranges = Arrays.asList(testCode + "!A1:"+ LATEST_COLUMN_TO_GET + HOW_MANY_ROWS_TO_GET);
        BatchGetValuesResponse readResult = getResponseFromSheet(ranges);

        TestQuestionMapper testQuestionMapper = new TestQuestionMapper();
        List<TestQuestion> result = new ArrayList<>();
        String testType = "";
        String article = "";
        String answerWriting = "";
        TestType testTypeMapped = null;
        Map<String, String> optionMap = new HashMap<>();

        List<List<Object>> l = readResult.getValueRanges().get(0).getValues();
        for (int i = 0; i < l.size(); i++) {
            if (i == 0) {  //first line is test type
                testType = l.get(i).get(0).toString();
                testTypeMapped = testQuestionMapper.mapTestType(testType);
                continue;
            }
            if (i == 1) //second line is header
                continue;
            if (i == 2) //article is always in third row and first column
                article = l.get(i).get(0).toString();
            if (testTypeMapped == TestType.normalWriting || testTypeMapped == TestType.articleWriting) //if answer should be written then no need to map options
                answerWriting = l.get(i).get(2).toString();
            else {
                for (int k = 3; k < l.get(i).size(); k++) {
                    optionMap.put(l.get(1).get(k).toString(), l.get(i).get(k).toString());
                }
            }
            result.add(testQuestionMapper.mapQuestion(testType, article, l.get(i).get(1).toString(), l.get(i).get(2).toString(), answerWriting, optionMap));
        }
        return result;
    }

    private BatchGetValuesResponse getResponseFromSheet(List<String> ranges) {
        BatchGetValuesResponse readResult;
        try {
            readResult = sheet.spreadsheets().values().batchGet(SPREADSHEET_ID).setRanges(ranges).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return readResult;
    }
}
