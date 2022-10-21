package bot.command;

import dto.Executable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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

    protected void executeMessages(AbsSender sender, Executable executable) {
        if (executable.getDeleteMessages().size() > 0) {
            for (DeleteMessage dm : executable.getDeleteMessages()) {
                try {
                    sender.execute(dm);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        if (executable.getEditMessages().size() > 0) {
            for (EditMessageText em : executable.getEditMessages()) {
                try {
                    sender.execute(em);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        if (executable.getSendMessages().size() > 0) {
            for (SendMessage sm : executable.getSendMessages()) {
                try {
                    sender.execute(sm);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
