package com.carson.commands;

import com.carson.core.Command;
import com.carson.core.CommandCollection;
import com.carson.core.Handler;
import com.carson.core.Test;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class AdminCommands extends CommandCollection {
    public AdminCommands(){
        super("Carson");
    }

    private static final long startTime = System.currentTimeMillis();


    @Override
    public void genCommands(List<Command> commands, Handler handler) {
        commands.add(toCommand(Test.startsWith("ping"), event -> handler.sendMessage(event,"pong")));


        commands.add(toCommand(Test.startsWith("status"), event -> {
            String uptime;
            try {
                uptime = "%" + uptime();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                uptime = "unknown";
            }

            List<IUser> users = new ArrayList<>();
            for(IGuild guild : event.getClient().getGuilds()){
                for(IUser user : guild.getUsers()){
                    if(!users.contains(user))
                        users.add(user);
                }
            }
            int userCount = users.size();
            handler.sendMessage(event, "uptime: `" +
                    (System.currentTimeMillis() - startTime)/1000 + "` Seconds, `" +
                    (System.currentTimeMillis() - startTime)/60_000/60 + "` Hours\n" +
                    "Guild Count: `" + event.getClient().getGuilds().size() + "`\n" +
                    "Guilds: `" + Arrays.toString(event.getClient().getGuilds().stream().map(IGuild::getName).toArray()) + "`\n" +
                    "Users in this Guild: `" + event.getGuild().getTotalMemberCount() + "`\n" +
                    "Users under this bot: `" + userCount + "`\n" +
                    "Server Uptime: `" + uptime + "`");
        }));


    }


    private static String uptime() throws IOException, InterruptedException {
//        ProcessBuilder pb = new ProcessBuilder("bash","-c", "up");
        File f = new File("/tmp/test" + UUID.randomUUID() + ".txt");
        ProcessBuilder pb = new ProcessBuilder("bash","-c","~/bin/up > " + f.getAbsolutePath());
        pb.inheritIO();
        final Process start = pb.start();
        start.waitFor();
        BufferedReader b = new BufferedReader(new FileReader(f));
        String line;
        StringBuilder in = new StringBuilder();
        while ((line = b.readLine()) != null) {
            in.append(line);
        }
        return in.toString();
    }


}
