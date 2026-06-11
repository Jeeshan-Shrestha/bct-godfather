package com.bct.bct_godfather.config;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class BotHelpListener extends ListenerAdapter {
    
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            super.onMessageReceived(event); // Call the parent method to handle existing logic
    
            String content = event.getMessage().getContentRaw();
    
            if (content.equalsIgnoreCase("..!help")) {
                String helpMessage = "```Available commands:\n\n" +
                        "!help - Show this help message\n\n" +
                        "ping dikesh - Spam ping dikesh \n\n"+
                        "@BCT081-GodFather - Get a helpful reply\n\n"+
                        "?afk [reason] - Set your AFK status with an optional reason\n\n" +
                        "/manga {manga_name} - to search for the mangas \n\n"+
                        "/chapters {manga_id} - to get all the chapters from the manga \n\n" + 
                        "/read {chapter_id} - get the downloadable pdf file for that chapter \n\n"+
                        "/pdf-to-docx {file.pdf} - converts pdf to docx file\n\n"+
                        "/docx-to-pdx {file.docx} - converts docx to pdf file\n\n"+
                        "/homework {subj} {descriptio} {no_of_days} {time} - sets reminder for the homework\n\n"+
                        "/cover {roll_no} - get the cover page with your name on it for DSA\n\n```";
    
                event.getChannel().sendMessage(helpMessage).queue();
            }
        }

}
