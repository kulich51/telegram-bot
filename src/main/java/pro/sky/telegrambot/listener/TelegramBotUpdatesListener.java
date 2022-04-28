package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;

    private final NotificationTaskRepository notificationTaskRepository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    private final String TASK_PATTERN = "([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)";
    private final String START_MESSAGE = "Введите задачу. Я напомню вам о ней";
    private final String SUCCESS_SAVE_MESSAGE = "Задача сохранена. Я напомню о ней";
    private final String BAD_DATE_MESSAGE = "Задача не сохранена. Ошибка в дате";
    private final String BAD_RESPONSE_MESSAGE = "Задача не распознана. Вероятно, где-то ошибка";
    private final NotificationTask EMPTY_TASK = new NotificationTask();

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            Message userMessage = getUserMessage(update);

            Matcher taskMatcher = getTaskMatcher(userMessage.text());

            if (userMessage.text().equals("/start")) {
                telegramBot.execute(sendTextMessage(userMessage, START_MESSAGE));
            } else if (taskMatcher.matches()) {
                NotificationTask task = getNewTask(userMessage, taskMatcher);
                String responseMessage = saveTask(task);
                telegramBot.execute(replyToMessage(userMessage, responseMessage));
            } else {
                telegramBot.execute(sendTextMessage(userMessage, BAD_RESPONSE_MESSAGE));
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    /**
     * Returns a Message depending on whether message was written by user: new or edited message
     * @param update an update for processing in bot
     * @return a Message which was written by user
     */
    private Message getUserMessage(Update update) {
        Message userMessage;
        if (update.message() != null) {
            userMessage = update.message();
        } else {
            userMessage = update.editedMessage();
        }
        return userMessage;
    }

    /**
     * Returns a matcher string of user message compared with TASK_PATTERN
     * @param textMessage a user's text message passed in the bot
     * @return a matcher string
     */
    private Matcher getTaskMatcher(String textMessage) {
        Pattern pattern = Pattern.compile(TASK_PATTERN);
        return pattern.matcher(textMessage);
    }

    /**
     * Save task in repository, if date was parsed correctly
     * @param task a created task
     * @return string with success or bad message
     */
    private String saveTask(NotificationTask task) {
        if (task.equals(EMPTY_TASK)) {
            return BAD_DATE_MESSAGE;
        }
        notificationTaskRepository.save(task);
        return SUCCESS_SAVE_MESSAGE;
    }

    /**
     * Create new instance of NotificationTask
     * @param userMessage a user message passed in the bot
     * @param matcher a matcher string
     * @return new NotificationTask object
     * <p>If date was parsed incorrectly when returns empty instance of NotificationTask</p>
     */
    private NotificationTask getNewTask(Message userMessage, Matcher matcher) {

        Long chatId = userMessage.chat().id();
        String taskDescription = matcher.group(3);

        LocalDateTime dateTime;
        try {
            dateTime = parseDateTime(matcher);
        } catch (DateTimeParseException exception) {
            return EMPTY_TASK;
        }
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        return new NotificationTask(chatId, taskDescription, date, time);
    }

    /**
     * Get LocalDateTime object from matcher string
     * @param matcher a matcher string with passed date and task
     * @return an instance of LocalDateTime with date parsed from matcher
     */
    private LocalDateTime parseDateTime(Matcher matcher) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return LocalDateTime.parse(matcher.group(1), formatter);
    }

    /**
     * Create new SendMessage object to send for user
     * @param userMessage a user message which was sent throw the bot
     * @param message text with response message to user
     * @return new SendMessage object
     */
    private SendMessage sendTextMessage(Message userMessage, String message) {

        Long chatId = userMessage.chat().id();

        return new SendMessage(chatId, message)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true);
    }

    /**
     * Create new SendMessage object to send for user with reply for user's message
     * @param userMessage a user message which was sent throw the bot
     * @param message text with response message to user
     * @return new SendMessage object
     */
    private SendMessage replyToMessage(Message userMessage, String message) {
        return sendTextMessage(userMessage, message)
                .replyToMessageId(userMessage.messageId());
    }

}
