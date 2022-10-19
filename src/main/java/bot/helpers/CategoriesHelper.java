package bot.helpers;

import bot.ReplyConstants;
import bot.keyboards.TestsKeyboard;
import dto.Test;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.List;

public class CategoriesHelper {

    public EditMessageText getSendTestsListMessage(Long chatId, Long userId, int messageId, List<Test> tests, String testChooseTimestamp) {

        String testsList = "";
        if (!tests.isEmpty()) {
            for (Test s : tests) {
                testsList = testsList + s.getName() + "\n";
            }
        }

        EditMessageText em = new EditMessageText();
        em.setMessageId(messageId);
        em.setChatId(chatId);
        em.setText(ReplyConstants.LIST_OF_TESTS_IN_CATEGORY + testsList);
        em.setReplyMarkup(TestsKeyboard.getTestsKeyboard(tests, userId, testChooseTimestamp));
        return em;
    }
}
