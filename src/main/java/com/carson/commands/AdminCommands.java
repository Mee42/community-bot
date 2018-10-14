package com.carson.commands;

import com.carson.core.Command;
import com.carson.core.CommandCollection;
import com.carson.core.Handler;
import com.carson.core.Test;

import java.util.List;

public class AdminCommands extends CommandCollection {
    public AdminCommands(){
        super("Carson");
    }

    private static final long startTime = System.currentTimeMillis();


    @Override
    public void genCommands(List<Command> commands, Handler handler) {
        commands.add(toCommand(Test.startsWith("ping"), event -> handler.sendMessage(event,"pong")));
        commands.add(toCommand(Test.startsWith("status"), event -> handler.sendMessage(event, "uptime:" + (System.currentTimeMillis() - startTime)/1000 + " Seconds, " +
                (System.currentTimeMillis() - startTime)/60000 + " Hours\n" +
                "Guilds:" + event.getClient().getGuilds().size() + "\n" +
                "Users in this Guild" + event.getGuild().getTotalMemberCount())));
    }
}
