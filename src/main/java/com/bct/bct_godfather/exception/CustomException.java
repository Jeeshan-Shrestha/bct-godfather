package com.bct.bct_godfather.exception;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CustomException extends RuntimeException{

    public CustomException(String message){
        super(message);
    }
    
}
