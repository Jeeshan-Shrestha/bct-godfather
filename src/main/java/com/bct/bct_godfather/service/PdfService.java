package com.bct.bct_godfather.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

@Component
public class PdfService extends ListenerAdapter {

    @Autowired
    private CoverService coverService;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("cover")) return;

        event.deferReply().queue();

        try {
            handlePdf(event);
        } catch (Exception ex) {
            event.getHook().sendMessage("❌ Failed to generate cover: " + ex.getMessage()).queue();
        }
    }

    private void handlePdf(SlashCommandInteractionEvent event) throws IOException, InterruptedException {
        String input = event.getOption("roll_no").getAsString().trim();

        String rollNo;
        if (input.startsWith("THA081BCT")) {
            rollNo = input;
        } else {
            rollNo = "THA081BCT" + String.format("%03d", Integer.parseInt(input));
        }

        byte[] pdf = coverService.getCoverPdf(rollNo);

        event.getHook()
                .sendMessage("📄 Cover page for **" + rollNo + "**")
                .addFiles(FileUpload.fromData(pdf, rollNo + "_cover.pdf"))
                .queue();
    }
}