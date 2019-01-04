package com.carson.core;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Handler extends MessageHandler {
    public Handler(IDiscordClient client) {
        super(client);
        init();
    }

    @Override
    public void onMessage(MessageReceivedEvent event) {
        Executors.newSingleThreadExecutor().execute(() -> {
            log(event);
            for (Command command : commands) {
//                System.out.println("Testing command (" + command.toString() + ")");
                if (command.test(event)) {
                    System.out.println("running command (" + command.toString() + ")");
                    command.run(event);
                }
            }
        });
    }

    private List<Command> commands = new ArrayList<>();

    void init(){
        for(CommandCollection collection : Main.collectionList){
            int size = commands.size();
            commands.addAll(collection.getCommands(this));
        }
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
