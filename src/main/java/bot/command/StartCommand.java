package bot.command;

import bot.GrammarBot;
import bot.ReplyConstants;
import bot.helpers.CategoriesHelper;
import db.UsersHelper;
import dto.Test;
import dto.TestState;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import sheets.SheetsUtil;

import java.util.List;
import java.util.Map;

public class StartCommand implements IBotCommand {
    @Override
    public String getCommandIdentifier() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "start";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        MessageProcessor mp = new MessageProcessor();
        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId());
        sm.setText(ReplyConstants.START_REPLY_WELCOME);
        mp.sendMsg(absSender, sm);

        // initiate new test attempt
        Map<String, List<Test>> categories =  SheetsUtil.getTests();
        TestState ts = new TestState(message.getFrom().getId(), categories);

        CategoriesHelper ch = new CategoriesHelper();
        ts.setTestsMessageId(mp.sendAndReturnMessageID(absSender, ch.getInitialTestingProcessMessage(message.getFrom().getId(), message.getChatId(), categories, ts.getCategoryChooseTimestamp())));
        GrammarBot.stateMap.put(message.getFrom().getId(), ts);

        UsersHelper uh = new UsersHelper();
        String userId = uh.findUserByTgId(message.getFrom().getId().toString(), message.getFrom(), message.getChatId().toString());
    }
}
