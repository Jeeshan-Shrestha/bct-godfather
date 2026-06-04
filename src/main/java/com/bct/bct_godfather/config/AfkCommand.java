package com.bct.bct_godfather.config;

import java.awt.Color;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class AfkCommand extends ListenerAdapter {

    // Stores userId -> AfkEntry (reason + timestamp)
    private static final Map<String, AfkEntry> afkUsers = new HashMap<>();

    private static final String PREFIX = "?";

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;

        String content = event.getMessage().getContentRaw();
        String userId = event.getAuthor().getId();
        String mention = event.getAuthor().getAsMention();

        // Check if a mentioned user is AFK 
        for (var mentioned : event.getMessage().getMentions().getUsers()) {
            String mentionedId = mentioned.getId();
            if (afkUsers.containsKey(mentionedId)) {
                AfkEntry entry = afkUsers.get(mentionedId);
                long secondsAgo = (Instant.now().getEpochSecond() - entry.timestamp);
                String timeAgo = formatDuration(secondsAgo);

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(Color.CYAN)
                        .setDescription("💤 **" + mentioned.getName() + "** is AFK: *" + entry.reason + "*")
                        .setFooter("AFK since " + timeAgo + " ago");

                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
        }

        //Remove AFK if the user sends any message (not a ?afk command)
        if (!content.startsWith(PREFIX + "afk") && afkUsers.containsKey(userId)) {
            afkUsers.remove(userId);

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setDescription("✅ Welcome back, " + mention + "! Your AFK status has been removed.");

            event.getChannel().sendMessageEmbeds(embed.build()).queue();
            return;
        }

        // ── Handle ?afk [reason] command ─────────────────────────────────────
        if (content.equalsIgnoreCase(PREFIX + "afk") || content.toLowerCase().startsWith(PREFIX + "afk ")) {
            String reason = "AFK"; // default reason

            if (content.length() > (PREFIX + "afk").length()) {
                reason = content.substring((PREFIX + "afk").length()).trim();
                if (reason.isEmpty()) reason = "AFK";
            }

            afkUsers.put(userId, new AfkEntry(reason, Instant.now().getEpochSecond()));

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.YELLOW)
                    .setDescription("💤 " + mention + " is now AFK: *" + reason + "*")
                    .setFooter("I'll let others know when they mention you.");

            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }

    // Helper: format seconds into human-readable duration
    private String formatDuration(long seconds) {
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "m " + (seconds % 60) + "s";
        long hours = seconds / 3600;
        long mins = (seconds % 3600) / 60;
        return hours + "h " + mins + "m";
    }

    // Inner class to hold AFK data 
    private static class AfkEntry {
        String reason;
        long timestamp; // epoch seconds

        AfkEntry(String reason, long timestamp) {
            this.reason = reason;
            this.timestamp = timestamp;
        }
    }
}