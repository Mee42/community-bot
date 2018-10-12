package com.carson.core;

import java.util.ArrayList;
import java.util.List;

public abstract class CommandCollection {

    public final String mainAuthor;

    public CommandCollection(String author) {
        this.mainAuthor = author;
    }

    private List<Command> commands = null;

    public List<Command> getCommands(Handler handler){
        if(commands == null){
            commands = new ArrayList<>();
            genCommands(commands,handler);
        }
        return commands;
    }
    public abstract void genCommands(List<Command> commands,Handler handle);



    protected Command toCommand(String author, Test test, CommandLambda lambda){
        return Command.from(author,this.getClass().getSimpleName(),test,lambda);
    }

    protected Command toCommand(Test test, CommandLambda lambda){
        return toCommand(mainAuthor,test,lambda);
    }

}
