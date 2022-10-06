package db;

import bot.enums.Option;
import bot.enums.TestType;
import dto.ProcessingTestState;
import dto.TestQuestion;
import dto.TestResult;
import mapper.TestQuestionMapper;
import org.telegram.telegrambots.meta.api.objects.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ResultsHelper {

    public void createAttempt(ProcessingTestState processingTestState, User user, String chatId) {
        UUID id = UUID.randomUUID();
        SimpleDateFormat formatter = new SimpleDateFormat(DatabaseHelper.pattern);
        String createdDate = formatter.format(new Date());
        UsersHelper uh = new UsersHelper();
        String userId = uh.findUserByTgId(processingTestState.getUserId().toString(), user, chatId);

        String insertQuery = String.format("insert into attempt (id, user_id, test_code, attempt_code, create_date) VALUES ('%s', '%s', '%s', '%s', '%s');",
                id, userId, processingTestState.getTestCode(), processingTestState.getAttemptCode(), createdDate);

        DatabaseHelper dbHelper = new DatabaseHelper();
        try {
            dbHelper.getPreparedStatement(insertQuery).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnections();
        }
    }

    public String findAttemptIdByAttemptCode(ProcessingTestState processingTestState) {
        String selectQuery = String.format("select id from public.attempt a where attempt_code = '%s';", processingTestState.getAttemptCode());

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

    public void createResult(ProcessingTestState processingTestState, String currentAnswer, boolean isRight) {
        TestQuestion testQuestion = processingTestState.getTest().get(processingTestState.getCurrentQuestion() - 1);

        String attemptId = findAttemptIdByAttemptCode(processingTestState);
        UUID id = UUID.randomUUID();
        String question = testQuestion.getQuestion();
        TestType type = testQuestion.getTestType();
        String article = testQuestion.getArticle();
        SimpleDateFormat formatter = new SimpleDateFormat(DatabaseHelper.pattern);
        String createdDate = formatter.format(new Date());

        String options = "";
        if (type == TestType.normal || type == TestType.article)
            options = getOptionsAsString(testQuestion.getOptions());

        String insertQuery = String.format("insert into results (id, attempt_id, type, article, question, is_right, answer, answer_writing, options, create_date) VALUES ('%s', '%s', '%s', '%s', '%s', %s, '%s', '%s', '%s', '%s');",
                id, attemptId, type.name(), normalizeString(article), normalizeString(question), isRight, currentAnswer, currentAnswer, options, createdDate);

        DatabaseHelper dbHelper = new DatabaseHelper();
        try {
            dbHelper.getPreparedStatement(insertQuery).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnections();
        }
    }

    public List<TestResult> getResultsByAttemptCode(ProcessingTestState processingTestState) {
        String attemptId = findAttemptIdByAttemptCode(processingTestState);
        String selectQuery = String.format("select type, article, question, options, answer, is_right from public.results where attempt_id  = '%s' order by create_date;", attemptId);

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
                }
                result.add(new TestResult(testType, st.getString(2), st.getString(3), optionMap, answer, st.getString(5), st.getObject(6, Boolean.class)));
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
