package bot.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MessageProcessor {

    protected void sendMsg(AbsSender absSender, SendMessage sm) {
        try {
            absSender.execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    protected int sendAndReturnMessageID(AbsSender absSender, SendMessage sm) {
        int messageId = 0;
        try {
            messageId = absSender.execute(sm).getMessageId();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return messageId;
    }

    protected void deleteMsg(AbsSender absSender, DeleteMessage dm) {
        try {
            absSender.execute(dm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
