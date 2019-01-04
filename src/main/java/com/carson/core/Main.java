package com.carson.core;

import com.carson.commands.AdminCommands;
import com.carson.commands.ChainCommands;
import com.carson.commands.KotlinCommands;
import sx.blah.discord.api.IDiscordClient;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static final String PREFIX = "!";
    public static void main(String[] args)  {
        final IDiscordClient client = Utils.buildClient();
        Handler handler = new Handler(client);
        Utils.registerListener(client,handler);
        client.login();
    }


    public static final List<CommandCollection> collectionList = new ArrayList<>();
    static{
        collectionList.add(new AdminCommands());
        collectionList.add(new KotlinCommands());
        collectionList.add(new ChainCommands());
    }
}
