package bot.helpers;

import bot.ReplyConstants;
import bot.keyboards.TestsKeyboard;
import dto.Test;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.List;

public class CategoriesHelper {

    public EditMessageText getSendTestsListMessage(Long chatId, Long userId, int messageId, String category, List<Test> tests, String testChooseTimestamp) {

        String testsList = "";
        if (!tests.isEmpty()) {
            for (Test s : tests) {
                testsList = testsList + s.getName() + "\n";
            }
        }

        EditMessageText em = new EditMessageText();
        em.setMessageId(messageId);
        em.setChatId(chatId);
        em.setText(ReplyConstants.LIST_OF_TESTS_IN_CATEGORY_1 + category + ReplyConstants.LIST_OF_TESTS_IN_CATEGORY_2 + testsList);
        em.setParseMode(ParseMode.HTML);
        em.setReplyMarkup(TestsKeyboard.getTestsKeyboard(tests, userId, testChooseTimestamp));
        return em;
    }
}
