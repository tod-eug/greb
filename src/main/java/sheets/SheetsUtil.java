package sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import exceptions.ExecutingSheetException;
import sheets.authorization.GoogleAuthorizeProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SheetsUtil {

    private static String SPREADSHEET_ID = "1xXJgahgs3noBCc2SiMelmdqgjLll6MOY1AzhpaqloUE";

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

    public List<String> getTests() {
        List<String> ranges = Arrays.asList("tests!A1:A4");
        BatchGetValuesResponse readResult;
        try {
            readResult = sheet.spreadsheets().values().batchGet(SPREADSHEET_ID).setRanges(ranges).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> result = new ArrayList<>();
        List<List<Object>> l = readResult.getValueRanges().get(0).getValues();
        for (List<Object> objects : l) {
            result.add(objects.get(0).toString());
        }
        return result;
    }
}
