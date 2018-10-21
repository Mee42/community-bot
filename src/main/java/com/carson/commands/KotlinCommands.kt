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

//        commands.add(toCommand(Test {
//            var arr = it.message.mentions
//            if(arr.size != 0)
//                return@Test
//            return arr.get(0).
//        }, CommandLambda {

//        }))
    }
    companion object {
        var emojis =  mutableMapOf<String,String>()
        val arr = "abcdefghijklmnopqrstuvwxyz".toCharArray()
        init {
            for((index, c) in ('\uDDE6'..'\uDDFF').withIndex())
                emojis["${arr[index]}"] = "\uD83C$c"
        }
    }
}