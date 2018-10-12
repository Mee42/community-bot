package com.carson;

import sx.blah.discord.api.IDiscordClient;

public class Main {
    public static void main(String[] args)  {
        final IDiscordClient client = Utils.buildClient();
        Handler handler = new Handler(client);
        Utils.registerListener(client,handler);
        client.login();
    }
}
