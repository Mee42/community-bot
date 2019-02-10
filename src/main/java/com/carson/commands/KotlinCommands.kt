package com.carson.commands

import com.carson.core.*
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer


class KotlinCommands : CommandCollection("Carson") {
    override fun genCommands(commands: MutableList<Command>, handle: Handler) {

        commands.add(toCommand(Test.startsWith("invite"), CommandLambda { handle.sendMessage(it,"https://discordapp.com/oauth2/authorize?client_id=500780039030308875&scope=bot") }))

        //mentions
        commands.add(toCommand(Test {
            val arr = it.message.mentions
            if(arr.size != 1)
                return@Test false
            if(!it.message.content.matches(Regex("<@[0-9!~]*>")))
                return@Test false
            return@Test arr[0] == it.client.ourUser
        }, CommandLambda {
            handle.sendMessage(it, "Hi, I'm <@500780039030308875>. I'm written mainly in kotlin, and run by" +
                    " <@293853365891235841>. \n" +
                    "use ~help for commands available")
        }))


        commands.add(toCommand(Test.startsWith("help"), CommandLambda {
            val message = it.message.content
            if(message.split(" ").size == 1){
                val embed = HELP.fold(EmbedBuilder()) {all,one -> all.appendField(one.name + " " +
                        one.args.fold("") {all2,one2 -> "$all2 ${if(one2.type == Type.SET) one2.raw.replaceFirst("${one2.name}:","") else one2.raw}"}
                ,one.short,false)}
                RequestBuffer.request { it.channel.sendMessage(embed.withTitle("Help menu").build()) }
            }else{
                val command = message.split(" ")[1]
                val entry = HELP.find { entry -> entry.name == command }
                if(entry == null){
                    handle.sendMessage(it,"I could not find that command")
                    return@CommandLambda
                }

                val folded = entry.args.fold("") {all,one->
                    if(one.type == Type.SET){

                    }
                    return@fold "$all\n" + "`" + one.name + "` :  " + if(one.type == Type.OPTIONAL) "Optional" else "Required"
                }.replaceFirst("\n","")

                var long = entry.long
                for(arg in entry.args){
                    long = long.replace(arg.raw,"`${arg.raw}`")
                }

                var argsStr = "\n$folded\n\n"
                if(folded.isEmpty())
                    argsStr = "\n"

                val str = "${entry.name}:$argsStr$long"
                handle.sendMessage(it,str)
            }
        }))

    }

}