package com.bct.bct_godfather.homework;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bct.bct_godfather.entity.HomeworkReminder;
import com.bct.bct_godfather.repo.HomeworkReminderRepository;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@Component
public class HomeworkCommand extends ListenerAdapter {

    private static final String ALLOWED_ROLE = "Amar";

    @Autowired
    private HomeworkReminderRepository repo;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("homework")) return;

        boolean hasRole = event.getMember().getRoles().stream()
            .anyMatch(r -> r.getName().equalsIgnoreCase(ALLOWED_ROLE));

        if (!hasRole) {
            event.reply("You don't have permission to use this command.").setEphemeral(true).queue();
            return;
        }
        

        String subject = event.getOption("subject").getAsString();
        int days = event.getOption("days").getAsInt();
        String time = event.getOption("time").getAsString(); // "HH:mm"

        LocalTime parsedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime deadline = LocalDateTime.now().plusDays(days)
            .withHour(parsedTime.getHour())
            .withMinute(parsedTime.getMinute())
            .withSecond(0).withNano(0);

        String description = event.getOption("description") != null 
    ? event.getOption("description").getAsString() 
    : "No description provided";

    
    
    HomeworkReminder reminder = new HomeworkReminder();
    reminder.setSubject(subject);
    reminder.setDeadline(deadline);
    reminder.setDescription(description);
        repo.save(reminder);

        event.reply(String.format("✅ Reminder set for **%s** — due %s", subject, deadline.toString())).queue();
    }
}
