package pro.sky.telegrambot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity(name = "notification_task")
public class NotificationTask {

    @Id
    @GeneratedValue()
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "task")
    private String taskDescription;

    @Column(name = "task_date")
    private LocalDate taskDate;

    @Column(name = "task_time")
    private LocalTime taskTime;

    public NotificationTask() {
    }

    public NotificationTask(Long chatId, String taskDescription, LocalDate taskDate, LocalTime taskTime) {
        this.chatId = chatId;
        this.taskDescription = taskDescription;
        this.taskDate = taskDate;
        this.taskTime = taskTime;
    }

    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public LocalDate getTaskDate() {
        return taskDate;
    }

    public LocalTime getTaskTime() {
        return taskTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public void setTaskDate(LocalDate taskDate) {
        this.taskDate = taskDate;
    }

    public void setTaskTime(LocalTime taskTime) {
        this.taskTime = taskTime;
    }
}
