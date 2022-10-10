package mapper;

import bot.enums.TestType;
import dto.Test;
import dto.TestQuestion;

import java.util.List;

public class TestMapper {

    public Test mapTest(String code, String name, String type, String task, List<TestQuestion> testQuestion) {
        switch (type.toLowerCase()) {
            case "normal":
                return new Test(code, name, TestType.normal, task, testQuestion);
            case "normalwriting":
                return new Test(code, name, TestType.normalWriting, task, testQuestion);
            case "article":
                return new Test(code, name, TestType.article, task, testQuestion);
            case "articlewriting":
                return new Test(code, name, TestType.articleWriting, task, testQuestion);
            case "match":
                return new Test(code, name, TestType.match, task, testQuestion);
            default:
                return new Test("", "", TestType.normal, task, testQuestion);
        }
    }
}