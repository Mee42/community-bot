package com.carson.core

import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.message.MessageCreateEvent
import java.io.File

fun main(args: Array<String>) {
    val client = DiscordClientBuilder(File("key.txt").readText()).build()
    client.eventDispatcher
            .on(MessageCreateEvent::class.java)
            .subscribe {it.message.content.ifPresent { println(it) } }
    client.eventDispatcher
            .on(MessageCreateEvent::class.java)
            .subscribe { it.message.author.blockOptional() { println("author:$it") }}
    client.login().block()
}