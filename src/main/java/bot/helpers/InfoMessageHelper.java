package bot.helpers;

import bot.enums.TestType;
import dto.TestResult;
import dto.TestState;

import java.util.List;

public class InfoMessageHelper {

    public String getMessage(TestState ts) {
        int currentQuestionNumber = ts.getCurrentQuestion();
        int amountOfQuestions = ts.getTest().size();
        boolean firstQuestion = currentQuestionNumber == 0;
        boolean lastQuestion = currentQuestionNumber == amountOfQuestions;

        String result = "";

        if (lastQuestion) {
            result = getPreviousQuestionResults(result, ts);
            result = getOverallResults(result, ts);
        } else {
            if (!firstQuestion) {
                result = getPreviousQuestionResults(result, ts);
            }
            result = getTestNameLine(result, ts);
            result = getNextQuestionLine(result, ts, currentQuestionNumber, amountOfQuestions);
        }
        return result;
    }


    private String getTestNameLine(String s, TestState ts) {
        String category = ts.getCategory();
        String testName = ts.getTest().get(0).getName();

        StringBuilder sb = new StringBuilder(s);
        return sb.append("<b>").append(category).append(".</b> <b>").append(testName).append("</b>\n").toString();
    }

    private String getNextQuestionLine(String s, TestState ts, int currentQuestionNumber, int amountOfQuestions) {
        String testTask = ts.getTest().get(0).getTask();

        StringBuilder sb = new StringBuilder(s);
        String result = sb.append("<b>").append(normalizeInt(currentQuestionNumber)).append("/").append(amountOfQuestions).append(".</b> ").append(testTask).toString();

        if (ts.getTest().get(0).getTestType() == TestType.normalWriting || ts.getTest().get(0).getTestType() == TestType.articleWriting) {
            StringBuilder sbuilder = new StringBuilder(result);
            result = sbuilder.append("\n<b>If a question has more than one gap please use \"-\" between the answers. Send please all answers in one message.</b>").toString();
        }
        return result;
    }

    private String getPreviousQuestionResults(String s, TestState ts) {
        int currentQuestionNumber = ts.getCurrentQuestion();
        boolean isRight = ts.getResults().get(currentQuestionNumber - 1).isRight();
        String previousQuestion = ts.getTest().get(currentQuestionNumber - 1).getQuestion();
        String userAnswer = "";
        String answer = "";
        if (ts.getTest().get(0).getTestType() == TestType.normal || ts.getTest().get(0).getTestType() == TestType.article) {
            userAnswer = ts.getTest().get(currentQuestionNumber - 1).getOptions().get(ts.getResults().get(currentQuestionNumber - 1).getAnswer());
            answer = ts.getTest().get(currentQuestionNumber - 1).getOptions().get(ts.getTest().get(currentQuestionNumber - 1).getAnswer());
        } else {
            userAnswer = ts.getResults().get(currentQuestionNumber - 1).getAnswerWriting();
            answer = ts.getTest().get(currentQuestionNumber - 1).getAnswerWriting().replace("#", "<b>or</b>");
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

    private String getOverallResults(String s, TestState ts) {
        List<TestResult> results = ts.getResults();
        int allQuestionsAmount = results.size();
        int rightAnswers = 0;
        for (TestResult nr : results) {
            if (nr.isRight())
                rightAnswers++;
        }
        String testName = ts.getTest().get(0).getName();

        StringBuilder sb = new StringBuilder(s);
        return sb.append("❤ <b>").append(testName).append("</b> from category <b>").append(ts.getCategory()).append("</b> is complete!\n")
                .append("<b>All questions:</b> ").append(allQuestionsAmount).append(". <b>Right answers:</b> ").append(rightAnswers).append(".\n\n").toString();
    }

    private int normalizeInt(int number) {
        return ++number;
    }
}
