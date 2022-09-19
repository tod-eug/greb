package mapper;

import bot.enums.TestType;
import dto.Test;

public class TestMapper {

    public Test mapTest(String code, String name, String type) {
        switch (type.toLowerCase()) {
            case "normal":
                return new Test(code, name, TestType.normal);
            case "normalwriting":
                return new Test(code, name, TestType.normalWriting);
            case "article":
                return new Test(code, name, TestType.article);
            case "articlewriting":
                return new Test(code, name, TestType.articleWriting);
            case "match":
                return new Test(code, name, TestType.match);
            default:
                return new Test("", "", TestType.normal);
        }
    }
}
