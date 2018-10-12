package com.carson.core;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public abstract class Command {
    private final String author;
    private final String collection;



    private final Test test;

    public Command(String author, String collection, Test test) {
        this.author = author;
        this.collection = collection;
        this.test = test;
    }

    abstract void run(MessageReceivedEvent event);

    public static Command from(String author, String collection,Test test, CommandLambda lambda){
        return new Command(author, collection, test) {
            @Override
            void run(MessageReceivedEvent event) {
                lambda.run(event);
            }
        };
    }

    public boolean test(MessageReceivedEvent event){
        return test.test(event);
    }
}
