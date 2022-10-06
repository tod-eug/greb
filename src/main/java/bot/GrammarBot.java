package bot;

import bot.command.CancelCommand;
import bot.command.StartCommand;
import bot.command.TestsCommand;
import bot.enums.State;
import bot.enums.TestType;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrammarBot extends TelegramLongPollingCommandBot {

    public static Map<Long, State> stateMap = new HashMap<>();
    public static Map<Long, ChoosingTestState> choosingStateMap = new HashMap<>();
    public static Map<Long, ProcessingTestState> processingStateMap = new HashMap<>();

    public GrammarBot() {
        super();
        register(new StartCommand());
        register(new TestsCommand());
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
        State state = stateMap.get(userId);

        //process logic
        if (state == State.CHOOSING_TEST) {
                if (choosingStateMap.containsKey(userId)) {
                    ChoosingTestState choosingTestState = choosingStateMap.get(userId);
                    CategoriesHelper categoriesHelper = new CategoriesHelper();
                    if (choosingTestState.getCategory().equals("")) {
                        //save category
                        String category = parsedCallbackForCategories[SysConstants.NUMBER_OF_CATEGORY_TYPE_IN_CALLBACK];
                        choosingTestState.setCategory(category);
                        send(categoriesHelper.getSendTestsListMessage(choosingTestState.getCategory(), update, choosingTestState));
                        sendAnswerCallbackQuery(update.getCallbackQuery().getId(), true);
                        deleteMessage(update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getMessage().getMessageId());
                    } else if (choosingTestState.getTestName().equals("")) {
                        //save test and initiate new attempt
                        String testCode = parsedCallbackForTests[SysConstants.NUMBER_OF_TEST_TYPE_IN_CALLBACK];
                        initiateNewTestAttempt(update, testCode, userId, callbackType);
                        stateMap.put(userId, State.PROCESSING_TEST);
                    } else {
                        processNextQuestion(update, callbackType);
                        //save test and go to processing test state
                    }
                } else {
                    //pressed button with tests while choosing test
                    sendMsg(update.getCallbackQuery().getMessage().getChatId(), ReplyConstants.MESSAGE_IS_OUTDATED);
                    //ToDo delete this message
                }
        } else if (state == State.PROCESSING_TEST) {
            if (callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
                if (processingStateMap.containsKey(userId)) {
                    processNextQuestion(update, callbackType);
                } else {
                    String testCode = parsedCallbackForTests[SysConstants.NUMBER_OF_TEST_TYPE_IN_CALLBACK];
                    initiateNewTestAttempt(update, testCode, userId, callbackType);
                }
            } else {
                //pressed button with category while doing the test
                sendMsg(update.getCallbackQuery().getMessage().getChatId(), ReplyConstants.MESSAGE_IS_OUTDATED);
                //ToDo delete this message
            }
        } else if (state == State.FREE)
            sendMsg(update.getCallbackQuery().getMessage().getChatId(), ReplyConstants.USE_TESTS_COMMAND);
    }

    private void processNextQuestion(Update update, String callbackType) {
        ProcessingTestState processingTestState = null;
        Long chatID = null;
        Long userId = null;
        Integer currentMessageId = null;
        String callbackQueryID = "";
        String currentAnswer = "";
        if (update.hasMessage()) {

            processingTestState = processingStateMap.get(update.getMessage().getFrom().getId());
            chatID = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();
            currentMessageId = update.getMessage().getMessageId();
            currentAnswer = update.getMessage().getText();
        }

        else if (update.hasCallbackQuery()) {
            processingTestState = processingStateMap.get(update.getCallbackQuery().getFrom().getId());
            chatID = update.getCallbackQuery().getMessage().getChatId();
            userId = update.getCallbackQuery().getFrom().getId();
            currentMessageId = update.getCallbackQuery().getMessage().getMessageId();
            callbackQueryID = update.getCallbackQuery().getId();
            if (callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
                String[] parsedCallbackForOptions = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK);
                currentAnswer = parsedCallbackForOptions[SysConstants.NUMBER_OF_RESULTS_IN_CALLBACK];
            }
        }

        TestType testType = processingTestState.getTest().get(0).getTestType();
        List<TestQuestion> test = processingTestState.getTest();

        ResultsHelper rh = new ResultsHelper();
        EvaluateAnswerHelper evaluateAnswerHelper = new EvaluateAnswerHelper();

        //process answer for previous question
        boolean isRight = false;
        if (callbackType.equals(SysConstants.QUESTIONS_CALLBACK_TYPE)) {
            if (testType == TestType.normal || testType == TestType.article)
                isRight = evaluateAnswerHelper.evaluateOptionAnswer(update, processingTestState);
            else if (testType == TestType.normalWriting || testType == TestType.articleWriting) {
                String userMessage = "";
                if (update.hasMessage()) {
                    userMessage = update.getMessage().getText().toLowerCase().strip();
                } else {
                    sendMsg(chatID, ReplyConstants.SEND_ANSWER_AS_MESSAGE);
                }
                isRight = evaluateAnswerHelper.evaluateWrittenAnswer(update, processingTestState, userMessage);
            }

            //send answer to callbackQuery
            if (update.hasCallbackQuery())
                sendAnswerCallbackQuery(callbackQueryID, isRight);

            //delete previous question
            deleteMessage(chatID, currentMessageId);
            if (testType == TestType.normalWriting || testType == TestType.articleWriting) {
                List<Integer> messagesToDelete = processingTestState.getMessagesToDelete();
                for (Integer i : messagesToDelete) {
                    deleteMessage(chatID, i);
                }
                List<Integer> newMessagesToDelete = new ArrayList<>();
                processingTestState.setMessagesToDelete(newMessagesToDelete);
            }
            rh.createResult(processingTestState, currentAnswer, isRight);
        }

        //send next question logic
        int currentQuestion = processingTestState.getCurrentQuestion();
        if (testType == TestType.article || testType == TestType.articleWriting) { //if article -> send article before questions
            if (currentQuestion == 0) {
                String text = processingTestState.getTest().get(0).getArticle();
                SendMessage sm = new SendMessage();
                sm.setChatId(chatID);
                sm.setText(text);
                sm.setParseMode(ParseMode.HTML);
                int articleMessageID = sendAndReturnMessageID(sm);
                processingTestState.setArticleMessageID(articleMessageID);
            }
        }

        if (currentQuestion < test.size()) { //if its not last question send next one
            SendMessage sm = new SendMessage();
            sm.setChatId(chatID);
            sm.setText(test.get(processingTestState.getCurrentQuestion()).getQuestion());
            sm.setParseMode(ParseMode.HTML);
            sm.setReplyMarkup(OptionsKeyboard.getOptionKeyboard(processingTestState));
            if (testType == TestType.normalWriting || testType == TestType.articleWriting) {
                //send message and store message id to delete it after answer
                int messageId = sendAndReturnMessageID(sm);
                List<Integer> messagesToDelete = processingTestState.getMessagesToDelete();
                messagesToDelete.add(messageId);
                processingTestState.setMessagesToDelete(messagesToDelete);
            } else
                send(sm);
        } else { //processing test result
            if (testType == TestType.article || testType == TestType.articleWriting) { //if article -> delete message with article
                deleteMessage(chatID, processingTestState.getArticleMessageID());
            }
            List<TestResult> results = rh.getResultsByAttemptCode(processingTestState);
            int allQuestionsAmount = results.size();
            int rightAnswers = 0;
            for (TestResult nr : results) {
                if (nr.isRight())
                    rightAnswers++;
            }
            String text = "Completed! Test code: "+ processingTestState.getTestCode() +"\nAll questions: " + allQuestionsAmount + ". Right answers: " + rightAnswers + ".";
            editMessage(chatID, processingTestState.getTestsMessageId(), text);

            //reset processing state
            processingStateMap.remove(userId);
        }
        processingTestState.setCurrentQuestion(++currentQuestion);
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
        choosingStateMap.remove(userId);

        String attemptCode = update.getCallbackQuery().getData();
        int testsMessageId = update.getCallbackQuery().getMessage().getMessageId();

        //get Test by testCode
        SheetsUtil sheetsUtil = new SheetsUtil();
        List<TestQuestion> test = sheetsUtil.getTest(testCode);
        //put user into current attempt of chosen test
        List<Integer> optionMessages = new ArrayList<>();
        ProcessingTestState processingTestState = new ProcessingTestState(testCode, userId, test, attemptCode, 0, testsMessageId, optionMessages, 0);
        processingStateMap.put(userId, processingTestState);
        //delete keyboard from the previous message
        editMessage(update.getCallbackQuery().getMessage().getChatId(), testsMessageId, update.getCallbackQuery().getMessage().getText());
        //save attempt into db
        ResultsHelper rh = new ResultsHelper();
        if (update.hasCallbackQuery())
            rh.createAttempt(processingTestState, update.getCallbackQuery().getFrom(), update.getCallbackQuery().getMessage().getChatId().toString());
        else if (update.hasMessage())
            rh.createAttempt(processingTestState, update.getCallbackQuery().getFrom(), update.getMessage().getChatId().toString());

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
