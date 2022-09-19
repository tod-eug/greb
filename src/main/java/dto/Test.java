package dto;

import bot.enums.TestType;

public class Test {
    private final String code;
    private final String name;
    private final TestType type;

    public Test(String code,
                String name,
                TestType type) {
        this.code = code;
        this.name = name;
        this.type = type;
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
}
