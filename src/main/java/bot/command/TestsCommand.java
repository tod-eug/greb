package bot.command;

import bot.Constants;
import bot.keyboards.TestsKeyboard;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import sheets.SheetsUtil;

import java.util.List;

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
        List<String> tests = sheetsUtil.getTests();

        String testsList = "";
        if (!tests.isEmpty()) {
            for (String s : tests) {
                testsList = testsList + "- " + s + "\n";
            }
        }

        MessageProcessor mp = new MessageProcessor();
        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId());
        sm.setText(Constants.TESTS_COMMAND + testsList);
        sm.setReplyMarkup(TestsKeyboard.getTestsKeyboard(tests, message.getFrom()));
        mp.sendMsg(absSender, sm);
    }
}
