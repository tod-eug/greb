package bot.keyboards;

import bot.SysConstants;
import org.apache.commons.collections4.ListUtils;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CategoriesKeyboard {

    public static ReplyKeyboard getCategoriesKeyboard(Set<String> categories, Long userId, String categoryChooseTimestamp) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<String> listFromSet = new ArrayList<>();
        listFromSet.addAll(categories);

        if (categories != null) {
            if (!categories.isEmpty()) {
                List<List<String>> list = ListUtils.partition(listFromSet, 2);
                for (List<String> l : list) {
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();
                    for (String s : l) {
                        InlineKeyboardButton button = new InlineKeyboardButton();
                        button.setText(s);
                        button.setCallbackData(SysConstants.CATEGORIES_CALLBACK_TYPE + SysConstants.DELIMITER_FOR_CATEGORIES_CALLBACK +
                                userId + SysConstants.DELIMITER_FOR_CATEGORIES_CALLBACK +
                                s + SysConstants.DELIMITER_FOR_CATEGORIES_CALLBACK +
                                categoryChooseTimestamp + SysConstants.DELIMITER_FOR_CATEGORIES_CALLBACK +
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
