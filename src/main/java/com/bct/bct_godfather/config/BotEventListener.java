package com.bct.bct_godfather.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bct.bct_godfather.service.CohereService;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class BotEventListener extends ListenerAdapter {

    @Autowired CohereService cohereService;


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println("Message received!");
        
        if (event.getAuthor().isBot()) return; // Ignore messages from bots
        

        Message message = event.getMessage();
        String content = message.getContentRaw();

        if (content.equalsIgnoreCase("lurang")) {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Lurang is GAY").queue();

        }

        if (content.equalsIgnoreCase("nara")){
            event.getChannel().sendMessage("Nara is GOAT").queue();
        }

        if (message.getMentions().isMentioned(event.getJDA().getSelfUser())) {

            String prompt = content
                    .replace(event.getJDA().getSelfUser().getAsMention(), "")
                    .trim();

            String reply = cohereService.getResponse(event.getChannel().getId(),prompt);
            if (reply.length() > 1900) {
                reply = reply.substring(0, 1900);
            }

            event.getChannel().sendMessage(reply).queue();
        }

    }
    

}
