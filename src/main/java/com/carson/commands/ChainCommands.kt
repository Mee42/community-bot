package com.carson.commands

import com.carson.core.Command
import com.carson.core.Handler
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.lang.NumberFormatException
import java.lang.RuntimeException
import kotlin.concurrent.thread


const val END   = "!END!"
const val START = "!START!"

enum class Context(name: String) {
    USER("author"),
    GUILD("guild"),
    GLOBAL("global"),
    CHANNEL("channel"),
    UNKNOWN("unknown");
    val databaseName :String = name
}

class ChainCommands : KotlinCommandCollection("Carson") {
    init{
        //init connection to the database on another thread
        thread { println("message counts:" + getMessageDB().countDocuments()) }
    }
    override fun genWeirdCommands(commands: MutableList<Command>, handle: Handler) {}


    override fun genKotlinCommands(commands: MutableMap<String, (MessageReceivedEvent) -> Unit>, handle: Handler) {
        //note: prefixes are automatically applied

        commands["chain"] = command@ {event ->
            val content = event.message.content
            val argument :String? =if(content == "!chain") null else content.replace("!chain","").trim()


            var queryType: Context?
            var queryData :Long? = null
            when{
                argument == null -> {
                    handle.sendMessage(event, "*help menu*")
                    return@command
                }
                event.message.mentions.size == 1 -> {
                    //run with mention
                    queryType = Context.USER
                    queryData = event.message.mentions[0].longID
                }
                event.message.mentions.size > 1 -> {
                    handle.sendMessage(event, "I can't use multiple users at once, sorry")
                    return@command
                }
                event.message.channelMentions.size == 1 -> {
                    queryType = Context.CHANNEL
                    queryData = event.message.channelMentions[0].longID
                }
                event.message.channelMentions.size > 1 -> {
                    handle.sendMessage(event, "I can't use multiple channels at once, sorry")
                    return@command
                }
                argument.toUpperCase() == "GLOBAL" -> {
                    queryType = Context.GLOBAL
                }
                argument.toUpperCase() == "GUILD" || argument.toUpperCase() == "SERVER" -> {
                    queryType = Context.GUILD
                    queryData = event.guild.longID
                }
                event.message.mentionsHere() -> {
                    handle.sendMessage(event, "I can't use `@here` for this")
                    return@command
                }
                else -> {
                    try{
                        queryData = argument.toLong()
                        queryType = Context.UNKNOWN
                    }catch(e :NumberFormatException){
                        handle.sendMessage(event,"I can't understand that argument :cry:")
                        return@command
                    }
                }
            }
            //attempt to find the right chain. if it doesn't exist, push it to the stack and start working on it in the background
            val chain = when (queryType) {
                Context.GUILD  -> ChainCache guild queryData!!
                Context.CHANNEL -> ChainCache channel queryData!!
                Context.USER -> ChainCache author queryData!!
                Context.GLOBAL -> ChainCache.global ?: run {
                    handle.sendMessage(event, "The global chain has not finished initial processing, check back later in a few minutes")
                    return@command
                }
                Context.UNKNOWN -> {
                    val chain = ChainCache guild queryData!! ?: ChainCache channel queryData ?: ChainCache author queryData
                    if(chain != null) chain
                    else { when {
                        event.guild.getUserByID(queryData) != null -> queryType = Context.USER
                        event.client.guilds.any { it.longID == queryData } -> queryType = Context.GUILD
                        event.guild.channels.any { it.longID == queryData } -> queryType = Context.CHANNEL
                    }; null }
                }
            } ?: if(queryType == Context.UNKNOWN){//if chain is null
                    //if it wasn't identified
                    handle.sendMessage(event,"I can't find that for some reason. Please contact the developer via dm here: <@293853365891235841>")
                    return@command
                }else{
                    //push the chain
                    ChainStack.push(queryType,queryData!!)//eehehehhehh. Writing this is painful.
                    handle.sendMessageAndGet(event.channel, "I'm putting that to the queue for processing. Once it's processed, new messages will be added to it dynamically.\n" +
                            "This should take anywhere from seconds to a minute or two for big servers. Keep in mind, collection will only include messages since 2019-1-5.\n" +
                            "I will automatically send it if the chain completes within 10 seconds")
                    ChainStack.singleton.popChain()
                    thread thread@{
                        val time = System.currentTimeMillis()
                        var chain :Chain? = null
                        while(time + 10_000 > System.currentTimeMillis()) {
                            chain = when (queryType) {
                                Context.GUILD  -> ChainCache guild queryData
                                Context.CHANNEL -> ChainCache channel queryData
                                Context.USER -> ChainCache author queryData
                                Context.GLOBAL -> ChainCache.global
                                else -> return@thread }
                            if(chain == null)
                                Thread.sleep(100)
                            else
                                break
                        }
                        handle.sendMessage(event,"Here's your phrase: ```\n${chain!!.generateSentance()}```")
                    }
                    return@command
                }
            handle.sendMessage(event,"Here's your phrase: ```\n${chain.generateSentance()}```")

        }

    }

}

val client :MongoClient = MongoClient("192.168.1.203:27017")
val db :MongoDatabase = client.getDatabase("carson-bot")
fun getMessageDB() :MongoCollection<Document>{
    return db.getCollection("messages")
}

class ChainCache{companion object{
    private val guilds = mutableMapOf<Long,Chain>()
    infix fun guild(long :Long) :Chain?{
        return guilds[long]
    }
    fun guild(id :Long,chain :Chain){
        guilds[id]=chain
    }

    private val authors = mutableMapOf<Long,Chain>()
    infix fun author(long :Long) :Chain?{
        return authors[long]
    }
    fun author(id :Long,chain :Chain){
        authors[id]=chain
    }

    private val channels = mutableMapOf<Long,Chain>()
    infix fun channel(long :Long) :Chain?{
        return channels[long]
    }
    fun channel(id :Long,chain :Chain){
        channels[id]=chain
    }

    var global :Chain? = null

}}


