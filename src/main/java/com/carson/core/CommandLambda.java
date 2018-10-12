package com.carson.core;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public interface CommandLambda {
    void run(MessageReceivedEvent event);
}
