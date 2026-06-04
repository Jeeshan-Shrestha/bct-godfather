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
                String helpMessage = "```Available commands:\n" +
                        "!help - Show this help message\n" +
                        "ping dikesh - Spam ping dikesh \n"+
                        "@BCT081-GodFather - Get a helpful reply\n"+
                        "?afk [reason] - Set your AFK status with an optional reason\n```";
    
                event.getChannel().sendMessage(helpMessage).queue();
            }
        }

}
