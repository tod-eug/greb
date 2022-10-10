package dto;

import bot.enums.Option;
import bot.enums.TestType;

import java.util.Map;

public class TestQuestion {

    private final TestType testType;
    private final String name;
    private final String task;
    private final String article;
    private final String question;
    private final Map<Option, String> options;
    private final Option answer;
    private final String answerWriting;

    public TestQuestion(TestType testType,
                        String name,
                        String task,
                        String article,
                        String question,
                        Map<Option, String> options,
                        Option answer,
                        String answerWriting) {
        this.testType = testType;
        this.name = name;
        this.task = task;
        this.article = article;
        this.question = question;
        this.options = options;
        this.answer = answer;
        this.answerWriting = answerWriting;
    }

    public String getQuestion() {
        return question;
    }

    public String getName() { return name; }

    public String getTask() { return task; }

    public Map<Option, String> getOptions() {
        return options;
    }

    public Option getAnswer() {
        return answer;
    }

    public TestType getTestType() {
        return testType;
    }

    public String getArticle() {
        return article;
    }

    public String getAnswerWriting() {
        return answerWriting;
    }
}
