package bot;

import bot.command.StartCommand;
import bot.command.TestsCommand;
import bot.enums.State;
import bot.enums.TestType;
import db.ResultsHelper;
import dto.ChoosingTestState;
import dto.InProgressTestState;
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

    public static Map<Long, State> stateMap = new HashMap<>();
    public static Map<Long, ChoosingTestState> choosingTestStateMap = new HashMap<>();
    public static Map<Long, InProgressTestState> inProgressStateMap = new HashMap<>();

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
            Long userId = update.getMessage().getFrom().getId();
            if (inProgressStateMap.get(userId) != null)
                if (inProgressStateMap.get(userId).getTestType() == TestType.normalWriting || inProgressStateMap.get(userId).getTestType() == TestType.articleWriting)
                //add check for Test type
                processNextQuestion(update, SysConstants.QUESTIONS_CALLBACK_TYPE);
                else
                    deleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());
            else
                sendMsg(update.getMessage().getChatId(), ReplyConstants.USE_TESTS_COMMAND);
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
        State state = null;
        if (stateMap.get(userId) != null)
            state = stateMap.get(userId);
        else
            state = State.FREE;

        //callback type checking logic
        String[] parsedCallbackForCategories = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_CATEGORIES_CALLBACK);
        String[] parsedCallbackForTests = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_TESTS_CALLBACK);
        String[] parsedCallbackForQuestion = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK);
        String callbackType = "";
        if (parsedCallbackForTests[SysConstants.NUMBER_OF_CALLBACK_TYPE_IN_CALLBACK].equals(SysConstants.TESTS_CALLBACK_TYPE))
            callbackType = SysConstants.TESTS_CALLBACK_TYPE;
        else if (parsedCallbackForQuestion[SysConstants.NUMBER_OF_CALLBACK_TYPE_IN_CALLBACK].equals(SysConstants.QUESTIONS_CALLBACK_TYPE))
            callbackType = SysConstants.QUESTIONS_CALLBACK_TYPE;
        else if (parsedCallbackForCategories[SysConstants.NUMBER_OF_CALLBACK_TYPE_IN_CALLBACK].equals(SysConstants.CATEGORIES_CALLBACK_TYPE))
            callbackType = SysConstants.CATEGORIES_CALLBACK_TYPE;

        //process logic
        if (state == State.TEST_CHOOSING && callbackType == SysConstants.CATEGORIES_CALLBACK_TYPE && choosingTestStateMap.get(userId) != null) {
            CategoriesTestsLogicHelper categoriesTestsLogicHelper = new CategoriesTestsLogicHelper();
            categoriesTestsLogicHelper.processTestChoosing(choosingTestStateMap.get(userId));
        }

        if (inProgressStateMap.get(userId) == null && callbackType.equals(SysConstants.TESTS_CALLBACK_TYPE)) {
            String testCode = parsedCallbackForTests[SysConstants.NUMBER_OF_TEST_TYPE_IN_CALLBACK];
            initiateNewTestAttempt(update, testCode, userId, callbackType);
        } else if (inProgressStateMap.get(userId) != null && callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
            processNextQuestion(update, callbackType);
        } else {
            sendMsg(update.getCallbackQuery().getMessage().getChatId(), ReplyConstants.USE_TESTS_COMMAND);
        }
    }

    private void processNextQuestion(Update update, String callbackType) {
        InProgressTestState inProgressTestState = null;
        Long chatID = null;
        Long userId = null;
        Integer currentMessageId = null;
        String callbackQueryID = "";
        String currentAnswer = "";
        if (update.hasMessage()) {
            inProgressTestState = inProgressStateMap.get(update.getMessage().getFrom().getId());
            chatID = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();
            currentMessageId = update.getMessage().getMessageId();
            currentAnswer = update.getMessage().getText();
        }

        else if (update.hasCallbackQuery()) {
            inProgressTestState = inProgressStateMap.get(update.getCallbackQuery().getFrom().getId());
            chatID = update.getCallbackQuery().getMessage().getChatId();
            userId = update.getCallbackQuery().getFrom().getId();
            currentMessageId = update.getCallbackQuery().getMessage().getMessageId();
            callbackQueryID = update.getCallbackQuery().getId();
            if (callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
                String[] parsedCallbackForOptions = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK);
                currentAnswer = parsedCallbackForOptions[SysConstants.NUMBER_OF_RESULTS_IN_CALLBACK];
            }
        }

        TestType testType = inProgressTestState.getTest().get(0).getTestType();
        List<TestQuestion> test = inProgressTestState.getTest();

        ResultsHelper rh = new ResultsHelper();
        EvaluateAnswerHelper evaluateAnswerHelper = new EvaluateAnswerHelper();

        //process answer for previous question
        boolean isRight = false;
        if (callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
            if (testType == TestType.normal || testType == TestType.article)
                isRight = evaluateAnswerHelper.evaluateOptionAnswer(update, inProgressTestState);
            else if (testType == TestType.normalWriting || testType == TestType.articleWriting) {
                String userMessage = "";
                if (update.hasMessage()) {
                    userMessage = update.getMessage().getText().toLowerCase().strip();
                } else {
                    sendMsg(chatID, ReplyConstants.SEND_ANSWER_AS_MESSAGE);
                }
                isRight = evaluateAnswerHelper.evaluateWrittenAnswer(update, inProgressTestState, userMessage);
            }

            //delete previous question
            if (update.hasCallbackQuery())
                sendAnswerCallbackQuery(callbackQueryID, isRight);
            deleteMessage(chatID, currentMessageId);
            if (testType == TestType.normalWriting || testType == TestType.articleWriting) {
                List<Integer> messagesToDelete = inProgressTestState.getMessagesToDelete();
                for (Integer i : messagesToDelete) {
                    deleteMessage(chatID, i);
                }
                List<Integer> newMessagesToDelete = new ArrayList<>();
                inProgressTestState.setMessagesToDelete(newMessagesToDelete);
            }
            rh.createResult(inProgressTestState, currentAnswer, isRight);
        }

        //send next question logic
        int currentQuestion = inProgressTestState.getCurrentQuestion();
        if (testType == TestType.article || testType == TestType.articleWriting) { //if article -> send article before questions
            if (currentQuestion == 0) {
                String text = inProgressTestState.getTest().get(0).getArticle();
                SendMessage sm = new SendMessage();
                sm.setChatId(chatID);
                sm.setText(text);
                int articleMessageID = sendAndReturnMessageID(sm);
                inProgressTestState.setArticleMessageID(articleMessageID);
            }
        }

        if (currentQuestion < test.size()) { //if its not last question send next one
            SendMessage sm = new SendMessage();
            sm.setChatId(chatID);
            sm.setText(test.get(inProgressTestState.getCurrentQuestion()).getQuestion());
            sm.setReplyMarkup(OptionsKeyboard.getOptionKeyboard(inProgressTestState));
            if (testType == TestType.normalWriting || testType == TestType.articleWriting) {
                //send message and store message id to delete it after answer
                int messageId = sendAndReturnMessageID(sm);
                List<Integer> messagesToDelete = inProgressTestState.getMessagesToDelete();
                messagesToDelete.add(messageId);
                inProgressTestState.setMessagesToDelete(messagesToDelete);
            } else
                send(sm);
        } else { //processing test result
            if (testType == TestType.article || testType == TestType.articleWriting) { //if article -> delete message with article
                deleteMessage(chatID, inProgressTestState.getArticleMessageID());
            }
            List<TestResult> results = rh.getResultsByAttemptCode(inProgressTestState);
            int allQuestionsAmount = results.size();
            int rightAnswers = 0;
            for (TestResult nr : results) {
                if (nr.isRight())
                    rightAnswers++;
            }
            String text = "Completed! Test code: "+ inProgressTestState.getTestCode() +"\nAll questions: " + allQuestionsAmount + ". Right answers: " + rightAnswers + ".";
            editMessage(chatID, inProgressTestState.getTestsMessageId(), text);

            //reset user test state
            inProgressStateMap.remove(userId);
        }
        inProgressTestState.setCurrentQuestion(++currentQuestion);
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
        TestType testType = test.get(0).getTestType();
        //put user into current attempt of chosen test
        List<Integer> optionMessages = new ArrayList<>();
        InProgressTestState inProgressTestState = new InProgressTestState(testCode, testType, userId, test, attemptCode, 0, testsMessageId, optionMessages, 0);
        inProgressStateMap.put(userId, inProgressTestState);
        //save attempt into db
        ResultsHelper rh = new ResultsHelper();
        rh.createAttempt(inProgressTestState, update.getCallbackQuery().getFrom());

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
