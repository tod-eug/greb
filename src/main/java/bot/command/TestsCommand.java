package bot.command;

import bot.GrammarBot;
import bot.ReplyConstants;
import bot.enums.State;
import bot.keyboards.CategoriesKeyboard;
import dto.ChoosingTestState;
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
        Map<String, List<Test>> categories =  sheetsUtil.getTestCategories();
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
        sendMessage.setReplyMarkup(CategoriesKeyboard.getCategoriesKeyboard(set, message.getFrom()));
        mp.sendMsg(absSender, sendMessage);

        ChoosingTestState choosingTestState = new ChoosingTestState();
        choosingTestState.setCategories(categories);
        GrammarBot.choosingStateMap.put(message.getFrom().getId(), choosingTestState);
        GrammarBot.stateMap.put(message.getFrom().getId(), State.CHOOSING_TEST);
    }
}
