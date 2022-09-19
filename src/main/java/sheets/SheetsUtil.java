package sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import dto.NormalTestQuestion;
import dto.Test;
import exceptions.ExecutingSheetException;
import mapper.NormalTestQuestionMapper;
import mapper.TestMapper;
import sheets.authorization.GoogleAuthorizeProvider;

import java.io.IOException;
import java.util.*;

public class SheetsUtil {

    private static String SPREADSHEET_ID = "1xXJgahgs3noBCc2SiMelmdqgjLll6MOY1AzhpaqloUE";

    private static String TESTS_LIST_NAME = "tests";
    private static int LINE_USED_FOR_ONE_TEST = 3;
    private static int SPACE_BETWEEN_TESTS_LINES = 3;
    private static String LATEST_COLUMN_TO_GET = "Z";
    private static int HOW_MANY_ROWS_TO_GET = 100;


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

    public List<Test> getTests() {
        List<String> ranges = Arrays.asList(TESTS_LIST_NAME + "!A1:"+ LATEST_COLUMN_TO_GET + HOW_MANY_ROWS_TO_GET);
        BatchGetValuesResponse readResult;
        try {
            readResult = sheet.spreadsheets().values().batchGet(SPREADSHEET_ID).setRanges(ranges).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

    public List<NormalTestQuestion> getNormalTest(String testCode) {
        List<String> ranges = Arrays.asList(testCode + "!A1:"+ LATEST_COLUMN_TO_GET + HOW_MANY_ROWS_TO_GET);
        BatchGetValuesResponse readResult;
        try {
            readResult = sheet.spreadsheets().values().batchGet(SPREADSHEET_ID).setRanges(ranges).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        NormalTestQuestionMapper normalTestQuestionMapper = new NormalTestQuestionMapper();
        List<NormalTestQuestion> result = new ArrayList<>();
        List<List<Object>> l = readResult.getValueRanges().get(0).getValues();
        for (int i = 0; i < l.size(); i++) {
            for (int j = 0; j < l.get(i).size(); j++) {
                if (i == 0) //first line is header
                    continue;
                Map<String, String> optionMap = new HashMap<>();
                for (int k = 2; k < l.get(i).size(); k++) {
                    optionMap.put(l.get(0).get(k).toString(), l.get(i).get(k).toString());
                }
                result.add(normalTestQuestionMapper.mapQuestion(l.get(i).get(0).toString(), l.get(i).get(1).toString(), optionMap));
            }
        }
        return result;
    }
}
