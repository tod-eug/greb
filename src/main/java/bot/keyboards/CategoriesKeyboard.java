package bot.keyboards;

import bot.SysConstants;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CategoriesKeyboard {

    public static ReplyKeyboard getTestsKeyboard(Set<String> categories, User user) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        if (categories != null) {
            if (!categories.isEmpty()) {
                for (String s : categories) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(s);
                    button.setCallbackData(SysConstants.CATEGORIES_CALLBACK_TYPE + SysConstants.DELIMITER_FOR_CATEGORIES_CALLBACK +
                            user.getId() + SysConstants.DELIMITER_FOR_CATEGORIES_CALLBACK +
                            s + SysConstants.DELIMITER_FOR_CATEGORIES_CALLBACK +
                            System.currentTimeMillis() / 1000);
                    rowInline.add(button);
                }
            }
        }
        rowsInline.add(rowInline);
        keyboardMarkup.setKeyboard(rowsInline);
        return keyboardMarkup;
    }
}
