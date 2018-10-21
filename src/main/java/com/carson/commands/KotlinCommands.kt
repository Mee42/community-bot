package com.carson.commands

import com.carson.core.*
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.handle.impl.obj.ReactionEmoji


class KotlinCommands() : CommandCollection("Carson") {
    override fun genCommands(commands: MutableList<Command>?, handle: Handler?) {
        if(commands == null || handle == null)
            return
        commands.add(toCommand(Test.startsWith("poll"), CommandLambda {
            var content = it.message.content ?: return@CommandLambda
            content = content.replaceFirst(Main.PREFIX + "poll", "")
            if(content == "help" || content == ""){
                handle.sendMessage(it,"usage: !poll question & answer-1 | answer-2 | ect")
                return@CommandLambda
            }

            var split = content.split("|").toMutableList()
            val question :String
            if(split[0].split("&").size == 1){
                question = ""
            }else {
                question = split[0].split("&")[0]
                split[0] = split[0].split("&")[1]
            }
            if(split.size == 0 || split.size == 1){
                handle.sendMessage(it, "not enough answers")
                return@CommandLambda
            }

            if (split.size > emojis.size) {
                handle.sendMessage(it, "To many options")
                return@CommandLambda
            }
            var index = 0
            val map = split.associate { Pair((emojis["${arr[index++]}"]), it) }//generate an array
            var message = "$question\nOptions:\n"
            for ((key, value) in map) {
                message += "$key : $value\n"
            }
            val messageSent = handle.sendMessageAndGet(it.channel, message)
            for (key in map.keys)
                RequestBuffer.request { messageSent.addReaction(ReactionEmoji.of(key))}.get()

            }))

        commands.add(toCommand(Test.startsWith("invite"), CommandLambda { handle.sendMessage(it,"https://discordapp.com/oauth2/authorize?client_id=500780039030308875&scope=bot") }))

        commands.add(toCommand(Test {
            var arr = it.message.mentions
            if(arr.size != 1)
                return@Test false
            return@Test arr[0] == it.client.ourUser
        }, CommandLambda {
            handle.sendMessage(it, "Hi, I'm <@500780039030308875>, a bot made to give people the ability to write" +
                    " Discord bots without having to host it or manage boilerplate. I'm written in java and mainly run by" +
                    " <@293853365891235841>, and contributing is really easy. Use !contribute to find out how!\n" +
                    "Commands avalible are:```\ninvite\npoll\nping\nstatus\ncontribute\n```")
        }))

        commands.add(toCommand(Test.startsWith("contribute"), CommandLambda {
            handle.sendMessage(it, "You can find the guild to start contributing here:" +
                    "https://github.com/Mee42/community-bot/wiki\n" +
                    "You can write both Java and Pesudo code, allowing everyone to contribute.")
        }))
    }
    companion object {
        val arr = "abcdefghijklmnopqrstufvwxyz".toCharArray()
        val emojis =  arr.mapIndexed {  index, c -> "$c" to "\uD83C${'\uDDE6' + index}"}.toMap()
    }
}