package bot.helpers;

import bot.GrammarBot;
import bot.ReplyConstants;
import bot.SysConstants;
import bot.enums.Option;
import bot.enums.TestType;
import dto.TestQuestion;
import dto.TestResult;
import dto.TestState;
import mapper.TestQuestionMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluateAnswerHelper {

    public boolean evaluateAnswer(TestType testType, Update update, TestState ts) {
        boolean isRight = false;
        if (testType == TestType.normal || testType == TestType.article)
            isRight = evaluateOptionAnswer(update, ts);
        else if (testType == TestType.normalWriting || testType == TestType.articleWriting) {
            String userMessage = "";
            if (update.hasMessage())
                userMessage = update.getMessage().getText().toLowerCase().strip();
            isRight = evaluateWrittenAnswer(update, ts, userMessage);
        }
        return isRight;
    }

    private boolean evaluateOptionAnswer(Update update, TestState ts) {
        TestQuestionMapper testQuestionMapper = new TestQuestionMapper();
        String[] parsedCallbackForOptions = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK);
        Option currentAnswer = testQuestionMapper.mapOption(parsedCallbackForOptions[SysConstants.NUMBER_OF_RESULTS_IN_CALLBACK]);
        Option expectedAnswer = ts.getTest().get(ts.getCurrentQuestion() - 1).getAnswer();
        boolean isRight = currentAnswer.equals(expectedAnswer);

        //save answer into the state
        List<TestQuestion> test = ts.getTest();
        List<TestResult> results = ts.getResults();
        results.add(new TestResult(test.get(0).getTestType(), test.get(0).getArticle(), test.get(ts.getCurrentQuestion() - 1).getQuestion(),
                test.get(ts.getCurrentQuestion() - 1).getOptions(), currentAnswer, "", isRight));
        ts.setResults(results);
        GrammarBot.stateMap.put(update.getCallbackQuery().getFrom().getId(), ts);

        return isRight;
    }

    private boolean evaluateWrittenAnswer(Update update, TestState ts, String userMessage) {
        boolean isRight = false;
        String[] options = ts.getTest().get(ts.getCurrentQuestion() - 1)
                .getAnswerWriting().split(SysConstants.DELIMITER_FOR_ALTERNATIVE_OPTIONS);
        //if there are more than one question in this question
        if (options.length > 1) { //1.test # test  2.test # test
            String[] userMessageSplitted = userMessage.split(SysConstants.DELIMITER_FOR_ALTERNATIVE_OPTIONS); // 1.test   2.test
            Map<Integer, Boolean> ifAllIsRight = new HashMap<>();
            for (int i = 0; i < options.length; i++) {
                ifAllIsRight.put(i, false);
                String[] optionSplitted = options[i].split(SysConstants.DELIMITER_FOR_WRITTEN_ANSWERS); // 1.test  2.test
                for (int j = 0; j < optionSplitted.length; j++) {
                    if (optionSplitted[j] != null && userMessageSplitted.length> i) {
                        if (optionSplitted[j].toLowerCase().strip().equals(userMessageSplitted[i].toLowerCase().strip()) && ifAllIsRight.get(i) == false)
                            ifAllIsRight.put(i, true);
                    }
                }
            }
            isRight = true;
            for (int k : ifAllIsRight.keySet()) {
                if (!ifAllIsRight.get(k))
                    isRight = false;
            }
            //if in question only one question
        } else {
            String[] optionsSplitted = ts.getTest().get(ts.getCurrentQuestion() - 1)
                    .getAnswerWriting().split(SysConstants.DELIMITER_FOR_WRITTEN_ANSWERS);
            isRight = false;
            for (String s : optionsSplitted) {
                if (s.toLowerCase().strip().equals(userMessage.toLowerCase().strip()))
                    isRight = true;
            }
        }

        //save answer into the state
        List<TestQuestion> test = ts.getTest();
        List<TestResult> results = ts.getResults();
        results.add(new TestResult(test.get(0).getTestType(), test.get(0).getArticle(), test.get(ts.getCurrentQuestion() - 1).getQuestion(),
                test.get(ts.getCurrentQuestion() - 1).getOptions(), Option.A, userMessage, isRight));
        ts.setResults(results);
        GrammarBot.stateMap.put(update.getMessage().getFrom().getId(), ts);

        return isRight;
    }
}
