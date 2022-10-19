package bot.helpers;

import bot.ReplyConstants;
import bot.keyboards.CategoriesKeyboard;
import dto.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoriesMessageHelper {

    public SendMessage initiateTestingProcess(Long userId, Long chatId, Map<String, List<Test>> categories, String categoryChooseTimestamp) {
        String categoriesList = "";
        Set<String> set = categories.keySet();
        if (!set.isEmpty()) {
            for (String s : set) {
                categoriesList = categoriesList + s + "\n";
            }
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(ReplyConstants.TESTS_COMMAND + categoriesList);
        sendMessage.setReplyMarkup(CategoriesKeyboard.getCategoriesKeyboard(set, userId, categoryChooseTimestamp));
        return sendMessage;
    }
}
