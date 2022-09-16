package bot.command;

import bot.Constants;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

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
        sm.setText(Constants.START_REPLY_WELCOME + Constants.USE_TESTS_COMMAND);
        mp.sendMsg(absSender, sm);
    }
}
