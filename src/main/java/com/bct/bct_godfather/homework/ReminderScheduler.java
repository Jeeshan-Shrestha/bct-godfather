package com.bct.bct_godfather.homework;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bct.bct_godfather.entity.HomeworkReminder;
import com.bct.bct_godfather.repo.HomeworkReminderRepository;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import net.dv8tion.jda.api.EmbedBuilder;
import java.awt.Color;
import java.time.format.DateTimeFormatter;

@Component
public class ReminderScheduler {

    @Value("${reminder.channel.id}")
    private String channelId;

    @Autowired
    private JDA jda;

    @Autowired
    private HomeworkReminderRepository repo;

    @Scheduled(fixedDelay = 60000)
public void checkReminders() {
    List<HomeworkReminder> reminders = repo.findByDeadlineReminderSentFalse();
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kathmandu"));
    TextChannel channel = jda.getTextChannelById(channelId);
    if (channel == null) return;

    for (HomeworkReminder r : reminders) {
        LocalDateTime deadline = r.getDeadline();

        if (!r.isOneDayReminderSent() && now.isAfter(deadline.minusDays(1))) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📚 Homework Reminder")
                .setColor(Color.ORANGE)
                .addField("Subject", r.getSubject(), false)
                .addField("Description", r.getDescription(), false)
                .addField("⏳ Deadline", deadline.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")), false)
                .setFooter("You have ~1 day left! Get to work.");

            channel.sendMessage("@everyone")
                .setEmbeds(embed.build())
                .queue();

            r.setOneDayReminderSent(true);
            repo.save(r);

        } else if (!r.isDeadlineReminderSent() && now.isAfter(deadline)) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🚨 Deadline Reached!")
                .setColor(Color.RED)
                .addField("Subject", r.getSubject(), false)
                .addField("Description", r.getDescription(), false)
                .addField("🕐 Was due at", deadline.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")), false)
                .setFooter("Submission time is over.");

            channel.sendMessage("@everyone")
                .setEmbeds(embed.build())
                .queue();

            r.setDeadlineReminderSent(true);
            repo.save(r);
            repo.delete(r);
        }
    }
}
}