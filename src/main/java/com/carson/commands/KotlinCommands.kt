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
                if (unicode == null)
                    println("Emoji $key is null $unicode")
                else
                    println("unicode value for key $key is $unicode")

                RequestBuffer.request { messageSent.addReaction(ReactionEmoji.of(unicode)) }.get()
            }

            }))
    }
    companion object {
        var emojis = HashMap<String, String>()
        init {
            val map = HashMap<String, String>()
            map["a"] = "\uD83C\uDDE6"
            map["b"] = "\uD83C\uDDE7"
            map["c"] = "\uD83C\uDDE8"
            map["d"] = "\uD83C\uDDE9"
            map["e"] = "\uD83C\uDDEA"
            map["f"] = "\uD83C\uDDEB"
            map["g"] = "\uD83C\uDDEC"
            map["h"] = "\uD83C\uDDED"
            map["i"] = "\uD83C\uDDEE"
            map["j"] = "\uD83C\uDDEF"
            map["k"] = "\uD83C\uDDF0"
            map["l"] = "\uD83C\uDDF1"
            map["m"] = "\uD83C\uDDf2"
            map["n"] = "\uD83C\uDDF3"
            map["o"] = "\uD83C\uDDF4"
            map["p"] = "\uD83C\uDDF5"
            map["q"] = "\uD83C\uDDF6"
            map["r"] = "\uD83C\uDDF7"
            map["s"] = "\uD83C\uDDF8"
            map["t"] = "\uD83C\uDDF9"
            map["u"] = "\uD83C\uDDFA"
            map["v"] = "\uD83C\uDDFB"
            map["w"] = "\uD83C\uDDFC"
            map["x"] = "\uD83C\uDDFD"
            map["y"] = "\uD83C\uDDFE"
            map["z"] = "\uD83C\uDDFF"
            for((key,value) in map){
                emojis[":regional_indicator_$key:"] = value
            }
        }
    }
}