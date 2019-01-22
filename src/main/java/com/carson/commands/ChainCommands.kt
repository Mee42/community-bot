package com.carson.commands

import com.carson.core.*
import com.carson.core.Main.PREFIX
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.vdurmont.emoji.EmojiManager
import org.bson.Document
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent
import sx.blah.discord.util.RequestBuffer
import java.lang.NumberFormatException
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
        thread { println("message counts:" + getMessageCollection().countDocuments()) }
    }
    override fun genWeirdCommands(commands: MutableList<Command>, handle: Handler) {}


    override fun genKotlinCommands(commands: MutableMap<String, (MessageReceivedEvent) -> Unit>, handle: Handler) {
        //note: prefixes are automatically applied

        commands["cache"] = command@ {
            val b = StringBuilder()
            b.append("Guilds:${ChainCache.guildSize}\n")
            b.append("Users:${ChainCache.authorSize}\n")
            b.append("Channels:${ChainCache.channelSize}\n")
            handle.sendMessage(it,"Cache:\n```\n$b\n```")
        }

        commands["chain"] = command@ {event ->
            val content = event.message.content
            val argument :String? =if(content == "!chain") null else content.replace("!chain","").trim()


            var queryType: Context?
            var queryData :Long? = null
            when{
                argument == null -> {
                    handle.sendMessage(event, """
-~-  Help Menu  -~-
!chain global          |  generate based on every message
!chain server          |  generate based on only messages from this server
!chain @mention  |  generate based only on messages from that person
!chain #mention   |  generate based only on messages in that channel
!chain *id*                  |  use an id for channel, server, or user

                    """.trimIndent())
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
//                    handle.sendMessageAndGet(event.channel, "I'm putting that to the queue for processing. Once it's processed, new messages will be added to it dynamically.\n" +
//                            "This should take anywhere from seconds to a minute or two for big servers. Keep in mind, collection will only include messages since 2019-1-5.\n" +
//                            "I will automatically send it if the chain completes within 10 seconds")
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
                        sendChainMessage(chain!!,event)
                    }
                    return@command
                }
            sendChainMessage(chain,event)
        }

        commands["best"] = command@ {event ->
            val b = StringBuilder()
            var length = 0
            getBotMessageCollection()
                    .find()
                    .toList()
                    .map { Triple(it,it[upvotes],it[downvotes])}
                    .filter { it.second != null && it.third != null }
                    .map { Pair(it.first,it.second.toString().toInt() - it.third.toString().toInt()) }
                    .sortedBy { it.second }
                    .map { length++;it }
                    .subList(0,if(length > 5) 5 else length)
                    .forEachIndexed { index, x ->
                        b.append(index + 1).append(" : `").append(x.first["content"].toString()).append("`\n")
                    }
            handle.sendMessage(event,b.toString())
        }

        commands["upvote"] = command@ {event ->
            if(event.author.longID != 293853365891235841)return@command

            val strId = event.message.content.substring("!upvote".length).trim()
            val id = try{ strId.toLong() }catch(e :NumberFormatException){
                handle.sendMessage(event,"unable to parse `$strId`")
                return@command
            }
            val message = getBotMessageCollection().find(Filters.all("_id",id)).first()
            if(message == null){
                handle.sendMessage(event,"Could not find the message with the id of $id")
                return@command
            }
            if(message.containsKey(upvotes))
                message[upvotes] = message[upvotes] as Int + 1
            else
                message[upvotes] = 1
            handle.sendMessage(event, "Done!")
        }
        commands["downvote"] = command@ {event ->
            if(event.author.longID != 293853365891235841)return@command
            val strId = event.message.content.substring("!downvote".length).trim()
            val id = try{ strId.toLong() }catch(e :NumberFormatException){
                handle.sendMessage(event,"unable to parse `$strId`")
                return@command
            }
            val message = getBotMessageCollection().find(Filters.all("_id",id)).first()
            if(message == null){
                handle.sendMessage(event,"Could not find the message with the id of $id")
                return@command
            }
            if(message.containsKey(upvotes))
                message[upvotes] = message[upvotes] as Int - 1
            else
                message[upvotes] = -1
            handle.sendMessage(event, "Done!")

        }

        commands["saved"] = command@ {event ->
            val id = event.author.longID
            val arr = getBotMessageCollection().find(Filters.all("saved",id))
            val b = StringBuilder().append("Here are your saved messages:\n")
            arr.forEach {
                b.append("`" + it["_id"].toString() + "` : " + it["content"] + "\n")
            }
            handle.sendMessage(event,b.trim().toString())
        }

        commands["save"] = command@ {event ->
            val content = event.message.content.substring("${PREFIX}save".length).trim()
            val authorId = event.author.longID
            val id = try{ content.toLong() }catch(e :NumberFormatException){
                handle.sendMessage(event,"I can't parse that. Please put the ID of the sentance you want to save. This only works for !chain messages")
                return@command
            }
            val doc :Document? = getBotMessageCollection().find(Filters.all("_id",id)).first()
            if(doc == null){
                handle.sendMessage(event, "I can't find that message. Make sure it comes from a !chain command")
                return@command
            }
//            val sentence = doc["content"].toString()
            @Suppress("UNCHECKED_CAST")
            val arr = (doc["saved"] as? List<Long>)?.toMutableList() ?: mutableListOf()//watch this fail lol
            if(arr.contains(authorId)) {
                arr.remove(authorId)
                handle.sendMessage(event, "Removed :+1:")
            }else{
                arr.add(authorId)
                handle.sendMessage(event, "Added :+1:")
            }
            doc.remove("saved")
            doc["saved"] = arr
            getBotMessageCollection().replaceOne(Filters.all("_id",id),doc)
        }



    }


    /*  <@293853365891235841> */
    //  012345678901234567890
    private fun sendChainMessage(chain :Chain, event :MessageReceivedEvent){
        val content = parse(chain.generateSentenceChecked().replace("```","\\```")) { id -> event.client.fetchUser(id).name +"#" + event.client.fetchUser(id).discriminator}
        val message  = RequestBuffer.request(RequestBuffer.IRequest { event.channel.sendMessage("`\$ID_GOES_HERE`  Here's your phrase: ```\n$content```") }).get()
        val messageContent = message.content.replace("\$ID_GOES_HERE",message.longID.toString())
        RequestBuffer.request { message.edit(messageContent) }
        getBotMessageCollection().insertOne(Document().append("_id",message.longID).append("content",content))
        RequestBuffer.request { message.addReaction(EmojiManager.getForAlias("thumbsup")) }.get()
        RequestBuffer.request {  message.addReaction(EmojiManager.getForAlias("thumbsdown")) }.get()
    }

}

