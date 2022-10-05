package bot;

public interface ReplyConstants {
    /**
     * Strings for commands replies
     */
    String START_REPLY_WELCOME = "Добро пожаловать! ";
    String TESTS_COMMAND = "Список доступных тестов по грамматике: \n";

    /**
     * Other strings
     */
    String LIST_OF_CATEGORIES = "Categories available: \n";
    String USE_TESTS_COMMAND = "Используйте команду /tests для просмотра доступных тестов по грамматике английского языка";
    String MESSAGE_IS_OUTDATED = "Sorry, but this message is outdated. Please use the /tests command";
    String SEND_ANSWER_AS_MESSAGE = "Please send answer as a message";

    /**
     * Strings for callbacks
     */
    String ADD_NEW_EXERCISE = "#addNewTests";
}
