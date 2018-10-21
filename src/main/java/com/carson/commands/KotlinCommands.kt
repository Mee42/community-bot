package com.carson.commands

import com.carson.core.*
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.MissingPermissionsException
import java.lang.Exception
import java.util.function.Predicate


class KotlinCommands() : CommandCollection("Carson") {
    override fun genCommands(commands: MutableList<Command>?, handle: Handler?) {
        if(commands == null || handle == null)
            return
        commands.add(toCommand(Test.startsWith("poll"), CommandLambda {
            var content = it.message.content ?: return@CommandLambda
            content = content.replaceFirst(Main.PREFIX + "poll", "")
            if(content == "help" || content == ""){
                handle.sendMessage(it,"usage: !poll question & answer-1 | answer-2 | ect")
                return@CommandLambda
            }

            var split = content.split("|").toMutableList()
            val question :String
            if(split[0].split("&").size == 1){
                question = ""
            }else {
                question = split[0].split("&")[0]
                split[0] = split[0].split("&")[1]
            }
            if(split.size == 0 || split.size == 1){
                handle.sendMessage(it, "not enough answers")
                return@CommandLambda
            }

            if (split.size > emojis.size) {
                handle.sendMessage(it, "To many options")
                return@CommandLambda
            }
            var index = 0
            val map = split.associate { Pair((emojis["${arr[index++]}"]), it) }//generate an array
            var message = "$question\nOptions:\n"
            for ((key, value) in map) {
                message += "$key : $value\n"
            }
            val messageSent = handle.sendMessageAndGet(it.channel, message)
            for (key in map.keys)
                RequestBuffer.request { messageSent.addReaction(ReactionEmoji.of(key))}.get()

            }))

        commands.add(toCommand(Test.startsWith("invite"), CommandLambda { handle.sendMessage(it,"https://discordapp.com/oauth2/authorize?client_id=500780039030308875&scope=bot") }))

        commands.add(toCommand(Test {
            var arr = it.message.mentions
            if(arr.size != 1)
                return@Test false
            return@Test arr[0] == it.client.ourUser
        }, CommandLambda {
            handle.sendMessage(it, "Hi, I'm <@500780039030308875>, a bot made to give people the ability to write" +
                    " Discord bots without having to host it or manage boilerplate. I'm written in java and mainly run by" +
                    " <@293853365891235841>, and contributing is really easy. Use !contribute to find out how!\n" +
                    "Commands avalible are:```\ninvite\npoll\nping\nstatus\ncontribute\n```")
        }))

        commands.add(toCommand(Test.startsWith("contribute"), CommandLambda {
            handle.sendMessage(it, "You can find the guild to start contributing here:" +
                    "https://github.com/Mee42/community-bot/wiki\n" +
                    "You can write both Java and Pesudo code, allowing everyone to contribute.")
        }))


        commands.add(toCommand(Test.startsWith("mc"), CommandLambda {
            var users = it.guild.users.map { user -> Pair(user, getName(user,it.guild))}.toMap().filter { (_,value) -> !value.replace("[","").toLowerCase().startsWith("mc")}

            users = users.map { (key,value) -> Pair(key,"Mc$value") }.toMap()

            val str = users.toList().fold("") { all, pair -> "$all\n${getName(pair.first,it.guild)} : ${pair.second}"}.trim()

            val message = handle.sendMessageAndGet(it.channel, "I will change these nicknames:\n```\n$str\n```\nHave an admin react :+1: to approve")

            RequestBuffer.request { message.addReaction(ReactionEmoji.of("\uD83D\uDC4D")) }.get()

            val reactionEvent = it.client.dispatcher.waitFor(Predicate<ReactionAddEvent> { mes ->
                return@Predicate mes.messageID == message.longID &&
                        mes.user != it.client.ourUser
                        && mes.author.getPermissionsForGuild(mes.guild).any { it == Permissions.ADMINISTRATOR}
            })
            handle.sendMessageAndGet(it.channel,reactionEvent.user.mention() + " has approved changes. Making changes now")
            var failedUsers = mutableListOf<String>()
            users.forEach {
                RequestBuffer.request {
                    try {
                        message.guild.setUserNickname(it.key, it.value)
                    } catch(e : Exception){
                        failedUsers.add(getName(it.key,message.guild))
                    }
                }.get()
            }
            handle.sendMessage(it,"I wasn't able to set these users:${failedUsers.fold("```\n") { all, key -> "$all\n$key" }}```")
        }))


    }

    fun getName(user : IUser, guild : IGuild) : String = if (user.getNicknameForGuild(guild)?: "null" == "null") user.name else user.getNicknameForGuild(guild)

    companion object {
        val arr = "abcdefghijklmnopqrstufvwxyz".toCharArray()
        val emojis =  arr.mapIndexed {  index, c -> "$c" to "\uD83C${'\uDDE6' + index}"}.toMap()
    }
}