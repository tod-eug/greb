package bot;

import bot.enums.Option;
import dto.ProcessingTestState;
import mapper.TestQuestionMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public class EvaluateAnswerHelper {

    public boolean evaluateOptionAnswer(Update update, ProcessingTestState processingTestState) {
        TestQuestionMapper testQuestionMapper = new TestQuestionMapper();
        String[] parsedCallbackForOptions = update.getCallbackQuery().getData().split(SysConstants.DELIMITER_FOR_QUESTIONS_CALLBACK);
        Option currentAnswer = testQuestionMapper.mapOption(parsedCallbackForOptions[SysConstants.NUMBER_OF_RESULTS_IN_CALLBACK]);
        Option expectedAnswer = processingTestState.getTest().get(processingTestState.getCurrentQuestion() - 1).getAnswer();
        return currentAnswer.equals(expectedAnswer);
    }

    public boolean evaluateWrittenAnswer(Update update, ProcessingTestState processingTestState, String userMessage) {
        boolean isRight = false;
        String[] options = processingTestState.getTest().get(processingTestState.getCurrentQuestion() - 1)
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
            String[] optionsSplitted = processingTestState.getTest().get(processingTestState.getCurrentQuestion() - 1)
                    .getAnswerWriting().split(SysConstants.DELIMITER_FOR_WRITTEN_ANSWERS);
            isRight = false;
            for (String s : optionsSplitted) {
                if (s.toLowerCase().strip().equals(userMessage.toLowerCase().strip()))
                    isRight = true;
            }
        }
        return isRight;
    }
}
