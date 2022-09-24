package bot;

import bot.command.StartCommand;
import bot.command.TestsCommand;
import bot.enums.Option;
import db.ResultsHelper;
import dto.CurrentUserTestState;
import bot.keyboards.OptionsKeyboard;
import dto.TestQuestion;
import dto.TestResult;
import mapper.TestQuestionMapper;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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

    private void processEmptyMessage(Update update) {
        sendMsg(update.getMessage().getChatId(), ReplyConstants.USE_TESTS_COMMAND);
    }

    private void processCallbackQuery(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();

        //callback type checking logic
        String[] parsedCallbackForTests = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_TESTS_CALLBACK);
        String[] parsedCallbackForQuestion = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK);
        String callbackType = "";
        if (parsedCallbackForTests[0].equals(SysConstants.TESTS_CALLBACK_TYPE))
            callbackType = SysConstants.TESTS_CALLBACK_TYPE;
        else if (parsedCallbackForQuestion[0].equals(SysConstants.QUESTIONS_CALLBACK_TYPE))
            callbackType = SysConstants.QUESTIONS_CALLBACK_TYPE;

        //process logic
        if (testStateMap.get(userId) == null && callbackType.equals(SysConstants.TESTS_CALLBACK_TYPE)) {
            String testCode = parsedCallbackForTests[2];
            initiateNewTestAttempt(update, testCode, userId, callbackType);
        } else if (testStateMap.get(userId) != null && callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
            processNextQuestion(update, callbackType);
        } else {
            sendMsg(update.getCallbackQuery().getMessage().getChatId(), ReplyConstants.USE_TESTS_COMMAND);
        }
    }

    private void processNextQuestion(Update update, String callbackType) {
        CurrentUserTestState currentUserTestState = testStateMap.get(update.getCallbackQuery().getFrom().getId());

        ResultsHelper rh = new ResultsHelper();
        TestQuestionMapper testQuestionMapper = new TestQuestionMapper();

        String[] parsedCallbackForOptions = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK);
        Long chatID = update.getCallbackQuery().getMessage().getChatId();
        Integer currentMessageId = update.getCallbackQuery().getMessage().getMessageId();
        List<TestQuestion> test = currentUserTestState.getTest();
        String callbackQueryID = update.getCallbackQuery().getId();

        //process answer for previous question and delete previous question
        if (callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
            Option currentAnswer = testQuestionMapper.mapOption(parsedCallbackForOptions[3]);
            Option expectedAnswer = currentUserTestState.getTest().get(currentUserTestState.getCurrentQuestion() - 1).getAnswer();
            boolean isRight = currentAnswer.equals(expectedAnswer);

            sendAnswerCallbackQuery(callbackQueryID, isRight);
            deleteMessage(chatID, currentMessageId);
            rh.createResult(currentUserTestState, currentAnswer.toString(), isRight);
        }

        //add message ID of question to List to do something with it later. Since now bot deleting the question its useless
        List<Integer> optionMessage = currentUserTestState.getOptionMessages();
        if (currentUserTestState.getCurrentQuestion() != 0) { //skip because if its first question then previous question was message with list of Tests
            optionMessage.add(currentMessageId);
            currentUserTestState.setOptionMessages(optionMessage);
        }

        //send next question logic
        int currentQuestion = currentUserTestState.getCurrentQuestion();
        if (currentQuestion < test.size()) { //if its not last question send next one
            SendMessage sm = new SendMessage();
            sm.setChatId(chatID);
            sm.setText(test.get(currentUserTestState.getCurrentQuestion()).getQuestion());
            sm.setReplyMarkup(OptionsKeyboard.getOptionKeyboard(currentUserTestState));
            send(sm);
        } else { //processing test result
            List<TestResult> results = rh.getResultsByAttemptCode(currentUserTestState);
            int allQuestionsAmount = results.size();
            int rightAnswers = 0;
            for (TestResult nr : results) {
                if (nr.isRight())
                    rightAnswers++;
            }
            String text = "Completed! Test code: "+ currentUserTestState.getTestCode() +"\nAll questions: " + allQuestionsAmount + ". Right answers: " + rightAnswers + ".";
            editMessage(update.getCallbackQuery().getMessage().getChatId(), currentUserTestState.getTestsMessageId(), text);

            //reset user test state
            testStateMap.remove(update.getCallbackQuery().getFrom().getId());
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

    private void initiateNewTestAttempt(Update update, String testCode, Long userId, String callbackType) {
        String attemptCode = update.getCallbackQuery().getData();
        int testsMessageId = update.getCallbackQuery().getMessage().getMessageId();

        //get Test by testCode
        SheetsUtil sheetsUtil = new SheetsUtil();
        List<TestQuestion> test = sheetsUtil.getTest(testCode);
        //put user into current attempt of chosen test
        List<Integer> optionMessages = new ArrayList<>();
        CurrentUserTestState currentUserTestState = new CurrentUserTestState(testCode, userId, test, attemptCode, 0, testsMessageId, optionMessages);
        testStateMap.put(userId, currentUserTestState);
        //save attempt into db
        ResultsHelper rh = new ResultsHelper();
        rh.createAttempt(currentUserTestState, update.getCallbackQuery().getFrom());

        processNextQuestion(update, callbackType);
    }

    private void sendAnswerCallbackQuery(String callbackQueryId, Boolean success) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        answerCallbackQuery.setShowAlert(false);
        if (success)
            answerCallbackQuery.setText(SysConstants.SUCCESS_EMOJI);
        else
            answerCallbackQuery.setText(SysConstants.WRONG_EMOJI);
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

    private void editMessage(long chatId, int messageId, String text) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
