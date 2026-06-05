package com.bct.bct_godfather.manga;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

import com.bct.bct_godfather.service.MangaDexService;
import com.fasterxml.jackson.databind.JsonNode;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

@Component
public class MangaCommand extends ListenerAdapter {

    private final MangaDexService mangaDexService;

    public MangaCommand(MangaDexService mangaDexService) {
        this.mangaDexService = mangaDexService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "manga"    -> handleSearch(event);
            case "chapters" -> handleChapters(event);
            case "read"     -> handleRead(event);
        }
    }

    private void handleSearch(SlashCommandInteractionEvent event) {
        String title = event.getOption("search").getAsString();
        event.deferReply().queue();

        try {
            EmbedBuilder embed = mangaDexService.buildSearchEmbed(title);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        } catch (Exception e) {
            event.getHook().sendMessage("❌ Error: " + e.getMessage()).queue();
        }
    }

    private void handleChapters(SlashCommandInteractionEvent event) {
        String mangaId = event.getOption("manga_id").getAsString();
        event.deferReply().queue();

        try {
            EmbedBuilder embed = mangaDexService.buildChaptersEmbed(mangaId);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        } catch (Exception e) {
            event.getHook().sendMessage("❌ Error: " + e.getMessage()).queue();
        }
    }

    private void handleRead(SlashCommandInteractionEvent event) {
        String chapterId = event.getOption("chapter_id").getAsString();
        event.deferReply().queue();

        CompletableFuture.runAsync(() -> {
            try {
                JsonNode chapterInfo = mangaDexService.getChapterInfo(chapterId);
                JsonNode attrs = chapterInfo.get("data").get("attributes");
                JsonNode externalUrl = attrs.get("externalUrl");

                if (externalUrl != null && !externalUrl.isNull()) {
                    event.getHook().sendMessage(
                        "⚠️ This chapter is hosted externally.\n🔗 " + externalUrl.asText()
                    ).queue();
                    return;
                }

                String chNum = attrs.get("chapter").isNull() ? "unknown" : attrs.get("chapter").asText();
                byte[] pdf = mangaDexService.buildChapterPdf(chapterId);

                long sizeKb = pdf.length / 1024;
                if (sizeKb > 8192) {
                    event.getHook().sendMessage(
                        "❌ PDF is too large (" + sizeKb + " KB). Discord limit is 8MB."
                    ).queue();
                    return;
                }

                event.getHook()
                    .sendMessage("📄 Chapter " + chNum + " — " + sizeKb + " KB")
                    .addFiles(FileUpload.fromData(pdf, "chapter_" + chNum + ".pdf"))
                    .queue();

            } catch (Exception e) {
                event.getHook().sendMessage("❌ Error: " + e.getMessage()).queue();
            }
        });
    }
}