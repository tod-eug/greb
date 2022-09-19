package sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import dto.Test;
import exceptions.ExecutingSheetException;
import mapper.TestMapper;
import sheets.authorization.GoogleAuthorizeProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SheetsUtil {

    private static String SPREADSHEET_ID = "1xXJgahgs3noBCc2SiMelmdqgjLll6MOY1AzhpaqloUE";

    private static String TESTS_LIST_NAME = "tests";
    private static int LINE_USED_FOR_ONE_TEST = 3;
    private static int SPACE_BETWEEN_TESTS_LINES = 3;
    private static String LATEST_COLUMN_TO_GET = "Z";
    private static int HOW_MANY_ROWS_TO_GET = 30;


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
        int end = 1 + HOW_MANY_ROWS_TO_GET;
        List<String> ranges = Arrays.asList(TESTS_LIST_NAME + "!A1:"+ LATEST_COLUMN_TO_GET + end);
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
}
