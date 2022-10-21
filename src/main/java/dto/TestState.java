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
    private int currentQuestion;
    private int testsMessageId;
    private int questionMessageId;
    private int articleMessageID;
    //codes for current testing attempt. Needed to separate old messages callback from current ones
    private final String categoryChooseTimestamp;
    private String testChooseTimestamp;
    private String attemptCode;

    private List<TestResult> results;

    public TestState(Long userId, Map<String, List<Test>> categories) {
        category = "";
        testCode = "";
        categoryChooseTimestamp = Long.toString(System.currentTimeMillis() / 1000);
        testsMessageId = 0;
        questionMessageId = 0;
        this.userId = userId;
        this.categories = categories;
    }

    public void setTestInformation(String testCode,
                                   Test test,
                                   String attemptCode,
                                   int currentQuestion,
                                   Integer articleMessageID,
                                   List<TestResult> results) {
        this.testCode = testCode;
        this.test = test;
        this.attemptCode = attemptCode;
        this.currentQuestion = currentQuestion;
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
        if (test != null)
            return test.getTestQuestion();
        return null;
    }

    public void setCurrentQuestion(int currentQuestion) {
        this.currentQuestion = currentQuestion;
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

    public String getCategoryChooseTimestamp() {
        return categoryChooseTimestamp;
    }

    public String getTestChooseTimestamp() {
        return testChooseTimestamp;
    }

    public void setTestChooseTimestamp(String testChooseTimestamp) {
        this.testChooseTimestamp = testChooseTimestamp;
    }

    public String getAttemptCode() {
        return attemptCode;
    }

    public void setQuestionMessageId(int questionMessageId) {
        this.questionMessageId = questionMessageId;
    }

    public int getQuestionMessageId() {
        return questionMessageId;
    }
}
