package com.carson.commands;

import com.carson.core.Command;
import com.carson.core.CommandCollection;
import com.carson.core.Handler;
import com.carson.core.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

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
            handler.sendMessage(event, "uptime: `" +
                    (System.currentTimeMillis() - startTime)/1000 + "` Seconds, `" +
                    (System.currentTimeMillis() - startTime)/60000 + "` Hours\n" +
                    "Guilds: `" + event.getClient().getGuilds().size() + "`\n" +
                    "Users in this Guild: `" + event.getGuild().getTotalMemberCount() + "`\n" +
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
            System.out.println(line);
            in.append(line);
        }
        System.out.println("STRING:" + in.toString());
        return in.toString();
    }


}
