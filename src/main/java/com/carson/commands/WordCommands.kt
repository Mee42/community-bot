package com.carson.commands

import com.carson.core.Command
import com.carson.core.Handler
import com.carson.core.KotlinCommandCollection
import com.mongodb.client.model.Filters
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.io.BufferedReader
import java.io.FileReader
import java.lang.StringBuilder
import java.util.*
import kotlin.concurrent.thread

class WordCommands : KotlinCommandCollection("Carson") {
    override fun genWeirdCommands(commands: MutableList<Command>, handle: Handler) {}

    override fun genKotlinCommands(commands: MutableMap<String, (MessageReceivedEvent) -> Unit>, handle: Handler) {
        commands["word"] = command@{ event ->
            if (chain.chain == null) {
                handle.sendMessage(event, "I need to finish processing before I can generate a word")
                return@command
            }
            handle.sendMessage(event, "Word:`${chain.chain!!.generate()}`")
        }
    }

}
val chain = LazyWordChain()
class WordChain(private val map: Map<String, List<String>>) {
    fun generate():String{
        val list = mutableListOf<String>()
        list+=START
        while(list.last() != END){
            val random = (Math.random() * map[list.last()]!!.size).toInt()
            list+=map[list.last()]!![random]
        }
        return list.filter { it != START && it != END }.fold(StringBuilder()) { q, w->q.append(w)}.toString()
    }
}

class LazyWordChain {
    var chain: WordChain? = null

    init {
        val map = mutableMapOf<String, MutableList<String>>()
        thread {
            //push processing to a seperate thread
            val reader = BufferedReader(FileReader("./words.txt"))
            while (true) {
                val str = reader.readLine() ?: break
                if (str.isBlank()) continue
                var bad = false
                "()':/.".forEach {
                    if(str.contains(it))
                        bad = true
                }
                if(bad) continue
                val arr = Arrays.asList(START, *str.toCharArray().map { it.toString() }.toTypedArray(), END)
                for (i in 0 until arr.size - 1) {
                    if (map[arr[i]] == null)
                        map[arr[i]] = mutableListOf()
                    map[arr[i]]!! += arr[i+1]
                }
            }
            println("Done processing")
            chain = WordChain(map)
        }
    }

    companion object {
        @JvmStatic
        fun staticStartProcessing() {
            chain
        }
    }
}
