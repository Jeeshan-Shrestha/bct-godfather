package com.bct.bct_godfather.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
public class HomeworkReminder {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subject;
    private String description;
    
    private LocalDateTime deadline;
    private boolean oneDayReminderSent;
    private boolean deadlineReminderSent;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public LocalDateTime getDeadline() {
        return deadline;
    }
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
    public boolean isOneDayReminderSent() {
        return oneDayReminderSent;
    }
    public void setOneDayReminderSent(boolean oneDayReminderSent) {
        this.oneDayReminderSent = oneDayReminderSent;
    }
    public boolean isDeadlineReminderSent() {
        return deadlineReminderSent;
    }
    public void setDeadlineReminderSent(boolean deadlineReminderSent) {
        this.deadlineReminderSent = deadlineReminderSent;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    
}
