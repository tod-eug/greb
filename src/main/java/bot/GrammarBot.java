package bot;

import bot.command.CancelCommand;
import bot.command.StartCommand;
import bot.enums.TestType;
import bot.helpers.CategoriesHelper;
import bot.helpers.EvaluateAnswerHelper;
import bot.helpers.InfoMessageHelper;
import db.ResultsHelper;
import dto.*;
import bot.keyboards.OptionsKeyboard;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import sheets.SheetsUtil;
import util.PropertiesProvider;

import java.util.*;

public class GrammarBot extends TelegramLongPollingCommandBot {

    public static Map<Long, TestState> stateMap = new HashMap<>();

    public GrammarBot() {
        super();
        register(new StartCommand());
        register(new CancelCommand());
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
            if (stateMap.get(update.getMessage().getFrom().getId()) == null) {
                initiateTestingProcess(update.getMessage().getFrom().getId(), update.getMessage().getChatId());
                deleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());
            }
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
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        //callback type checking logic
        String[] parsedCallbackForCategories = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_CATEGORIES_CALLBACK);
        String[] parsedCallbackForTests = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_TESTS_CALLBACK);
        String[] parsedCallbackForQuestion = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK);
        String callbackType = "";
        if (parsedCallbackForCategories[SysConstants.NUMBER_OF_CALLBACK_TYPE_IN_CALLBACK].equals(SysConstants.CATEGORIES_CALLBACK_TYPE))
            callbackType = SysConstants.CATEGORIES_CALLBACK_TYPE;
        else if (parsedCallbackForTests[SysConstants.NUMBER_OF_CALLBACK_TYPE_IN_CALLBACK].equals(SysConstants.TESTS_CALLBACK_TYPE))
            callbackType = SysConstants.TESTS_CALLBACK_TYPE;
        else if (parsedCallbackForQuestion[SysConstants.NUMBER_OF_CALLBACK_TYPE_IN_CALLBACK].equals(SysConstants.QUESTIONS_CALLBACK_TYPE))
            callbackType = SysConstants.QUESTIONS_CALLBACK_TYPE;
        TestState ts = stateMap.get(userId);
        if (ts == null) {
            //message is outdated
            deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
            initiateTestingProcess(userId, chatId);
        } else {
            if (callbackType.equals(SysConstants.CATEGORIES_CALLBACK_TYPE)) {
                if (parsedCallbackForCategories[3].equals(ts.getCategoryChooseTimestamp())) {
                    if (ts.getCategory().equals("")) {
                        //save category
                        CategoriesHelper categoriesHelper = new CategoriesHelper();
                        String category = parsedCallbackForCategories[SysConstants.NUMBER_OF_CATEGORY_TYPE_IN_CALLBACK];
                        int testMessageId = ts.getTestsMessageId();
                        String testChooseTimestamp = parsedCallbackForCategories[3];
                        ts.setCategory(category);
                        ts.setTestsMessageId(testMessageId);
                        ts.setTestChooseTimestamp(testChooseTimestamp);
                        editMessage(categoriesHelper.getSendTestsListMessage(chatId, userId, testMessageId, category, ts.getCategories().get(category), testChooseTimestamp));
                        sendAnswerCallbackQuery(update.getCallbackQuery().getId());
                    }
                } else
                    //if callback from other category choose -> delete the message
                    deleteMessage(chatId, messageId);
            } else if (callbackType.equals(SysConstants.TESTS_CALLBACK_TYPE)) {
                if (parsedCallbackForTests[3].equals(ts.getTestChooseTimestamp())) {
                    if (parsedCallbackForTests[4].equals(SysConstants.GO_BACK)) {
                        Map<String, List<Test>> categories =  SheetsUtil.getTests();
                        TestState newTs = new TestState(userId, categories);
                        newTs.setTestsMessageId(ts.getTestsMessageId());

                        CategoriesHelper ch = new CategoriesHelper();
                        editMessage(ch.getInitialTestingProcessMessageEdit(userId, chatId, categories, newTs.getCategoryChooseTimestamp(), newTs.getTestsMessageId()));
                        stateMap.put(userId, newTs);
                    }
                    if (ts.getTestCode().equals("")) {
                        //save test and initiate new attempt
                        String testCode = parsedCallbackForTests[SysConstants.NUMBER_OF_TEST_TYPE_IN_CALLBACK];
                        initiateNewTestAttempt(update, ts.getCategory(), testCode, userId, callbackType);
                    }
                } else
                    //if callback from other test choose -> delete the message
                    deleteMessage(chatId, messageId);
            } else if (callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
                String attemptCode = parsedCallbackForQuestion[1];
                if (stateMap.containsKey(userId)) {
                    if (ts.getAttemptCode().equals(attemptCode))
                        processNextQuestion(update, callbackType);
                    else
                        //if callback from other attempt -> delete the message
                        deleteMessage(chatId, messageId);
                } else {
                    String testCode = parsedCallbackForTests[SysConstants.NUMBER_OF_TEST_TYPE_IN_CALLBACK];
                    initiateNewTestAttempt(update, stateMap.get(userId).getCategory(), testCode, userId, callbackType);
                }
            } else
                //unknown callback, lets delete the message just to be safe
                deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
        }
    }

    private void processNextQuestion(Update update, String callbackType) {
        TestState ts = null;
        Long chatID = null;
        Long userId = null;
        Integer currentMessageId = null;
        String callbackQueryID = "";
        String currentAnswer = "";
        if (update.hasMessage()) {

            ts = stateMap.get(update.getMessage().getFrom().getId());
            chatID = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();
            currentMessageId = update.getMessage().getMessageId();
            currentAnswer = update.getMessage().getText();
        }

        else if (update.hasCallbackQuery()) {
            ts = stateMap.get(update.getCallbackQuery().getFrom().getId());
            chatID = update.getCallbackQuery().getMessage().getChatId();
            userId = update.getCallbackQuery().getFrom().getId();
            currentMessageId = update.getCallbackQuery().getMessage().getMessageId();
            callbackQueryID = update.getCallbackQuery().getId();
            if (callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
                String[] parsedCallbackForOptions = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK);
                currentAnswer = parsedCallbackForOptions[SysConstants.NUMBER_OF_RESULTS_IN_CALLBACK];
            }
        }

        TestType testType = ts.getTest().get(0).getTestType();
        List<TestQuestion> test = ts.getTest();

        ResultsHelper rh = new ResultsHelper();
        EvaluateAnswerHelper evaluateAnswerHelper = new EvaluateAnswerHelper();

        //process answer for previous question
        boolean isRight = false;
        if (callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
            isRight = evaluateAnswerHelper.evaluateAnswer(testType, update, ts);
            ts = stateMap.get(userId);

            //send answer to callbackQuery
            if (update.hasCallbackQuery())
                sendAnswerCallbackQuery(callbackQueryID);

            //save current result into db
            rh.createResult(ts, currentAnswer, isRight);
        }

        //send next question logic
        int currentQuestion = ts.getCurrentQuestion();
        if (testType == TestType.article || testType == TestType.articleWriting) { //if article -> send article before questions
            if (currentQuestion == 0) {
                String text = ts.getTest().get(0).getArticle();
                SendMessage sm = new SendMessage();
                sm.setChatId(chatID);
                sm.setText(text);
                sm.setParseMode(ParseMode.HTML);
                int articleMessageID = sendAndReturnMessageID(sm);
                ts.setArticleMessageID(articleMessageID);
            }
        }

        if (currentQuestion < test.size()) { //if its not last question send next one
            if (ts.getQuestionMessageId() == 0) { //if its first message then send it and store message id
                SendMessage sm = new SendMessage();
                sm.setChatId(chatID);
                sm.setText(test.get(ts.getCurrentQuestion()).getQuestion());
                sm.setParseMode(ParseMode.HTML);
                sm.setReplyMarkup(OptionsKeyboard.getOptionKeyboard(ts.getTest().get(ts.getCurrentQuestion()).getOptions(), ts.getAttemptCode(), ts.getCurrentQuestion()));

                int messageId = sendAndReturnMessageID(sm);
                ts.setQuestionMessageId(messageId);
            } else { //otherwise edit existing one
                EditMessageText em = new EditMessageText();
                em.setChatId(chatID);
                em.setMessageId(ts.getQuestionMessageId());
                em.setText(test.get(ts.getCurrentQuestion()).getQuestion());
                em.setParseMode(ParseMode.HTML);
                em.setReplyMarkup(OptionsKeyboard.getOptionKeyboard(ts.getTest().get(ts.getCurrentQuestion()).getOptions(), ts.getAttemptCode(), ts.getCurrentQuestion()));
                editMessage(em);
            }
            InfoMessageHelper imh = new InfoMessageHelper();
            editMessage(chatID, ts.getTestsMessageId(), imh.getMessage(ts), true);
        } else { //processing test result
            if (testType == TestType.article || testType == TestType.articleWriting) { //if article -> delete message with article
                deleteMessage(chatID, ts.getArticleMessageID());
            }
            //delete question message
            deleteMessage(chatID, ts.getQuestionMessageId());
            InfoMessageHelper imh = new InfoMessageHelper();
            editMessage(chatID, ts.getTestsMessageId(), imh.getMessage(ts), true);

            //reset processing state
            stateMap.remove(userId);
            initiateTestingProcess(userId, chatID);
        }
        if (testType == TestType.normalWriting || testType == TestType.articleWriting) {
            if (update.hasMessage())
                deleteMessage(chatID, update.getMessage().getMessageId());
        }
        ts.setCurrentQuestion(++currentQuestion);
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

    private void initiateNewTestAttempt(Update update, String category, String testCode, Long userId, String callbackType) {
        String attemptCode = update.getCallbackQuery().getData();
        TestState ts = stateMap.get(userId);

        //get Test by testCode
        Test test = null;
        for (Test t: ts.getCategories().get(ts.getCategory())) {
            if (t.getCode().equals(testCode))
                test = t;
        }
        //put user into current attempt of chosen test
        List<TestResult> testResults = new ArrayList<>();
        ts.setTestInformation(testCode, test, attemptCode, 0, 0, testResults);
        stateMap.put(userId, ts);

        //save attempt into db
        ResultsHelper rh = new ResultsHelper();
        if (update.hasCallbackQuery())
            rh.createAttempt(ts, update.getCallbackQuery().getFrom(), update.getCallbackQuery().getMessage().getChatId().toString());
        else if (update.hasMessage())
            rh.createAttempt(ts, update.getCallbackQuery().getFrom(), update.getMessage().getChatId().toString());

        processNextQuestion(update, callbackType);
    }

    public void initiateTestingProcess(Long userId, Long chatId) {
        Map<String, List<Test>> categories =  SheetsUtil.getTests();
        TestState ts = new TestState(userId, categories);

        CategoriesHelper ch = new CategoriesHelper();
        ts.setTestsMessageId(sendAndReturnMessageID(ch.getInitialTestingProcessMessage(userId, chatId, categories, ts.getCategoryChooseTimestamp())));
        stateMap.put(userId, ts);
    }

    private void sendAnswerCallbackQuery(String callbackQueryId) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        answerCallbackQuery.setShowAlert(false);
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

    private void editMessage(long chatId, int messageId, String text, boolean htmlParseMode) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        if (htmlParseMode)
            editMessageText.setParseMode(ParseMode.HTML);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void editMessage(EditMessageText em) {
        try {
            execute(em);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void executeMessages(Executable executable) {
        if (executable.getDeleteMessages().size() > 0) {
            for (DeleteMessage dm : executable.getDeleteMessages()) {
                try {
                    execute(dm);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        if (executable.getEditMessages().size() > 0) {
            for (EditMessageText em : executable.getEditMessages()) {
                try {
                    execute(em);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        if (executable.getSendMessages().size() > 0) {
            for (SendMessage sm : executable.getSendMessages()) {
                try {
                    execute(sm);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
