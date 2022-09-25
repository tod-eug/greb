package dto;

import java.util.List;

public class CurrentUserTestState {
    private final String testCode;
    private final Long userId;
    private final List<TestQuestion> test;
    private final String attemptCode;
    private int currentQuestion;
    private final int testsMessageId;
    private List<Integer> messagesToDelete;

    public CurrentUserTestState(String testCode,
                                Long userId,
                                List<TestQuestion> test,
                                String attemptCode,
                                int currentQuestion,
                                int testsMessageId,
                                List<Integer> messagesToDelete) {
        this.testCode = testCode;
        this.userId = userId;
        this.test = test;
        this.attemptCode = attemptCode;
        this.currentQuestion = currentQuestion;
        this.testsMessageId = testsMessageId;
        this.messagesToDelete = messagesToDelete;
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
}
