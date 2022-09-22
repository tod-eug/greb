package bot;

import bot.command.StartCommand;
import bot.command.TestsCommand;
import bot.enums.Option;
import dto.CurrentUserTestState;
import bot.keyboards.OptionsKeyboard;
import dto.NormalTestQuestion;
import mapper.NormalTestQuestionMapper;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import sheets.SheetsUtil;
import util.PropertiesProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrammarBot extends TelegramLongPollingCommandBot {

    private static Map<Long, String> stateMap = new HashMap<>();
    private static Map<Long, CurrentUserTestState> testStateMap = new HashMap<>();

    public GrammarBot() {
        super();
        register(new StartCommand());
        register(new TestsCommand());
    }
    @Override
    public String getBotUsername() {
        return PropertiesProvider.configurationProperties.get("BotName");
    }

    @Override
    public String getBotToken() {
        return PropertiesProvider.configurationProperties.get("BotToken");
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            processCallbackQuery(update);
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            processEmptyMessage(update);
        }
    }

    @Override
    public void processInvalidCommandUpdate(Update update) {
        super.processInvalidCommandUpdate(update);
    }

    @Override
    public boolean filter(Message message) {
        return super.filter(message);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    private void processCallbackQuery(Update update) {
        String[] parsedCallbackForTests = update.getCallbackQuery().getData().split("-");
        String[] parsedCallbackForOptions = update.getCallbackQuery().getData().split(":");
        String testCode = parsedCallbackForTests[2];
        Long userId = update.getCallbackQuery().getFrom().getId();
        if (testStateMap.get(userId) == null && testCode != null) {
            SheetsUtil sheetsUtil = new SheetsUtil();
            List<NormalTestQuestion> test = sheetsUtil.getNormalTest(testCode);
            List<Integer> optionMessages = new ArrayList<>();
            testStateMap.put(userId, new CurrentUserTestState(testCode, userId, test, update.getCallbackQuery().getData(), 0, optionMessages));
            processAttempt(update);
        } else if (testStateMap.get(userId) != null && parsedCallbackForOptions[3] != null) {
            processAttempt(update);
        } else {
            sendMsg(update.getCallbackQuery().getMessage().getChatId(), Constants.USE_TESTS_COMMAND);
        }
    }

    private void processEmptyMessage(Update update) {
        sendMsg(update.getMessage().getChatId(), Constants.USE_TESTS_COMMAND);
    }

    private void processAttempt(Update update) {
        Long chatID = update.getCallbackQuery().getMessage().getChatId();
        CurrentUserTestState currentUserTestState = testStateMap.get(update.getCallbackQuery().getFrom().getId());
        List<NormalTestQuestion> test = currentUserTestState.getTest();
        //process answer
        String[] parsedCallbackForOptions = update.getCallbackQuery().getData().split(":");
        NormalTestQuestionMapper normalTestQuestionMapper = new NormalTestQuestionMapper();
        if (parsedCallbackForOptions.length > 2) {
            Option answer = normalTestQuestionMapper.mapOption(parsedCallbackForOptions[3]);
            sendAnswerCallbackQuery(update.getCallbackQuery().getId(), answer.equals(currentUserTestState.getTest().get(currentUserTestState.getCurrentQuestion() - 1)
                    .getAnswer()));
        }

        List<Integer> optionMessage = currentUserTestState.getOptionMessages();
        if (currentUserTestState.getCurrentQuestion() != 0) {
            optionMessage.add(update.getCallbackQuery().getMessage().getMessageId());
            currentUserTestState.setOptionMessages(optionMessage);
        }

        //send next question
        int currentQuestion = currentUserTestState.getCurrentQuestion();
        if (currentUserTestState.getCurrentQuestion() < test.size()) {
            SendMessage sm = new SendMessage();
            sm.setChatId(chatID);
            sm.setText(test.get(currentUserTestState.getCurrentQuestion()).getQuestion());
            sm.setReplyMarkup(OptionsKeyboard.getOptionKeyboard(currentUserTestState));
            send(sm);
        } else {
            testStateMap.remove(update.getCallbackQuery().getFrom().getId());
            SendMessage sm = new SendMessage();
            sm.setChatId(chatID);
            sm.setText("Completed!");
            send(sm);
            for (Integer i: optionMessage) {
                deleteMessage(update.getCallbackQuery().getMessage().getChatId(), i);
            }
        }
        currentUserTestState.setCurrentQuestion(++currentQuestion);
    }

    private void sendMsg(long chatId, String text) {
        SendMessage sm = new SendMessage();
        sm.setChatId(chatId);
        sm.setText(text);
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendAnswerCallbackQuery(String id, Boolean success) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(id);
        answerCallbackQuery.setShowAlert(false);
        if (success)
            answerCallbackQuery.setText("✅");
        else
            answerCallbackQuery.setText("❌");
        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void send(SendMessage sm) {
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(Long.toString(chatId));
        deleteMessage.setMessageId(messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
