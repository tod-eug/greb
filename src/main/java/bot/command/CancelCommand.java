package bot.command;

import bot.GrammarBot;
import bot.ReplyConstants;
import bot.enums.State;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class CancelCommand implements IBotCommand {
    @Override
    public String getCommandIdentifier() {
        return "cancel";
    }

    @Override
    public String getDescription() {
        return "cancel";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        MessageProcessor mp = new MessageProcessor();
        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId());
        sm.setText(ReplyConstants.CANCEL_REPLY_RESET);
        mp.sendMsg(absSender, sm);
        //ToDo delete all messages from previous test/category choosing

        GrammarBot.stateMap.put(message.getFrom().getId(), State.FREE);
        GrammarBot.choosingStateMap.remove(message.getFrom().getId());
        GrammarBot.processingStateMap.remove(message.getFrom().getId());

    }
}
