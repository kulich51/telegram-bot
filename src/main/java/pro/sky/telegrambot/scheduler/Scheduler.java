package pro.sky.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Component
public class Scheduler {

    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;

    public Scheduler(TelegramBot telegramBot, NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    /**
     * Send message to user if task started
     */
    @Scheduled(cron = "${cron.expression}")
    public void remindTask() {
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Collection<NotificationTask> tasks = notificationTaskRepository.findNotificationTaskByTaskDateAndAndTaskTime(
                currentTime.toLocalDate(),
                currentTime.toLocalTime()
        );

        final String message =  "Время выполнить задачу: ";
        for (NotificationTask task : tasks) {
            telegramBot.execute(new SendMessage(task.getChatId(), message + task.getTaskDescription()));
        }
    }
}
