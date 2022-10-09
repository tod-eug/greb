package dto;

import bot.enums.TestType;

import java.util.List;

public class Test {
    private final String code;
    private final String name;
    private final TestType type;
    private final String task;
    private final List<TestQuestion> testQuestion;

    public Test(String code,
                String name,
                TestType type,
                String task,
                List<TestQuestion> testQuestion) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.task = task;
        this.testQuestion = testQuestion;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public TestType getType() {
        return type;
    }

    public String getTask() {
        return task;
    }

    public List<TestQuestion> getTestQuestion() {
        return testQuestion;
    }
}
