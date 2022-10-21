package bot.keyboards;

import bot.SysConstants;
import bot.enums.Option;
import org.apache.commons.collections4.ListUtils;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

public class OptionsKeyboard {

    public static InlineKeyboardMarkup getOptionKeyboard(Map<Option, String> options, String attemptCode, int currentQuestion) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        if (options != null) {
            if (!options.isEmpty()) {
                Set<Option> optionsSet = options.keySet();
                //define 2 biggest options length
                int maxOptionLength = 0;
                int secondOptionLength = 0;
                for (Option o: optionsSet) {
                    if (options.get(o).length() > maxOptionLength)
                        maxOptionLength = options.get(o).length();
                    if (options.get(o).length() < maxOptionLength && options.get(o).length() > secondOptionLength)
                        secondOptionLength = options.get(o).length();
                }

                //if they are long enough then split the options to lines
                int linesForOptions = 0;
                if (maxOptionLength > SysConstants.BIGGEST_LENGTH_OF_OPTION_TO_LINE_BREAK) {
                    if (secondOptionLength > SysConstants.SECOND_BIGGEST_LENGTH_OF_OPTION_TO_LINE_BREAK)
                        linesForOptions = 1;
                    else
                        linesForOptions = 2;
                } else
                    linesForOptions = 3;


                List<Option> listFromSet = new ArrayList<>();
                listFromSet.addAll(optionsSet);
                List<List<Option>> list = ListUtils.partition(listFromSet, linesForOptions);

                for (List<Option> l : list) {
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();
                    for (Option o : l) {
                        InlineKeyboardButton button = new InlineKeyboardButton();
                        button.setText(options.get(o));
                        button.setCallbackData(SysConstants.QUESTIONS_CALLBACK_TYPE + SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK +
                                attemptCode + SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK +
                                currentQuestion + SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK +
                                o.name());
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
