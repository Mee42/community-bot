package com.carson.commands;

import com.carson.core.Command;
import com.carson.core.CommandCollection;
import com.carson.core.Handler;
import com.carson.core.Test;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public class TestCommands extends CommandCollection {
    public TestCommands(){
        super("My - Name");
    }

    @Override
    public void genCommands(List<Command> commands, Handler handle) {
        commands.add(
            toCommand(Test.startsWith("ping"), (MessageReceivedEvent event) -> {
                handle.sendMessage(event,"pong");
            })
        );
    }
}
