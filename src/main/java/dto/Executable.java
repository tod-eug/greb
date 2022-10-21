package dto;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.ArrayList;
import java.util.List;

public class Executable {

    private List<SendMessage> sm;
    private List<EditMessageText> em;
    private List<DeleteMessage> dm;

    public Executable() {
        sm = new ArrayList<>();
        em = new ArrayList<>();
        dm = new ArrayList<>();
    }

    public List<SendMessage> getSendMessages() {
        return sm;
    }

    public List<EditMessageText> getEditMessages() {
        return em;
    }

    public List<DeleteMessage> getDeleteMessages() {
        return dm;
    }

    public void addSendMessage(SendMessage sendMessage) {
        sm.add(sendMessage);
    }

    public void addEditMessage(EditMessageText editMessageText) {
        em.add(editMessageText);
    }

    public void addDeleteMessage(DeleteMessage deleteMessage) {
        dm.add(deleteMessage);
    }
}
