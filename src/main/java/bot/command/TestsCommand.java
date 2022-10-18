package bot.command;

import bot.ReplyConstants;
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

        Map<String, List<Test>> categories =  SheetsUtil.getTests();
        String categoriesList = "";
        Set<String> set = categories.keySet();
        if (!set.isEmpty()) {
            for (String s : set) {
                categoriesList = categoriesList + s + "\n";
            }
        }

        MessageProcessor mp = new MessageProcessor();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(ReplyConstants.TESTS_COMMAND + categoriesList);
        mp.sendMsg(absSender, sendMessage);
    }
}
