package bot.command;

import bot.GrammarBot;
import bot.ReplyConstants;
import bot.enums.State;
import bot.keyboards.CategoriesKeyboard;
import bot.keyboards.TestsKeyboard;
import dto.Test;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import sheets.SheetsUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestsCommand implements IBotCommand {
    @Override
    public String getCommandIdentifier() {
        return "tests";
    }

    @Override
    public String getDescription() {
        return "tests";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SheetsUtil sheetsUtil = new SheetsUtil();
        Map<String, List<Test>> categoriesWithTests = sheetsUtil.getTestCategories();

        String categoriesList = "";
        Set<String> categoriesSet = categoriesWithTests.keySet();
        if (!categoriesSet.isEmpty()) {
            for (String s : categoriesSet) {
                categoriesList = categoriesList + s + "\n";
            }
        }
        GrammarBot.stateMap.put(message.getFrom().getId(), State.TEST_CHOOSING);

        MessageProcessor mp = new MessageProcessor();
        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId());
        sm.setText(ReplyConstants.TESTS_COMMAND + categoriesList);
        sm.setReplyMarkup(CategoriesKeyboard.getTestsKeyboard(categoriesSet, message.getFrom()));
        mp.sendMsg(absSender, sm);







//        List<Test> tests = sheetsUtil.getTests();
//
//        String testsList = "";
//        if (!tests.isEmpty()) {
//            for (Test s : tests) {
//                testsList = testsList + s.getCode() + " - " + s.getName() + "\n";
//            }
//        }
//
//        MessageProcessor mp = new MessageProcessor();
//        SendMessage sm = new SendMessage();
//        sm.setChatId(message.getChatId());
//        sm.setText(ReplyConstants.TESTS_COMMAND + testsList);
//        sm.setReplyMarkup(TestsKeyboard.getTestsKeyboard(tests, message.getFrom()));
//        mp.sendMsg(absSender, sm);
    }
}
