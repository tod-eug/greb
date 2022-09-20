package bot.keyboards;

import bot.enums.Option;
import dto.CurrentUserTestState;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

public class OptionsKeyboard {

    public static ReplyKeyboard getOptionKeyboard(CurrentUserTestState currentUserTestState) {
        Map<Option, String> options = currentUserTestState.getTest().get(currentUserTestState.getCurrentQuestion()).getOptions();

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        if (options != null) {
            if (!options.isEmpty()) {
                Set<Option> optionsSet = options.keySet();
                for (Option o : optionsSet) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(options.get(o));
                    button.setCallbackData("answer" + ":" + currentUserTestState.getAttemptCode() + ":" + currentUserTestState.getCurrentQuestion() + ":" + o.name());
                    rowInline.add(button);
                }
            }
        }

        rowsInline.add(rowInline);
        keyboardMarkup.setKeyboard(rowsInline);
        return keyboardMarkup;
    }
}
