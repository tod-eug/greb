package bot.keyboards;

import com.ibm.icu.text.Transliterator;
import dto.Test;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class TestsKeyboard {

    public static ReplyKeyboard getTestsKeyboard(List<Test> tests, User user) {
        Transliterator toLatinTrans = Transliterator.getInstance("Russian-Latin/BGN");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        if (tests != null) {
            if (!tests.isEmpty()) {
                for (Test s : tests) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(s.getCode());
                    button.setCallbackData("test" + "-" +user.getId() + "-" + toLatinTrans.transliterate(s.getCode()));
                    rowInline.add(button);
                }
            }
        }

        rowsInline.add(rowInline);
        keyboardMarkup.setKeyboard(rowsInline);
        return keyboardMarkup;
    }
}
