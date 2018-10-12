package com.carson;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Handler extends MessageHandler {
    public Handler(IDiscordClient client) {
        super(client);
    }

    @Override
    public void onMessage(MessageReceivedEvent event) {
        log(event);
    }




    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private String log(MessageReceivedEvent event) {
        String str = event.getAuthor() != null?event.getAuthor().getName():"NULL";
        str+=":";
        str+= (!(event.getMessage() == null || event.getMessage().getContent() == null)) ? event.getMessage().getContent() : "NULL";
        str+=" ";
        String channel = "(" + (event.getChannel() != null?event.getChannel().getName():"NULL") + " - " +
                (event.getGuild() != null?event.getGuild().getName():"NULL") + ")";
        str+=channel;
        if(event.getMessage() != null && event.getMessage().getTimestamp() != null)
            str+= format.format(new Date(event.getMessage().getTimestamp().toEpochMilli()));
        System.out.println(str);
        return str;
    }


}
