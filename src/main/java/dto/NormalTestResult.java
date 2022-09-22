package dto;

import bot.enums.Option;

import java.util.Map;

public class NormalTestResult {
    private final String question;
    private final Map<Option, String> options;
    private final Option answer;
    private final boolean isRight;

    public NormalTestResult(String question,
                            Map<Option, String> options,
                            Option answer,
                            boolean isRight) {
        this.question = question;
        this.options = options;
        this.answer = answer;
        this.isRight = isRight;
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
}
