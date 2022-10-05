package bot;

import bot.keyboards.CategoriesKeyboard;
import bot.keyboards.TestsKeyboard;
import dto.ChoosingTestState;
import dto.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import sheets.SheetsUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoriesHelper {

    public SendMessage getChooseCategoryMessage(Update update, ChoosingTestState choosingTestState) {

        SheetsUtil sheetsUtil = new SheetsUtil();
        Map<String, List<Test>> categories =  sheetsUtil.getTestCategories();
        String categoriesList = "";
        Set<String> set = categories.keySet();
        if (!set.isEmpty()) {
            for (String s : set) {
                categoriesList = categoriesList + s + "\n";
            }
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setText(ReplyConstants.LIST_OF_CATEGORIES + categoriesList);
        sendMessage.setReplyMarkup(CategoriesKeyboard.getCategoriesKeyboard(set, update.getMessage().getFrom()));

        choosingTestState.setCategories(categories);
        GrammarBot.choosingStateMap.put(update.getCallbackQuery().getFrom().getId(), choosingTestState);
        return sendMessage;
    }

    public SendMessage getSendTestsListMessage(String category, Update update, ChoosingTestState choosingTestState) {

        List<Test> tests = choosingTestState.getCategories().get(category);

        String testsList = "";
        if (!tests.isEmpty()) {
            for (Test s : tests) {
                testsList = testsList + s.getCode() + " - " + s.getName() + "\n";
            }
        }

        SendMessage sm = new SendMessage();
        sm.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sm.setText(ReplyConstants.TESTS_COMMAND + testsList);
        sm.setReplyMarkup(TestsKeyboard.getTestsKeyboard(tests, update.getCallbackQuery().getMessage().getFrom()));
        return sm;
    }
}
