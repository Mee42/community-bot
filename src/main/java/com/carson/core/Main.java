package com.carson.core;

import com.carson.commands.*;
import sx.blah.discord.api.IDiscordClient;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static final String PREFIX = "!";
    public static void main(String[] args)  {
        LazyWordChain.staticStartProcessing();
        final IDiscordClient client = Utils.buildClient();
        Handler handler = new Handler(client);
        Utils.registerListener(client,handler);
        client.getDispatcher().registerListener(new EmojiHandler());
        client.login();
    }


    public static final List<CommandCollection> collectionList = new ArrayList<>();
    static{
        collectionList.add(new AdminCommands());
        collectionList.add(new KotlinCommands());
        collectionList.add(new ChainCommands());
        collectionList.add(new WordCommands());
    }
}
