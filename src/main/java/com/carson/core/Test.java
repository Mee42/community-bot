package com.carson.core;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public interface Test {
    boolean test(MessageReceivedEvent event);
    static Test startsWith(final String str){
        return (event) -> event.getMessage().getContent().startsWith(Main.PREFIX + str);
    }

}
