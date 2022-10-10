import bot.GrammarBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import sheets.SheetsUtil;
import util.PropertiesProvider;


public class Main {

    public static void main(String[] args) {

        PropertiesProvider.setup();
        SheetsUtil.setup();

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new GrammarBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
