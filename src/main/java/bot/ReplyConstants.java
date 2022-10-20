package bot;

public interface ReplyConstants {
    /**
     * Strings for commands replies
     */
    String START_REPLY_WELCOME = "Welcome! I can help you with training your English grammar!\nAll tests are split by category. Please choose the category to start!";
    String TESTS_COMMAND = "All tests are split by category. List of available categories: \n";
    String CANCEL_REPLY_RESET = "Cancelling current test.";

    /**
     * Other strings
     */
    String LIST_OF_TESTS_IN_CATEGORY_1 = "Available Tests in category <b>";
    String LIST_OF_TESTS_IN_CATEGORY_2 = "</b>:\n";
    String USE_TESTS_COMMAND = "Please use /tests command to check available tests for English grammar";
    String MESSAGE_IS_OUTDATED = "Sorry, but this message is outdated. Please use the /tests command";
    String SEND_ANSWER_AS_MESSAGE = "Please send answer as a message";
}
