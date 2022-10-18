package dto;

import java.util.List;
import java.util.Map;

public class TestState {

    private final Long userId;
    private String category;
    private String testCode;
    private String testName;
    private final Map<String, List<Test>> categories;
    private Test test;
    private String attemptCode;
    private int currentQuestion;
    private int testsMessageId;
    private List<Integer> messagesToDelete;
    private int articleMessageID;

    private List<TestResult> results;

    public TestState(Long userId, Map<String, List<Test>> categories) {
        category = "";
        testName = "";
        this.userId = userId;
        this.categories = categories;
    }

    public void setTestInformation(String testCode,
                                   Test test,
                                   String attemptCode,
                                   int currentQuestion,
                                   List<Integer> messagesToDelete,
                                   Integer articleMessageID,
                                   List<TestResult> results) {
        this.testCode = testCode;
        this.test = test;
        this.attemptCode = attemptCode;
        this.currentQuestion = currentQuestion;
        this.messagesToDelete = messagesToDelete;
        this.articleMessageID = articleMessageID;
        this.results = results;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Map<String, List<Test>> getCategories() {
        return categories;
    }

    public String getTestCode() {
        return testCode;
    }

    public Long getUserId() {
        return userId;
    }

    public int getCurrentQuestion() {
        return currentQuestion;
    }

    public List<TestQuestion> getTest() {
        return test.getTestQuestion();
    }

    public String getAttemptCode() {
        return attemptCode;
    }

    public void setCurrentQuestion(int currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public List<Integer> getMessagesToDelete() {
        return messagesToDelete;
    }

    public void setMessagesToDelete(List<Integer> messagesToDelete) {
        this.messagesToDelete = messagesToDelete;
    }

    public int getTestsMessageId() {
        return testsMessageId;
    }

    public void setTestsMessageId(int testsMessageId) {
        this.testsMessageId = testsMessageId;
    }

    public int getArticleMessageID() {
        return articleMessageID;
    }

    public void setArticleMessageID(Integer articleMessageID) {
        this.articleMessageID = articleMessageID;
    }

    public List<TestResult> getResults() {
        return results;
    }

    public void setResults(List<TestResult> results) {
        this.results = results;
    }
}
