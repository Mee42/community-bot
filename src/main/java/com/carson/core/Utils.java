package com.carson.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Utils {
    private static long timeStarted;
    static{
        timeStarted = System.currentTimeMillis() / 1000;
    }

    public static final Gson gson;
    static{
        gson = new GsonBuilder().setPrettyPrinting().create();
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
        StringBuilder token = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while(line != null) {
                token.append(line);
                line = br.readLine();
            }
        }catch(Exception e) {
            System.err.println("threw a " + e.getClass().getName() + " when trying to read from key");
            System.err.println("remember - the key needs to be in a key.txt file right next to the jar");
            e.printStackTrace();
            System.exit(-1);
        }
        token = new StringBuilder(token.toString().replace("\n", ""));//tokens don't have newlines, but some text editors leave on at the end
        return token.toString();
    }



}
