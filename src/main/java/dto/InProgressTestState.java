package dto;

import bot.enums.TestType;

import java.util.List;

public class InProgressTestState {
    private final String testCode;
    private final TestType testType;
    private final Long userId;
    private final List<TestQuestion> test;
    private final String attemptCode;
    private int currentQuestion;
    private final int testsMessageId;
    private List<Integer> messagesToDelete;
    private int articleMessageID;

    public InProgressTestState(String testCode,
                               TestType testType,
                               Long userId,
                               List<TestQuestion> test,
                               String attemptCode,
                               int currentQuestion,
                               int testsMessageId,
                               List<Integer> messagesToDelete,
                               Integer articleMessageID) {
        this.testCode = testCode;
        this.testType = testType;
        this.userId = userId;
        this.test = test;
        this.attemptCode = attemptCode;
        this.currentQuestion = currentQuestion;
        this.testsMessageId = testsMessageId;
        this.messagesToDelete = messagesToDelete;
        this.articleMessageID = articleMessageID;
    }

    public String getTestCode() {
        return testCode;
    }

    public TestType getTestType() {
        return testType;
    }

    public Long getUserId() {
        return userId;
    }

    public int getCurrentQuestion() {
        return currentQuestion;
    }

    public List<TestQuestion> getTest() {
        return test;
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

    public int getArticleMessageID() {
        return articleMessageID;
    }

    public void setArticleMessageID(Integer articleMessageID) {
        this.articleMessageID = articleMessageID;
    }
}
