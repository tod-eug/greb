package bot;

import bot.keyboards.TestsKeyboard;
import dto.ChoosingTestState;
import dto.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class CategoriesHelper {

    public SendMessage getSendTestsListMessage(String category, Update update, ChoosingTestState choosingTestState) {

        List<Test> tests = choosingTestState.getCategories().get(category);

        String testsList = "";
        if (!tests.isEmpty()) {
            for (Test s : tests) {
                testsList = testsList + s.getName() + "\n";
            }
        }

        SendMessage sm = new SendMessage();
        sm.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sm.setText(ReplyConstants.LIST_OF_TESTS_IN_CATEGORY + testsList);
        sm.setReplyMarkup(TestsKeyboard.getTestsKeyboard(tests, update.getCallbackQuery().getMessage().getFrom()));
        return sm;
    }
}
