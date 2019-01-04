package com.carson.commands

import com.carson.core.Command
import com.carson.core.Handler
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.lang.NumberFormatException


const val END   = "!END!"
const val START = "!START!"

class ChainCommands : KotlinCommandCollection("Carson") {
    init{
        //init connection to the database
        Thread { println("message counts:" + getMessageDB().countDocuments()) }.start()
    }
    override fun genWeirdCommands(commands: MutableList<Command>, handle: Handler) {
        commands+=toCommand({ true }, {
            val coll = getMessageDB()
            val doc = Document()
                    .append("_id",it.messageID)
                    .append("author",it.author.longID)
                    .append("channel",it.channel.longID)
                    .append("content",it.message.content)
            if(!it.channel.isPrivate){
                doc.append("guild",it.guild.longID)
            }
            println("inserting doc into db")
            coll.insertOne(doc)
        })

    }

    override fun genKotlinCommands(commands: MutableMap<String, (MessageReceivedEvent) -> Unit>, handle: Handler) {
        //note: prefixes are automatically applied
        commands["chainASDF"] = command@ {event ->
            val content = event.message.content
            val argument :String? =if(content == "!chain") null else content.replace("!chain","").trim()


            val USER = "USER"; val GUILD = "GUILD"; val GLOBAL = "GLOBAL"; val CHANNEL = "CHANNEL"; val UNKNOWN = "UNKNOWN"
            var queryType: String? = null
            var queryData :Long? = null
            when{
                argument == null -> {
                    handle.sendMessage(event, "*help menu*")
                    return@command
                }
                event.message.mentions.size == 1 -> {
                    //run with mention
                    queryType = USER
                    queryData = event.message.mentions[0].longID
                }
                event.message.mentions.size > 1 -> {
                    handle.sendMessage(event, "I can't use multiple users at once, sorry")
                    return@command
                }
                event.message.channelMentions.size == 1 -> {
                    queryType = CHANNEL
                    queryData = event.message.channelMentions[0].longID
                }
                event.message.channelMentions.size > 1 -> {
                    handle.sendMessage(event, "I can't use multiple channels at once, sorry")
                    return@command
                }
                argument.toUpperCase() == "GLOBAL" -> {
                    queryType = GLOBAL
                }
                argument.toUpperCase() == "GUILD" || argument.toUpperCase() == "SERVER" -> {
                    queryType = GUILD
                }
                event.message.mentionsHere() -> {
                    handle.sendMessage(event, "I can't use `@here` for this")
                }
                else -> {
                    try{
                        queryData = argument.toLong()
                        queryType = UNKNOWN
                    }catch(e :NumberFormatException){
                        handle.sendMessage(event,"I can't understand that argument :cry:")
                        return@command
                    }
                }
            }
            //now that we have the data, we need to build up the entire database structure before we can continue
            handle.sendMessage(event,"I don't have any more information, but I can tell you that:\n" +
                    "content=`$content`\n" +
                    "argument=`$argument`\n" +
                    "queryType=`$queryType`\n" +
                    "queryData=`$queryData`\n")


            //gotta make the query now

            //attempt to find the right chain. if it doesn't exist, push it to the stack and start working on it in the background
            val chain = when (queryType) {
                GUILD -> ChainCache guild queryData!!
                CHANNEL -> ChainCache channel queryData!!
                USER -> ChainCache author queryData!!
                GLOBAL -> ChainCache.global
                UNKNOWN -> {
                    val chain = ChainCache guild queryData!! ?: ChainCache channel queryData ?: ChainCache author queryData
                    if(chain != null) chain
                    else { when {
                        event.guild.getUserByID(queryData) != null -> queryType = USER
                        event.client.guilds.any { it.longID == queryData } -> queryType = GUILD
                        event.guild.channels.any { it.longID == queryData } -> queryType = CHANNEL
                    }; null }
                }
                else -> null
            }
            if(chain == null){
                if(queryType == UNKNOWN){
                    //if it wasn't identified
                    handle.sendMessage(event,"I can't find that. I'm not very good at identifying ID's. Please use a mention if possible")
                    return@command
                }
            }
            handle.sendMessage(event, "I haven't coded that path yet! ")
            handle.sendMessage(event,"I don't have any more information, but I can tell you that:\n" +
                    "content=`$content`\n" +
                    "argument=`$argument`\n" +
                    "queryType=`$queryType`\n" +
                    "queryData=`$queryData`\n")

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

    val global = Chain()
}}



class Chain{
    val map :MutableMap<String,MutableList<String>> = mutableMapOf()
    /** feeds another message into the chain */
    fun feed(input: String) {
        val list :List<String> = parse(input)
        for(i in 1 until list.size){
            if(!map.containsKey(list[i-1]))
                map[list[i-1]] = mutableListOf()
            map[list[i-1]]!!+=list[i]
        }
    }

    private fun parse(input: String): List<String> = (END + input + START).split(" ").filter { it.isNotBlank() }

    fun generateSentance() :String{
        val str = mutableListOf<String>()
        str+=START
        while(!str.isEmpty() && str.last() != END){
            val mapPos = str.last()
            if(map[mapPos] == null){
                str+=END
                break
            }
            val rand = (Math.random() * map[mapPos]!!.size).toInt()
            str+=map[mapPos]!![rand]
        }
        str.removeIf { it == END || it == START }
        return str.fold("") {fold,one -> "$fold $one"}.trim()
    }

}
