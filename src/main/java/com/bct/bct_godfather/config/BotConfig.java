package com.bct.bct_godfather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.bct.bct_godfather.homework.HomeworkCommand;
import com.bct.bct_godfather.manga.MangaCommand;
import com.bct.student.StudentEventListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Configuration
public class BotConfig {

    @org.springframework.beans.factory.annotation.Value("${discord.bot.token}")
    private String token;   

    private final PdfService pdfService;

    private final BotEventListener botEventListener;
    private final AfkCommand afkCommand;
    private final BotHelpListener botHelpListener;
    private final SpamPingDikesh spamPingDikesh;
    private final PdfBotListener pdfBotListener;
    private final HomeworkCommand homeworkCommand;
    private final StudentEventListener studentEventListener;

    public BotConfig(PdfBotListener pdfBotListener,
        BotEventListener botEventListener, 
        AfkCommand afkCommand, 
        BotHelpListener botHelpListener, 
        SpamPingDikesh spamPingDikesh,
        PdfService pdfService,
        HomeworkCommand homeworkCommand,
        StudentEventListener studentEventListener) {
        this.botEventListener = botEventListener;
        this.afkCommand = afkCommand;
        this.botHelpListener = botHelpListener;
        this.spamPingDikesh = spamPingDikesh;
        this.pdfService = pdfService;
        this.pdfBotListener = pdfBotListener;
        this.homeworkCommand = homeworkCommand;
        this.studentEventListener = studentEventListener;
    }
    
    @Bean
    public JDA jda(MangaCommand mangaCommand,
                DocxConvertListener docxConvertListener) throws Exception {

        JDA jda = JDABuilder.createDefault(token,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_VOICE_STATES
            )
            .addEventListeners(studentEventListener,homeworkCommand,docxConvertListener,pdfBotListener,pdfService,mangaCommand, botEventListener, afkCommand, botHelpListener, spamPingDikesh)
            .build()
            .awaitReady();

        jda
   .updateCommands()
   .addCommands(
       Commands.slash("student", "get the details about the students")
           .addOption(OptionType.STRING, "roll_no", "THA081BCT0XX",true),
       Commands.slash("manga", "Search for manga on MangaDex")
           .addOption(OptionType.STRING, "search", "Manga title", true),
       Commands.slash("chapters", "Get latest chapters for a manga")
           .addOption(OptionType.STRING, "manga_id", "MangaDex manga UUID", true),
       Commands.slash("read", "Send the first page of a chapter")
           .addOption(OptionType.STRING, "chapter_id", "MangaDex chapter UUID", true),
        Commands.slash("cover", "get the cover page for DSA with your roll number")
            .addOption(OptionType.STRING, "roll_no","desc",true),
        Commands.slash("pdf-to-docx", "convert pdf file to docx")
            .addOption(OptionType.ATTACHMENT, "file", "The PDF file", true),
        Commands.slash("pptx-to-pdf", "convert pptx file to pdf")
            .addOption(OptionType.ATTACHMENT, "file", "The pptx file", true),
        DocxConvertListener.getCommandData(),
        Commands.slash("homework", "Set a homework reminder")
            .addOption(OptionType.STRING, "subject", "Subject or task name", true)
            .addOption(OptionType.STRING, "description", "Homework description", true) 
            .addOption(OptionType.INTEGER, "days", "Days until deadline", true)
            .addOption(OptionType.STRING, "time", "Time of deadline (HH:mm)", true),
        Commands.slash("get-homework", "Get all the homework assigned")
   ).queue();

        return jda;
    }


}
