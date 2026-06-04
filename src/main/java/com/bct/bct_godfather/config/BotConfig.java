package com.bct.bct_godfather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
    public JDA jda() throws Exception {

        JDA jda = JDABuilder.createDefault(token,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_VOICE_STATES
            )
            .addEventListeners(botEventListener, afkCommand, botHelpListener, spamPingDikesh)
            .build()
            .awaitReady();

        return jda;
    }


}
