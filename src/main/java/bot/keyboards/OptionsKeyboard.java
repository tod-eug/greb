package bot.keyboards;

import bot.SysConstants;
import bot.enums.Option;
import dto.ProcessingTestState;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

public class OptionsKeyboard {

    public static ReplyKeyboard getOptionKeyboard(ProcessingTestState processingTestState) {
        Map<Option, String> options = processingTestState.getTest().get(processingTestState.getCurrentQuestion()).getOptions();

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        if (options != null) {
            if (!options.isEmpty()) {
                Set<Option> optionsSet = options.keySet();
                for (Option o : optionsSet) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(options.get(o));
                    button.setCallbackData(SysConstants.QUESTIONS_CALLBACK_TYPE + SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK +
                            processingTestState.getAttemptCode() + SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK +
                            processingTestState.getCurrentQuestion() + SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK +
                            o.name());
                    rowInline.add(button);
                }
            }
        }

        rowsInline.add(rowInline);
        keyboardMarkup.setKeyboard(rowsInline);
        return keyboardMarkup;
    }
}
