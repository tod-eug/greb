package bot;

import bot.command.StartCommand;
import bot.command.TestsCommand;
import bot.enums.TestType;
import db.ResultsHelper;
import dto.CurrentUserTestState;
import bot.keyboards.OptionsKeyboard;
import dto.TestQuestion;
import dto.TestResult;
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
            //ToDo
            processNextQuestion(update, SysConstants.QUESTIONS_CALLBACK_TYPE);
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
        if (parsedCallbackForTests[SysConstants.NUMBER_OF_CALLBACK_TYPE_IN_CALLBACK].equals(SysConstants.TESTS_CALLBACK_TYPE))
            callbackType = SysConstants.TESTS_CALLBACK_TYPE;
        else if (parsedCallbackForQuestion[SysConstants.NUMBER_OF_CALLBACK_TYPE_IN_CALLBACK].equals(SysConstants.QUESTIONS_CALLBACK_TYPE))
            callbackType = SysConstants.QUESTIONS_CALLBACK_TYPE;

        //process logic
        if (testStateMap.get(userId) == null && callbackType.equals(SysConstants.TESTS_CALLBACK_TYPE)) {
            String testCode = parsedCallbackForTests[SysConstants.NUMBER_OF_TEST_TYPE_IN_CALLBACK];
            initiateNewTestAttempt(update, testCode, userId, callbackType);
        } else if (testStateMap.get(userId) != null && callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
            processNextQuestion(update, callbackType);
        } else {
            sendMsg(update.getCallbackQuery().getMessage().getChatId(), ReplyConstants.USE_TESTS_COMMAND);
        }
    }

    private void processNextQuestion(Update update, String callbackType) {
        CurrentUserTestState currentUserTestState = null;
        Long chatID = null;
        Long userId = null;
        Integer currentMessageId = null;
        String callbackQueryID = "";
        if (update.hasMessage()) {
            currentUserTestState = testStateMap.get(update.getMessage().getFrom().getId());
            chatID = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();
            currentMessageId = update.getMessage().getMessageId();
        }

        else if (update.hasCallbackQuery()) {
            currentUserTestState = testStateMap.get(update.getCallbackQuery().getFrom().getId());
            chatID = update.getCallbackQuery().getMessage().getChatId();
            userId = update.getCallbackQuery().getFrom().getId();
            currentMessageId = update.getCallbackQuery().getMessage().getMessageId();
            callbackQueryID = update.getCallbackQuery().getId();
        }

        TestType testType = currentUserTestState.getTest().get(0).getTestType();
        List<TestQuestion> test = currentUserTestState.getTest();

        ResultsHelper rh = new ResultsHelper();
        EvaluateAnswerHelper evaluateAnswerHelper = new EvaluateAnswerHelper();

        //process answer for previous question
        boolean isRight = false;
        if (callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
            if (testType == TestType.normal || testType == TestType.article)
                isRight = evaluateAnswerHelper.evaluateOptionAnswer(update, currentUserTestState);
            else if (testType == TestType.normalWriting || testType == TestType.articleWriting) {
                String userMessage = "";
                if (update.hasMessage()) {
                    userMessage = update.getMessage().getText().toLowerCase().strip();
                } else {
                    sendMsg(chatID, ReplyConstants.SEND_ANSWER_AS_MESSAGE);
                }
                isRight = evaluateAnswerHelper.evaluateWrittenAnswer(update, currentUserTestState, userMessage);
            }

            //delete previous question
            if (update.hasCallbackQuery())
                sendAnswerCallbackQuery(callbackQueryID, isRight);
            deleteMessage(chatID, currentMessageId);
            if (testType == TestType.normalWriting || testType == TestType.articleWriting) {
                List<Integer> messagesToDelete = currentUserTestState.getMessagesToDelete();
                for (Integer i : messagesToDelete) {
                    deleteMessage(chatID, i);
                }
                List<Integer> newMessagesToDelete = new ArrayList<>();
                currentUserTestState.setMessagesToDelete(newMessagesToDelete);
            }
//            rh.createResult(currentUserTestState, currentAnswer.toString(), isRight);
        }

        //send next question logic
        int currentQuestion = currentUserTestState.getCurrentQuestion();
        if (testType == TestType.article || testType == TestType.articleWriting) { //if article -> send article before questions
            if (currentQuestion == 0) {
                String text = currentUserTestState.getTest().get(0).getArticle();
                SendMessage sm = new SendMessage();
                sm.setChatId(chatID);
                sm.setText(text);
                send(sm);
            }
        }

        if (currentQuestion < test.size()) { //if its not last question send next one
            SendMessage sm = new SendMessage();
            sm.setChatId(chatID);
            sm.setText(test.get(currentUserTestState.getCurrentQuestion()).getQuestion());
            sm.setReplyMarkup(OptionsKeyboard.getOptionKeyboard(currentUserTestState));
            if (testType == TestType.normalWriting || testType == TestType.articleWriting) {
                //send message and store message id to delete it after answer
                int messageId = sendAndReturnMessageID(sm);
                List<Integer> messagesToDelete = currentUserTestState.getMessagesToDelete();
                messagesToDelete.add(messageId);
                currentUserTestState.setMessagesToDelete(messagesToDelete);
            } else
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
            editMessage(chatID, currentUserTestState.getTestsMessageId(), text);

            //reset user test state
            testStateMap.remove(userId);
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

    private int sendAndReturnMessageID(SendMessage sm) {
        int messageId = 0;
        try {
            messageId = execute(sm).getMessageId();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return messageId;
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
