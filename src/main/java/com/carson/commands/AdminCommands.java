package com.carson.commands;

import com.carson.core.Command;
import com.carson.core.CommandCollection;
import com.carson.core.Handler;
import com.carson.core.Test;

import java.util.List;

public class AdminCommands extends CommandCollection {
    public AdminCommands(){
        super("Carson - AdminCommands");
    }




    @Override
    public void genCommands(List<Command> commands, Handler handler) {
        commands.add(toCommand(Test.startsWith("ping"), event -> handler.sendMessage(event,"pong")));
    }
}
