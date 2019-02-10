package com.carson.commands

import com.carson.core.Command
import com.carson.core.Handler
import com.carson.core.KotlinCommandCollection
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

class TempCommands :KotlinCommandCollection("Carson") {
    override fun genWeirdCommands(commands: MutableList<Command>, handle: Handler) {}

    override fun genKotlinCommands(commands: MutableMap<String, (MessageReceivedEvent) -> Unit>, handle: Handler) {

    }
}