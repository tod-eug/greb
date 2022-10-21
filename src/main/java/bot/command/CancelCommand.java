package bot.command;

import bot.GrammarBot;
import bot.enums.TestType;
import bot.helpers.CategoriesHelper;
import dto.Test;
import dto.TestState;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import sheets.SheetsUtil;

import java.util.List;
import java.util.Map;

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
        TestState ts = GrammarBot.stateMap.get(message.getFrom().getId());

        if (ts != null) {
            //delete previous tests message
            if (ts.getTestsMessageId() != 0) {
                DeleteMessage deleteTestMessage = new DeleteMessage();
                deleteTestMessage.setChatId(message.getChatId());
                deleteTestMessage.setMessageId(ts.getTestsMessageId());
                mp.deleteMsg(absSender, deleteTestMessage);
            }


            //delete previous question message
            if (ts.getQuestionMessageId() != 0) {
                DeleteMessage deleteQuestionMessage = new DeleteMessage();
                deleteQuestionMessage.setChatId(message.getChatId());
                deleteQuestionMessage.setMessageId(ts.getQuestionMessageId());
                mp.deleteMsg(absSender, deleteQuestionMessage);
            }

            //delete previous article message if applicable
            if (ts.getTest() != null) {
                TestType testType = ts.getTest().get(0).getTestType();
                if (testType == TestType.article || testType == TestType.articleWriting) {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(message.getChatId());
                    deleteMessage.setMessageId(ts.getArticleMessageID());
                    mp.deleteMsg(absSender, deleteMessage);
                }
            }
        }

        // initiate new test attempt
        Map<String, List<Test>> categories =  SheetsUtil.getTests();
        ts = new TestState(message.getFrom().getId(), categories);

        CategoriesHelper ch = new CategoriesHelper();
        ts.setTestsMessageId(mp.sendAndReturnMessageID(absSender, ch.getInitialTestingProcessMessage(message.getFrom().getId(), message.getChatId(), categories, ts.getCategoryChooseTimestamp())));
        GrammarBot.stateMap.put(message.getFrom().getId(), ts);
    }
}
