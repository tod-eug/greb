package bot;

public interface SysConstants {
    String CATEGORIES_CALLBACK_TYPE = "categories";
    String TESTS_CALLBACK_TYPE = "test";
    String QUESTIONS_CALLBACK_TYPE = "question";
    String DELIMITER_FOR_CATEGORIES_CALLBACK = ";";
    String DELIMITER_FOR_TESTS_CALLBACK = "-";
    String DELIMITER_FOR_QUESTIONS_CALLBACK = ":";
    Integer NUMBER_OF_CALLBACK_TYPE_IN_CALLBACK = 0;
    Integer NUMBER_OF_TEST_TYPE_IN_CALLBACK = 2;
    Integer NUMBER_OF_RESULTS_IN_CALLBACK = 3;

    String DELIMITER_FOR_WRITTEN_ANSWERS = "#";
    String DELIMITER_FOR_ALTERNATIVE_OPTIONS = "-";

    String SUCCESS_EMOJI = "✅";
    String WRONG_EMOJI = "❌";

}
