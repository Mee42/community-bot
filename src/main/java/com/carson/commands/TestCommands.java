package com.carson.commands;

import com.carson.core.Command;
import com.carson.core.CommandCollection;
import com.carson.core.Handler;

import java.util.List;

public class TestCommands extends CommandCollection {
    public TestCommands(){
        super("My - name");
    }

    @Override
    public void genCommands(List<Command> commands, Handler handle) {

    }
}
