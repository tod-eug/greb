package dto;

import bot.enums.Option;
import bot.enums.TestType;

import java.util.Map;

public class TestResult {

    private final TestType testType;
    private final String article;
    private final String question;
    private final Map<Option, String> options;
    private final Option answer;
    private final String answerWriting;
    private final boolean isRight;

    public TestResult(TestType testType,
                      String article,
                      String question,
                      Map<Option, String> options,
                      Option answer,
                      String answerWriting,
                      boolean isRight) {
        this.testType = testType;
        this.article = article;
        this.question = question;
        this.options = options;
        this.answer = answer;
        this.answerWriting = answerWriting;
        this.isRight = isRight;
    }

    public TestType getTestType() {
        return testType;
    }

    public String getArticle() {
        return article;
    }

    public String getQuestion() {
        return question;
    }

    public Map<Option, String> getOptions() {
        return options;
    }

    public Option getAnswer() {
        return answer;
    }

    public boolean isRight() {
        return isRight;
    }

    public String getAnswerWriting() {
        return answerWriting;
    }
}
