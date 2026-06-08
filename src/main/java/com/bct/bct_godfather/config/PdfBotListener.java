package com.bct.bct_godfather.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bct.bct_godfather.service.PdfToDocxService;
import com.itextpdf.io.exceptions.IOException;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

@Component
public class PdfBotListener extends ListenerAdapter{

    @Autowired PdfToDocxService pdfToDocxService;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("pdf-to-docx")) return;

        Message.Attachment attachment = event.getOption("file").getAsAttachment();
        event.deferReply().queue();

        attachment.getProxy().download().thenAccept(inputStream -> {
            try {
                byte[] pdfBytes = inputStream.readAllBytes();
                byte[] docxBytes = pdfToDocxService.convert(pdfBytes);
                String outputName = attachment.getFileName().replaceAll("(?i)\\.pdf$", ".docx");

                event.getHook()
                    .sendFiles(FileUpload.fromData(docxBytes, outputName))
                    .queue();
            } catch (IOException e) {
                event.getHook().sendMessage("Conversion failed: " + e.getMessage()).queue();
            } catch (java.io.IOException e) {
                event.getHook().sendMessage("Conversion failed: " + e.getMessage()).queue();
            }
        });
}
    


}
