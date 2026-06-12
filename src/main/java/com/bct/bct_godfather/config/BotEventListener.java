package com.bct.bct_godfather.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bct.bct_godfather.service.CohereService;
import com.bct.bct_godfather.service.DocxToPdfService;
import com.bct.bct_godfather.service.PptxToPdfService;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

@Component
public class BotEventListener extends ListenerAdapter {

    @Autowired CohereService cohereService;
    @Autowired PptxToPdfService pptxToPdfService;
    @Autowired DocxToPdfService docxToPdfService;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println("Message received!");
        
        if (event.getAuthor().isBot()) return; // Ignore messages from bots
        


        Message message = event.getMessage();
        String content = message.getContentRaw();

        for (Message.Attachment attachment : event.getMessage().getAttachments()) {

        if (!attachment.getFileName().toLowerCase().endsWith(".pptx")) {
            continue;
        }

        attachment.getProxy().download().thenAccept(inputStream -> {

            try {
                byte[] pptxBytes = inputStream.readAllBytes();

                byte[] pdfBytes = pptxToPdfService.convert(pptxBytes);

                String outputName = attachment.getFileName()
                        .replaceAll("(?i)\\.pptx$", ".pdf");

                event.getChannel()
                        .sendFiles(FileUpload.fromData(pdfBytes, outputName))
                        .queue();

            } catch (Exception e) {
                event.getChannel()
                        .sendMessage("Failed to convert `" +
                                attachment.getFileName() +
                                "`: " + e.getMessage())
                        .queue();
            }

        }).exceptionally(ex -> {
            event.getChannel()
                    .sendMessage("Failed to download file: " + ex.getMessage())
                    .queue();
            return null;
        });
    }

    for (Message.Attachment attachment : event.getMessage().getAttachments()) {

        if (!attachment.getFileName().toLowerCase().endsWith(".docx")) {
            continue;
        }

        event.getChannel()
                .sendMessage("Converting `" + attachment.getFileName() + "`...")
                .queue();

        attachment.getProxy().download().thenAccept(inputStream -> {

            try {
                byte[] docxBytes = inputStream.readAllBytes();

                byte[] pdfBytes = docxToPdfService.convert(docxBytes);

                String outputName = attachment.getFileName()
                        .replaceAll("(?i)\\.docx$", ".pdf");

                event.getChannel()
                        .sendFiles(FileUpload.fromData(pdfBytes, outputName))
                        .queue();

            } catch (Exception e) {
                event.getChannel()
                        .sendMessage("Failed to convert `" +
                                attachment.getFileName() +
                                "`: " + e.getMessage())
                        .queue();
            }

        }).exceptionally(ex -> {
            event.getChannel()
                    .sendMessage("Failed to download file: " + ex.getMessage())
                    .queue();
            return null;
        });
    }

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
