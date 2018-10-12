package com.carson.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static long timeStarted;
    static{
        timeStarted = System.currentTimeMillis() / 1000;
    }

    private static Gson gson;
    static{
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public static String getGenericBotInfo(MessageReceivedEvent event){
        String str = "";
        long seconds = System.currentTimeMillis() / 1000 - timeStarted;
        double hours = (seconds / (60d*60d));
        str+="uptime:" + seconds + " seconds, " + hours + " hours\n";
        List<IGuild> guilds = event.getClient().getGuilds();
        str+="guilds:" + guilds.size() + "\n";
        List<IUser> users = new ArrayList<>();
        for(IGuild guild : guilds){
            for(IUser user : guild.getUsers()){
                if(!users.contains(user))users.add(user);
            }
        }
        str+="users:" + users.size() + "\n";
        return str;
    }


    public static void registerListener(IDiscordClient client, MessageHandler handler){
        client.getDispatcher().registerListener(handler);
    }

    public static IDiscordClient buildClient(File f){
        return new ClientBuilder()
                .withToken(readToken(f))
                .build();
    }
    public static IDiscordClient buildClient(String fileName){
        return buildClient(new File(fileName));
    }
    public static IDiscordClient buildClient(){
        return buildClient("key.txt");
    }
    public static String readToken(File file){
        String token = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while(line != null) {
                token+=line;
                line = br.readLine();
            }
        }catch(Exception e) {
            System.err.println("threw a " + e.getClass().getName() + " when trying to read from key");
            System.err.println("remember - the key needs to be in a key.txt file right next to the jar");
            e.printStackTrace();
            System.exit(-1);
        }
        token = token.replace("\n","");//tokens don't have newlines, but some text editors leave on at the end
        return token;
    }



    public static Gson getGson() {
        return gson;
    }
}
