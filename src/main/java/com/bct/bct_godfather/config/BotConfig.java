package com.bct.bct_godfather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bct.bct_godfather.manga.MangaCommand;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Configuration
public class BotConfig {

    @org.springframework.beans.factory.annotation.Value("${discord.bot.token}")
    private String token;   

    private final BotEventListener botEventListener;
    private final AfkCommand afkCommand;
    private final BotHelpListener botHelpListener;
    private final SpamPingDikesh spamPingDikesh;

    public BotConfig(BotEventListener botEventListener, AfkCommand afkCommand, BotHelpListener botHelpListener, SpamPingDikesh spamPingDikesh) {
        this.botEventListener = botEventListener;
        this.afkCommand = afkCommand;
        this.botHelpListener = botHelpListener;
        this.spamPingDikesh = spamPingDikesh;
    }
    
    @Bean
    public JDA jda(MangaCommand mangaCommand) throws Exception {

        JDA jda = JDABuilder.createDefault(token,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_VOICE_STATES
            )
            .addEventListeners(mangaCommand, botEventListener, afkCommand, botHelpListener, spamPingDikesh)
            .build()
            .awaitReady();

        jda.getGuildById("1510692666235556033")
   .updateCommands().addCommands(
       Commands.slash("manga", "Search for manga on MangaDex")
           .addOption(OptionType.STRING, "search", "Manga title", true),
       Commands.slash("chapters", "Get latest chapters for a manga")
           .addOption(OptionType.STRING, "manga_id", "MangaDex manga UUID", true),
       Commands.slash("read", "Send the first page of a chapter")
           .addOption(OptionType.STRING, "chapter_id", "MangaDex chapter UUID", true)
   ).queue();

        return jda;
    }


}
