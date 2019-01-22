package com.carson.commands

import com.carson.core.Command
import com.carson.core.Handler
import com.carson.core.KotlinCommandCollection
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer

class TempCommands :KotlinCommandCollection("Carson") {
    override fun genWeirdCommands(commands: MutableList<Command>, handle: Handler) {}

    override fun genKotlinCommands(commands: MutableMap<String, (MessageReceivedEvent) -> Unit>, handle: Handler) {
        commands["asdf"] = {event ->
            val content = event.message.content.replace("!asdf","").trim()
            val words = content.split(" ")
            var string = ""
            var back = START
            var i = 0

            val mutable = words.toMutableList()
            mutable.add(END)
            val totalChanceList = mutableListOf<Double>()
            mutable.forEach{word ->
                val map = ChainCache.global?.getMap()
                if(map == null){
                    RequestBuffer.request { event.channel.sendMessage("Impossible to generate!") }
                    return@forEach
                }
                val list = map[back]!!
                val chance = list.count { word == it }.toDouble().times(100) / list.size.toDouble()
                totalChanceList+=chance
                string+= "word `$word`(${i++}) has a `$chance%` of coming after `$back`\n"
                back = word
            }
            val total = totalChanceList.fold(1.0) {all,one ->one * all}
            string+="Total chance: $total"
            string+=""
            RequestBuffer.request { event.channel.sendMessage(string) }
        }
    }
}