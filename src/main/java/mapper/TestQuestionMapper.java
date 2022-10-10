package mapper;

import bot.enums.Option;
import bot.enums.TestType;
import dto.TestQuestion;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestQuestionMapper {

    public TestQuestion mapQuestion(String testType, String name, String task, String article, String question, String answer, String answerWriting, Map<String, String> options) {
        Option answerOption = mapOption(answer);
        Map<Option, String> optionsOption = new HashMap<>();
        TestType type = mapTestType(testType);

        Set<String> optionKeys = options.keySet();
        for (String key : optionKeys) {
            Option keyOption = mapOption(key);
            optionsOption.put(keyOption, options.get(key));
        }
        return new TestQuestion(type, name, task, article, question, optionsOption, answerOption, answerWriting);
    }

    public TestType mapTestType(String text) {
        switch (text.toLowerCase()) {
            case "normalwriting":
                return TestType.normalWriting;
            case "article":
                return TestType.article;
            case "articlewriting":
                return TestType.articleWriting;
            case "match":
                return TestType.match;
            default:
                return TestType.normal;
        }
    }

    public Option mapOption(String text) {
        switch (text.toLowerCase()) {
            case "a":
                return Option.A;
            case "b":
                return Option.B;
            case "c":
                return Option.C;
            case "d":
                return Option.D;
            case "e":
                return Option.E;
            case "f":
                return Option.F;
            case "g":
                return Option.G;
            case "h":
                return Option.H;
            case "i":
                return Option.I;
            case "j":
                return Option.J;
            case "k":
                return Option.K;
            case "l":
                return Option.L;
            case "m":
                return Option.M;
            case "n":
                return Option.N;
            case "o":
                return Option.O;
            case "p":
                return Option.P;
            case "q":
                return Option.Q;
            case "r":
                return Option.R;
            case "s":
                return Option.S;
            case "t":
                return Option.T;
            case "u":
                return Option.U;
            case "v":
                return Option.V;
            case "w":
                return Option.W;
            case "x":
                return Option.X;
            case "y":
                return Option.Y;
            default:
                return Option.Z;
        }
    }
}
