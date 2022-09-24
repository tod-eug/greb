package bot.command;

import bot.ReplyConstants;
import db.UsersHelper;
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
        sm.setText(ReplyConstants.START_REPLY_WELCOME + ReplyConstants.USE_TESTS_COMMAND);
        mp.sendMsg(absSender, sm);

        UsersHelper uh = new UsersHelper();
        String userId = uh.findUserByTgId(message.getFrom().getId().toString(), message.getFrom());
    }
}
