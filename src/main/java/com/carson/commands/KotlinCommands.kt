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

            val arr = "abcdefghijklmnopqrstuvwxyz".toCharArray()
            if (split.size > arr.size) {
                handle.sendMessage(it, "To many options")
                return@CommandLambda
            }

            var index = 0
            val map = split.associate { Pair(":regional_indicator_${arr[index++]}:", it) }
            var message = "$question\nOptions:\n"
            for ((key, value) in map) {
                message += "$key : $value\n"
            }
            val messageSent = handle.sendMessageAndGet(it.channel, message)
            for (key in map.keys) {
                val unicode = emojis[key]
                RequestBuffer.request { messageSent.addReaction(ReactionEmoji.of(unicode)) }.get()
            }

            }))
    }
    companion object {
        var emojis = HashMap<String, String>()
        init {
            val arr = "abcdefghijklmnopqrstuvwxyz".toCharArray()
            for((index, c) in ('\uDDE6'..'\uDDFF').withIndex())
                emojis[":regional_indicator_${arr[index]}:"] = "\uD83C$c"
        }
    }
}