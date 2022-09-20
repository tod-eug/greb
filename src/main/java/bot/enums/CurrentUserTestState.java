package bot.enums;

import dto.NormalTestQuestion;

import java.util.List;

public class CurrentUserTestState {
    private final String testCode;
    private final Long userId;
    private final List<NormalTestQuestion> test;
    private final String attemptCode;
    private int currentQuestion;

    public CurrentUserTestState(String testCode,
                                Long userId,
                                List<NormalTestQuestion> test,
                                String attemptCode,
                                int currentQuestion) {
        this.testCode = testCode;
        this.userId = userId;
        this.test = test;
        this.attemptCode = attemptCode;
        this.currentQuestion = currentQuestion;
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

    public List<NormalTestQuestion> getTest() {
        return test;
    }

    public String getAttemptCode() {
        return attemptCode;
    }

    public void setCurrentQuestion(int currentQuestion) {
        this.currentQuestion = currentQuestion;
    }
}
