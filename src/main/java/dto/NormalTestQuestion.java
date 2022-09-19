package dto;

import bot.enums.Option;

import java.util.Map;

public class NormalTestQuestion {
    private final String question;
    private final Map<Option, String> options;
    private final Option answer;

    public NormalTestQuestion(String question,
                              Map<Option, String> options,
                              Option answer) {
        this.question = question;
        this.options = options;
        this.answer = answer;
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
}
