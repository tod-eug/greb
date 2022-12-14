package db;

import bot.enums.Option;
import bot.enums.TestType;
import dto.TestQuestion;
import dto.TestResult;
import dto.TestState;
import mapper.TestQuestionMapper;
import org.telegram.telegrambots.meta.api.objects.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ResultsHelper {

    public void createAttempt(TestState ts, User user, String chatId) {
        UUID id = UUID.randomUUID();
        SimpleDateFormat formatter = new SimpleDateFormat(DatabaseHelper.pattern);
        String createdDate = formatter.format(new Date());
        UsersHelper uh = new UsersHelper();
        String userId = uh.findUserByTgId(ts.getUserId().toString(), user, chatId);

        String insertQuery = String.format("insert into attempt (id, user_id, test_code, attempt_code, create_date) VALUES ('%s', '%s', '%s', '%s', '%s');",
                id, userId, ts.getTestCode(), ts.getAttemptCode(), createdDate);

        DatabaseHelper dbHelper = new DatabaseHelper();
        try {
            dbHelper.getPreparedStatement(insertQuery).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnections();
        }
    }

    public String findAttemptIdByAttemptCode(TestState ts) {
        String selectQuery = String.format("select id from public.attempt a where attempt_code = '%s';", ts.getAttemptCode());

        DatabaseHelper dbHelper = new DatabaseHelper();
        String id = "";
        try {
            ResultSet st = dbHelper.getPreparedStatement(selectQuery).executeQuery();
            if (st.next()) {
                id = st.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnections();
        }
        return id;
    }

    public void createResult(TestState ts, String currentAnswer, boolean isRight) {
        TestQuestion testQuestion = ts.getTest().get(ts.getCurrentQuestion() - 1);

        String attemptId = findAttemptIdByAttemptCode(ts);
        UUID id = UUID.randomUUID();
        String question = testQuestion.getQuestion();
        TestType type = testQuestion.getTestType();
        String article = testQuestion.getArticle();
        SimpleDateFormat formatter = new SimpleDateFormat(DatabaseHelper.pattern);
        String createdDate = formatter.format(new Date());
        String correctAnswer = "";

        String options = "";
        if (type == TestType.normal || type == TestType.article) {
            options = getOptionsAsString(testQuestion.getOptions());
            correctAnswer = testQuestion.getAnswer().toString();
        } else {
            correctAnswer = testQuestion.getAnswerWriting();
        }



        String insertQuery = String.format("insert into results (id, attempt_id, type, article, question, is_right, answer, answer_writing, correct_answer, options, create_date) VALUES ('%s', '%s', '%s', '%s', '%s', %s, '%s', '%s', '%s', '%s', '%s');",
                id, attemptId, type.name(), normalizeString(article), normalizeString(question), isRight, currentAnswer, currentAnswer, correctAnswer, options, createdDate);

        DatabaseHelper dbHelper = new DatabaseHelper();
        try {
            dbHelper.getPreparedStatement(insertQuery).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnections();
        }
    }

    public List<TestResult> getResultsByAttemptCode(TestState ts) {
        String attemptId = findAttemptIdByAttemptCode(ts);
        String selectQuery = String.format("select type, article, question, options, answer, correct_answer, is_right from public.results where attempt_id  = '%s' order by create_date;", attemptId);

        DatabaseHelper dbHelper = new DatabaseHelper();
        TestQuestionMapper testQuestionMapper = new TestQuestionMapper();
        List<TestResult> result = new ArrayList<>();
        try {
            ResultSet st = dbHelper.getPreparedStatement(selectQuery).executeQuery();

            while (st.next()) {
                TestType testType = testQuestionMapper.mapTestType(st.getString(1));
                Map<Option, String> optionMap = new HashMap<>();
                Option answer = Option.Z;
                if (testType == TestType.normal || testType == TestType.article) {
                    String[] parsedGluing = st.getString(4).split("#");
                    for (String s : parsedGluing) {
                        String[] parsedOptions = s.split("-");
                        optionMap.put(testQuestionMapper.mapOption(parsedOptions[0]), parsedOptions[1]);
                    }
                    answer = testQuestionMapper.mapOption(st.getString(3));
                } else {

                }
                result.add(new TestResult(testType, st.getString(2), st.getString(3), optionMap, answer, st.getString(6), st.getObject(7, Boolean.class)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnections();
        }
        return result;
    }

    private String getOptionsAsString(Map<Option, String> map) {
        String result = "";
        Set<Option> set = map.keySet();
        for (Option o : set) {
            String value = map.get(o);
            result = result + o.toString() + "-" + normalizeString(value) + "#";
        }
        return result.substring(0, result.length() - 1);
    }

    private String normalizeString(String string) {
        return string.replace("'", "''");
    }
}