fun parse(contentX :String,namer : (Long) -> String):String{
    var content = contentX
    for(i in 0 until content.length - ("<@>".length + 17)){
        val sub = content.substring(i)
        if(sub[0] != '<' || sub[1] != '@')continue
        val ad = if(sub[2] == '!') 3 else 2

        println("sub:" + sub.substring(ad,ad + 18) + ":" + sub.substring(ad,ad + 17))
        val id = sub.substring(ad,ad + 18).toLongOrNull() ?: sub.substring(ad,ad + 17).toLongOrNull() ?: continue
        content = content.replace("<@${if(ad==3) "!" else ""}$id>",namer(id))//maybe this will fail? maybe not
    }
    return content
}


const val upvotes = "upvotes"
const val downvotes = "downvotes"
class EmojiHandler{
    @EventSubscriber
    fun emojiAdd(event: ReactionAddEvent) {
        onEmojiEvent(event,true)
    }
    @EventSubscriber
    fun emojiRemove(event : ReactionRemoveEvent){
        onEmojiEvent(event,false)
    }

    fun onEmojiEvent(event :ReactionEvent, add :Boolean){
        val id = event.messageID
        if(event.user.isBot)return
        val doc = getBotMessageCollection()
                .find(Filters.all("_id",id))
                .first() ?: return
        if(event.reaction.emoji.name == "\uD83D\uDC4D")//upvote
            if(doc.containsKey(upvotes))
                doc[upvotes] = doc[upvotes] as Int + if (add) 1 else -1
            else
                doc[upvotes] = if(add) 1 else -1

        if(event.reaction.emoji.name == "\uD83D\uDC4E")//downvote
            if(doc.containsKey(downvotes))
                doc[downvotes] = doc[downvotes] as Int + if (add) 1 else -1
            else
                doc[downvotes] = if(add) 1 else -1

        getBotMessageCollection().replaceOne(Filters.all("_id",id),doc)

    }


}

val client :MongoClient = MongoClient("192.168.1.203:27017")
val db :MongoDatabase = client.getDatabase("carson-bot")
fun getMessageCollection() :MongoCollection<Document> = db.getCollection("messages")

fun getBotMessageCollection() :MongoCollection<Document> = db.getCollection("bot-messages")

class ChainCache{companion object{
    private val guilds = mutableMapOf<Long,Chain>()
    val guildSize
    get() = guilds.size
    infix fun guild(long :Long) :Chain?{
        return guilds[long]
    }
    fun guild(id :Long,chain :Chain){
        guilds[id]=chain
    }

    private val authors = mutableMapOf<Long,Chain>()
    val authorSize
    get() = authors.size
    infix fun author(long :Long) :Chain?{
        return authors[long]
    }
    fun author(id :Long,chain :Chain){
        authors[id]=chain
    }

    private val channels = mutableMapOf<Long,Chain>()
    val channelSize
    get() = channels.size
    infix fun channel(long :Long) :Chain?{
        return channels[long]
    }
    fun channel(id :Long,chain :Chain){
        channels[id]=chain
    }

    var global :Chain? = null

}}