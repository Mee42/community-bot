package com.carson.core

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.all
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import java.io.File

//constants
const val PREFIX = "!"

fun main(args: Array<String>) {
    val client = DiscordClientBuilder(File("key.txt").readText()).build()
    client.eventDispatcher
            .on(MessageCreateEvent::class.java)
            .subscribe {

            }

    client.login().block()
}

data class Message(
        val message :MessageCreateEvent,
        val prefix :Mono<String> = Mono.fromCallable<String>  { mongo.getGuildDataCollection().find(all("_id",message.message.id.asLong())).firstOrNull()?.getString("prefix") ?: PREFIX } )
}

class Command(val test: (Message) -> Boolean,
              val execute: (Message) -> Boolean)


val mongo = Mongo()
class Mongo(private val mClient :MongoClient = MongoClient("192.168.1.203:27017"), private val db : MongoDatabase = mClient.getDatabase("carson-bot")){
    fun getMessageCollection() = db.getCollection("messages")
    fun getBotMessagesCollection() = db.getCollection("bot-messages")
    fun getGuildDataCollection() = db.getCollection("guild-data")
    fun getUserDataCollection() = db.getCollection("user-data")
}