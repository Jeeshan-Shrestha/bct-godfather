package com.bct.student;

import java.awt.Color;

import org.springframework.stereotype.Component;

import com.bct.bct_godfather.entity.BctStudent;
import com.bct.bct_godfather.service.StudentService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

@Component
public class StudentEventListener extends ListenerAdapter{

    private final StudentService studentService;

    public StudentEventListener(StudentService studentService){
        this.studentService = studentService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch(event.getName()){
            case "students" -> handleStudent(event);
        }
    }
    
    public void handleStudent(SlashCommandInteractionEvent event){

        event.deferReply().queue();
        String id = event.getOption("roll_no").getAsString();
        BctStudent student = studentService.getStudentDetailsById(id);

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Student detail of " + student.getName())
            .setColor(Color.ORANGE)
            .addField("Name",student.getName(),false)
            .addField("Date-Of-Birth",student.getDob().toString(),false)
            .addField("Address",student.getAddress(),false)
            .addField("Contact",student.getContact(),false);

        event.getHook().sendMessage("Here is the Details of the student: "+ student.getId())
        .setEmbeds(embed.build())
        .queue();

    }

}
