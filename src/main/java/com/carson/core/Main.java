package com.carson.core;

import com.carson.commands.AdminCommands;
import sx.blah.discord.api.IDiscordClient;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static final String PREFIX = "@";
    public static void main(String[] args)  {
        final IDiscordClient client = Utils.buildClient();
        Handler handler = new Handler(client);
        Utils.registerListener(client,handler);
        initCommmands(handler);
        client.login();
    }

    private static void initCommmands(Handler handler) {
//        test(Command.from(Test.startsWith("ping"),event -> handler.sendMessage(event,"pong"));
    }

    public static final List<CommandCollection> collectionList = new ArrayList<>();
    static{
        collectionList.add(new AdminCommands());
    }
}
