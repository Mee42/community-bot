package com.carson.commands

import com.mongodb.client.model.Filters
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.util.*
import java.util.concurrent.Executors

class ChainStack{
    companion object {
        public val singleton = ChainStack()
        init {
            push(Context.GLOBAL,-1)
            val executer = Executors.newFixedThreadPool(2)
            executer.execute {
                while(singleton.run){
                    singleton.popEvent()
                    Thread.sleep(10)
                }
            }
            executer.execute {
                while(singleton.run){
                    singleton.popChain()
                    Thread.sleep(100)
                }
            }
        }
        public fun push(event : MessageReceivedEvent) = singleton.push(event)
        public fun push(context : Context, id :Long) = singleton.push(Pair(context,id))
    }
    private var run = true
    private val messageStack = Stack<MessageReceivedEvent>()
    private val chainStack = Stack<Pair<Context, Long>>()

    fun push(event: MessageReceivedEvent){ messageStack.push(event) }
    fun push(chainId :Pair<Context,Long>){ chainStack.push(chainId) }
    fun popEvent(){
        if(messageStack.empty())return
        val event = messageStack.pop()
        val content = event.message.content
        if(content.startsWith("!"))return
        if(content.contains(START) || content.contains(END))return
        if(content.isBlank())return
        (ChainCache author event.message.author.longID)?.feed(content)
        (ChainCache channel event.channel.longID)?.feed(content)
        (ChainCache guild event.guild.longID)?.feed(content)
        (ChainCache.global)?.feed(content)
    }
    fun popChain(){
        if(chainStack.empty())return
        val pair = chainStack.pop()
        val context = pair.first
        if(context == Context.UNKNOWN)throw UnsupportedOperationException("Can not pop a chain with an unknown type")
        if(context == Context.GLOBAL && ChainCache.global != null)throw UnsupportedOperationException("Can not pop global chain as it has already been created")
        val id = pair.second
        val found = when(context){
            Context.GLOBAL -> getMessageDB().find()
            else -> getMessageDB().find(Filters.all(context.databaseName, id))
        }
        val chain = Chain()
        found.forEach {
            if(it["content"] != null)
                if(!it.get("content",String::class.java).startsWith("!chain"))
                    chain.feed(it.get("content",String::class.java))
        }
        when(context){
            Context.GUILD -> ChainCache.guild(id, chain)
            Context.USER -> ChainCache.author(id, chain)
            Context.CHANNEL -> ChainCache.channel(id, chain)
            Context.GLOBAL -> ChainCache.global = chain
            Context.UNKNOWN -> throw java.lang.UnsupportedOperationException("Can not pop a chain with an unknown type") //can't cache...?
        }
        println("Popped chain:$id $context")
    }
}