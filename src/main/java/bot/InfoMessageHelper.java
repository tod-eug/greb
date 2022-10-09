package bot;

import bot.enums.TestType;
import dto.ProcessingTestState;
import dto.TestResult;

import java.util.List;

public class InfoMessageHelper {

    public String getMessage(ProcessingTestState processingTestState) {
        int currentQuestionNumber = processingTestState.getCurrentQuestion();
        int amountOfQuestions = processingTestState.getTest().size();
        boolean firstQuestion = currentQuestionNumber == 0;
        boolean lastQuestion = currentQuestionNumber == amountOfQuestions;

        String result = "";

        if (lastQuestion) {
            result = getPreviousQuestionResults(result, processingTestState);
            result = getOverallResults(result, processingTestState);
        } else {
            if (!firstQuestion) {
                result = getPreviousQuestionResults(result, processingTestState);
            }
            result = getTestNameLine(result, processingTestState);
            result = getNextQuestionLine(result, processingTestState, currentQuestionNumber, amountOfQuestions);
        }
        return result;
    }


    private String getTestNameLine(String s, ProcessingTestState processingTestState) {
        String category = processingTestState.getCategory();
        String testName = processingTestState.getTest().get(0).getName();

        StringBuilder sb = new StringBuilder(s);
        return sb.append("<b>").append(category).append(".</b> <b>").append(testName).append("</b>\n").toString();
    }

    private String getNextQuestionLine(String s, ProcessingTestState processingTestState, int currentQuestionNumber, int amountOfQuestions) {
        String testTask = processingTestState.getTest().get(0).getTask();

        StringBuilder sb = new StringBuilder(s);
        return sb.append("<b>").append(normalizeInt(currentQuestionNumber)).append("/").append(amountOfQuestions).append(".</b> ").append(testTask).toString();
    }

    private String getPreviousQuestionResults(String s, ProcessingTestState processingTestState) {
        int currentQuestionNumber = processingTestState.getCurrentQuestion();
        boolean isRight = processingTestState.getResults().get(currentQuestionNumber - 1).isRight();
        String previousQuestion = processingTestState.getTest().get(currentQuestionNumber - 1).getQuestion();
        String userAnswer = "";
        String answer = "";
        if (processingTestState.getTest().get(0).getTestType() == TestType.normal || processingTestState.getTest().get(0).getTestType() == TestType.article) {
            userAnswer = processingTestState.getTest().get(currentQuestionNumber - 1).getOptions().get(processingTestState.getResults().get(currentQuestionNumber - 1).getAnswer());
            answer = processingTestState.getTest().get(currentQuestionNumber - 1).getOptions().get(processingTestState.getTest().get(currentQuestionNumber - 1).getAnswer());
        } else {
            userAnswer = processingTestState.getResults().get(currentQuestionNumber - 1).getAnswerWriting();
            answer = processingTestState.getTest().get(currentQuestionNumber - 1).getAnswerWriting();
        }

        StringBuilder sb = new StringBuilder(s);
        String result = "";
        if (isRight) {
            result = sb.append("✅ <b>Right!</b>\n\n").toString();
        } else {
            result = sb.append("❌ <b>Wrong!</b>\n\n")
                    .append("<b>Previous question:</b> ")
                    .append(previousQuestion).append("\n")
                    .append("<b>Answer:</b> ").append(answer).append(". <b>Your answer:</b> ").append(userAnswer).append(".\n\n").toString();
        }

        return result;
    }

    private String getOverallResults(String s, ProcessingTestState processingTestState) {
        List<TestResult> results = processingTestState.getResults();
        int allQuestionsAmount = results.size();
        int rightAnswers = 0;
        for (TestResult nr : results) {
            if (nr.isRight())
                rightAnswers++;
        }

        StringBuilder sb = new StringBuilder(s);
        return sb.append("❤ <b>Test is complete!</b>\n")
                .append("<b>All questions:</b> ").append(allQuestionsAmount).append(". <b>Right answers:</b> ").append(rightAnswers).append(".\n\n").toString();
    }

    private int normalizeInt(int number) {
        return ++number;
    }
}
