package bot.keyboards;

import bot.SysConstants;
import dto.Test;
import org.apache.commons.collections4.ListUtils;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class TestsKeyboard {

    public static InlineKeyboardMarkup getTestsKeyboard(List<Test> tests, Long userId, String testChooseTimestamp) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        if (tests != null) {
            if (!tests.isEmpty()) {
                List<List<Test>> list = ListUtils.partition(tests, 5);
                for (List<Test> l : list) {
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();
                    for (Test s: l) {
                        InlineKeyboardButton button = new InlineKeyboardButton();
                        button.setText(s.getName());
                        button.setCallbackData(SysConstants.TESTS_CALLBACK_TYPE + SysConstants.DELIMITER_FOR_TESTS_CALLBACK +
                                userId + SysConstants.DELIMITER_FOR_TESTS_CALLBACK +
                                s.getCode() + SysConstants.DELIMITER_FOR_TESTS_CALLBACK +
                                testChooseTimestamp + SysConstants.DELIMITER_FOR_TESTS_CALLBACK +
                                System.currentTimeMillis() / 1000);
                        rowInline.add(button);
                    }
                    rowsInline.add(rowInline);
                }
            }
        }

        keyboardMarkup.setKeyboard(rowsInline);
        return keyboardMarkup;
    }
}
