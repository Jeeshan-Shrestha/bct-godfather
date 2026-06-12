package com.bct.bct_godfather.config;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bct.bct_godfather.service.DocxToPdfService;
import com.bct.bct_godfather.service.PptxToPdfService;

import java.io.IOException;

@Component
public class DocxConvertListener extends ListenerAdapter {

    private final DocxToPdfService docxToPdfService;
    private final PptxToPdfService pptxToPdfService;

    public DocxConvertListener(DocxToPdfService docxToPdfService, PptxToPdfService pptxToPdfService) {
        this.docxToPdfService = docxToPdfService;
        this.pptxToPdfService = pptxToPdfService;
    }

    public static SlashCommandData getCommandData() {
        return Commands.slash("docx-to-pdf", "Convert a DOCX file to PDF")
                .addOption(OptionType.ATTACHMENT, "file", "The DOCX file to convert", true);
    }

    public void handleDocxToPdfConversion(SlashCommandInteractionEvent event){

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

    public void handlePPTXtoPDFConversion(SlashCommandInteractionEvent event){

        Message.Attachment attachment = event.getOption("file").getAsAttachment();

    if (!attachment.getFileName().toLowerCase().endsWith(".pptx")) {
        event.reply("Please upload a `.pptx` file.").setEphemeral(true).queue();
        return;
    }

    event.deferReply().queue();

    attachment.getProxy().download().thenAccept(inputStream -> {
        try {
            byte[] pptxBytes = inputStream.readAllBytes();

            byte[] pdfBytes = pptxToPdfService.convert(pptxBytes);

            String outputName = attachment.getFileName()
                    .replaceAll("(?i)\\.pptx$", ".pdf");

            event.getHook()
                    .sendFiles(FileUpload.fromData(pdfBytes, outputName))
                    .queue();

        } catch (IOException e) {
            event.getHook()
                    .sendMessage("Failed to convert: " + e.getMessage())
                    .queue();
        } catch (Exception e) {
            event.getHook()
                    .sendMessage("Conversion error: " + e.getMessage())
                    .queue();
        }

    }).exceptionally(ex -> {
        event.getHook()
                .sendMessage("Failed to download file: " + ex.getMessage())
                .queue();
        return null;
    });


    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch(event.getName()){
            case "docx-to-pdf" -> handleDocxToPdfConversion(event);
            case "pptx-to-pdf" -> handlePPTXtoPDFConversion(event);
        }
    }
}