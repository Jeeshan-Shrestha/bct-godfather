package com.bct.bct_godfather.config;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.stereotype.Component;

import com.bct.bct_godfather.service.DocxToPdfService;

import java.io.IOException;

@Component
public class DocxConvertListener extends ListenerAdapter {

    private final DocxToPdfService docxToPdfService;

    public DocxConvertListener(DocxToPdfService docxToPdfService) {
        this.docxToPdfService = docxToPdfService;
    }

    public static SlashCommandData getCommandData() {
        return Commands.slash("docx-to-pdf", "Convert a DOCX file to PDF")
                .addOption(OptionType.ATTACHMENT, "file", "The DOCX file to convert", true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("docx-to-pdf")) return;

        Message.Attachment attachment = event.getOption("file").getAsAttachment();

        if (!attachment.getFileName().toLowerCase().endsWith(".docx")) {
            event.reply("Please upload a `.docx` file.").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        attachment.getProxy().download().thenAccept(inputStream -> {
            try {
                byte[] docxBytes = inputStream.readAllBytes();
                byte[] pdfBytes = docxToPdfService.convert(docxBytes);

                String outputName = attachment.getFileName()
                        .replaceAll("(?i)\\.docx$", ".pdf");

                event.getHook()
                        .sendFiles(FileUpload.fromData(pdfBytes, outputName))
                        .queue();

            } catch (IOException e) {
                event.getHook()
                        .sendMessage("Failed to convert: " + e.getMessage())
                        .queue();
            } catch (InterruptedException e) {
                event.getHook()
                        .sendMessage("Failed to convert: " + e.getMessage())
                        .queue();
            }
        }).exceptionally(ex -> {
            event.getHook()
                    .sendMessage("Failed to download the file: " + ex.getMessage())
                    .queue();
            return null;
        });
    }
}