package bot.helpers;

import bot.ReplyConstants;
import bot.keyboards.CategoriesKeyboard;
import bot.keyboards.TestsKeyboard;
import dto.Test;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoriesHelper {

    public SendMessage getInitialTestingProcessMessage(Long userId, Long chatId, Map<String, List<Test>> categories, String categoryChooseTimestamp) {
        String categoriesList = "";
        Set<String> set = categories.keySet();
        if (!set.isEmpty()) {
            for (String s : set) {
                categoriesList = categoriesList + s + "\n";
            }
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(ReplyConstants.TESTS_COMMAND + categoriesList);
        sendMessage.setReplyMarkup(CategoriesKeyboard.getCategoriesKeyboard(set, userId, categoryChooseTimestamp));
        return sendMessage;
    }

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
