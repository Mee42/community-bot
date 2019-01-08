package com.carson.core

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

abstract class KotlinCommandCollection(author :String) : CommandCollection(author) {
    override fun genCommands(commands: MutableList<Command>, handle: Handler) {
        val coms = mutableMapOf<String,(MessageReceivedEvent) -> Unit>()
        genKotlinCommands(coms, handle)
        genWeirdCommands(commands, handle)
        coms.forEach {
            commands += toCommand(Test.startsWith(it.key), CommandLambda { e -> it.value(e) })
        }
    }

    abstract fun genWeirdCommands(commands: MutableList<Command>, handle: Handler)

    abstract fun genKotlinCommands(commands :MutableMap<String,(MessageReceivedEvent) -> Unit>, handle: Handler)
}