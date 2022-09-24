package db;

import bot.enums.Option;
import dto.CurrentUserTestState;
import dto.TestQuestion;
import dto.TestResult;
import mapper.TestQuestionMapper;
import org.telegram.telegrambots.meta.api.objects.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ResultsHelper {

    public void createAttempt(CurrentUserTestState currentUserTestState, User user) {
        UUID id = UUID.randomUUID();
        SimpleDateFormat formatter = new SimpleDateFormat(DatabaseHelper.pattern);
        String createdDate = formatter.format(new Date());
        UsersHelper uh = new UsersHelper();
        String userId = uh.findUserByTgId(currentUserTestState.getUserId().toString(), user);

        String insertQuery = String.format("insert into attempt (id, user_id, test_code, attempt_code, create_date) VALUES ('%s', '%s', '%s', '%s', '%s');",
                id, userId, currentUserTestState.getTestCode(), currentUserTestState.getAttemptCode(), createdDate);

        DatabaseHelper dbHelper = new DatabaseHelper();
        try {
            dbHelper.getPreparedStatement(insertQuery).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnections();
        }
    }

    public String findAttemptIdByAttemptCode(CurrentUserTestState currentUserTestState) {
        String selectQuery = String.format("select id from public.attempt a where attempt_code = '%s';", currentUserTestState.getAttemptCode());

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

    public void createResult(CurrentUserTestState currentUserTestState, String currentAnswer, boolean isRight) {
        TestQuestion testQuestion = currentUserTestState.getTest().get(currentUserTestState.getCurrentQuestion() - 1);

        String attemptId = findAttemptIdByAttemptCode(currentUserTestState);
        UUID id = UUID.randomUUID();
        String question = testQuestion.getQuestion();
        String options = getOptionsAsString(currentUserTestState.getTest().get(currentUserTestState.getCurrentQuestion() - 1).getOptions());
        SimpleDateFormat formatter = new SimpleDateFormat(DatabaseHelper.pattern);
        String createdDate = formatter.format(new Date());

        String insertQuery = String.format("insert into results (id, attempt_id, question, is_right, answer, options, create_date) VALUES ('%s', '%s', '%s', %s, '%s', '%s', '%s');",
                id, attemptId, normalizeString(question), isRight, currentAnswer, options, createdDate);

        DatabaseHelper dbHelper = new DatabaseHelper();
        try {
            dbHelper.getPreparedStatement(insertQuery).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnections();
        }
    }

    public List<TestResult> getResultsByAttemptCode(CurrentUserTestState currentUserTestState) {
        String attemptId = findAttemptIdByAttemptCode(currentUserTestState);
        String selectQuery = String.format("select question, options, answer, is_right from public.results where attempt_id  = '%s' order by create_date;", attemptId);

        DatabaseHelper dbHelper = new DatabaseHelper();
        TestQuestionMapper testQuestionMapper = new TestQuestionMapper();
        List<TestResult> result = new ArrayList<>();
        try {
            ResultSet st = dbHelper.getPreparedStatement(selectQuery).executeQuery();

            while (st.next()) {
                Map<Option, String> optionMap = new HashMap<>();
                String[] parsedGluing = st.getString(2).split("#");
                for (String s : parsedGluing) {
                    String[] parsedOptions = s.split("-");
                    optionMap.put(testQuestionMapper.mapOption(parsedOptions[0]), parsedOptions[1]);
                }
                result.add(new TestResult(st.getString(1), optionMap, testQuestionMapper.mapOption(st.getString(3)), st.getObject(4, Boolean.class)));
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
