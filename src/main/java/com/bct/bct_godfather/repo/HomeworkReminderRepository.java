package com.bct.bct_godfather.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bct.bct_godfather.entity.HomeworkReminder;


public interface HomeworkReminderRepository extends JpaRepository<HomeworkReminder, Long> {
    List<HomeworkReminder> findByDeadlineReminderSentFalse();
}