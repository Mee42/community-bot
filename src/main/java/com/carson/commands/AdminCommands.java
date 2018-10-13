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




    @Override
    public void genCommands(List<Command> commands, Handler handler) {
        commands.add(toCommand(Test.startsWith("ping"), event -> handler.sendMessage(event,"pong")));
        commands.add(toCommand(event -> {
            if(event.getAuthor().getLongID() == 293853365891235841L){
                return false;
            }
            return Test.startsWith("shutdown").test(event);
        }, event -> System.exit(0)));
    }
}
