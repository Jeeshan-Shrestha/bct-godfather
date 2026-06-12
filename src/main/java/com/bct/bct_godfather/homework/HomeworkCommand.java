package com.bct.bct_godfather.homework;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.bct.bct_godfather.entity.HomeworkReminder;
import com.bct.bct_godfather.repo.HomeworkReminderRepository;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;


@Component
public class HomeworkCommand extends ListenerAdapter {

    private static final String ALLOWED_ROLE = "Amar";

    private final HomeworkReminderRepository repo;

    HomeworkCommand(HomeworkReminderRepository repo) {
        this.repo = repo;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        
        switch(event.getName()){
            case "homework" -> handleHomework(event);
            case "get-homework" -> handleGetHomework(event);
        }
    
    }

    public void handleGetHomework(SlashCommandInteractionEvent event){
        
        List<HomeworkReminder> allHomework = repo.findAll();
        if (allHomework.isEmpty()){
            event.getChannel().sendMessage("No Homework YAY").queue();
            return ;
        }
        for (HomeworkReminder homework : allHomework){

            EmbedBuilder embed = new EmbedBuilder()
            .setTitle("📚 Homework Reminder")
            .setColor(Color.ORANGE)
            .addField("Subject", homework.getSubject(), false)
            .addField("Description", homework.getDescription(), false)
            .addField("⏳ Deadline", homework.getDeadline().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")), false);


            event.getChannel().sendMessage("Homework Assigned")
            .setEmbeds(embed.build())
            .queue();
        }

    }

    public void handleHomework(SlashCommandInteractionEvent event){
        
        boolean hasRole = event.getMember().getRoles().stream()
            .anyMatch(r -> r.getName().equalsIgnoreCase(ALLOWED_ROLE));

        if (!hasRole) {
            event.reply("You don't have permission to use this command.").setEphemeral(true).queue();
            return;
        }
        

        String subject = event.getOption("subject").getAsString();
        int days = event.getOption("days").getAsInt();
        String time = event.getOption("time").getAsString(); // "HH:mm"

        LocalTime parsedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm"));
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
