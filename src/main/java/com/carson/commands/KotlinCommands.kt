package com.carson.commands

import com.carson.core.*
import com.google.gson.GsonBuilder
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.MissingPermissionsException
import java.util.function.Predicate


class KotlinCommands : CommandCollection("Carson") {
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

            val split = content.split("|").toMutableList()
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
            val map = split.associate { itt -> Pair((emojis["${arr[index++]}"]), itt) }//generate an array
            var message = "$question\nOptions:\n"
            for ((key, value) in map) {
                message += "$key : $value\n"
            }
            val messageSent = handle.sendMessageAndGet(it.channel, message)
            for (key in map.keys)
                RequestBuffer.request { messageSent.addReaction(ReactionEmoji.of(key))}.get()

            }))

        commands.add(toCommand(Test.startsWith("invite"), CommandLambda { handle.sendMessage(it,"https://discordapp.com/oauth2/authorize?client_id=500780039030308875&scope=bot") }))

        //mentions
        commands.add(toCommand(Test {
            val arr = it.message.mentions
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
                        mes.user != it.client.ourUser &&
                        ( mes.author.getPermissionsForGuild(mes.guild).any { itt -> itt == Permissions.ADMINISTRATOR } || mes.guild.owner == mes.author)
            })
            handle.sendMessageAndGet(it.channel,reactionEvent.user.mention() + " has approved changes. Making changes now")
            users.forEach {itt ->
                RequestBuffer.request {
                    try { message.guild.setUserNickname(itt.key, itt.value) }
                    catch(e :MissingPermissionsException){}
                }.get()
            }
            val failedUsers = it.guild.users.map { user -> Pair(user, getName(user,it.guild))}.toMap()
                    .filter { (_,value) -> !value.replace("[","").toLowerCase().startsWith("mc")}.toList()

            handle.sendMessage(it,"I wasn't able to set these users:${failedUsers.fold("```\n") { all, key -> "$all\n${key.second}" }}```")
        }))

        commands.add(toCommand(Test.startsWith("helpraw"), CommandLambda {
            handle.sendMessage(it, "```json\n${GsonBuilder().setPrettyPrinting().create().toJson(HELP)}```")
        }))

        commands.add(toCommand(Test.startsWith("help"), CommandLambda {
            val message = it.message.content
            if(message.split(" ").size == 1){
                val str = HELP.fold("") {all,one -> "$all\n${one.name} ${
                one.args.fold("") {all2,one2 -> "$all2 ${if(one2.type == Type.SET) one2.raw.replaceFirst("${one2.name}:","") else one2.raw}"}
                }  |  ${one.short}" }
                handle.sendMessage(it,"```\n$str```")
            }else{
                val command = message.split(" ")[1]
                val entry = HELP.find { entry -> entry.name == command }
                if(entry == null){
                    handle.sendMessage(it,"I could not find that command")
                    return@CommandLambda
                }

                val folded = entry.args.fold("") {all,one->
                    if(one.type == Type.SET){

                    }
                    return@fold "$all\n" + "`" + one.name + "` :  " + if(one.type == Type.OPTIONAL) "Optional" else "Required"
                }.replaceFirst("\n","")

                var long = entry.long
                for(arg in entry.args){
                    long = long.replace(arg.raw,"`${arg.raw}`")
                }

                var argsStr = "\n$folded\n\n"
                if(folded.isEmpty())
                    argsStr = "\n"

                val str = "${entry.name}:$argsStr$long"
                handle.sendMessage(it,str)
            }
        }))


    }

    private fun getName(user : IUser, guild : IGuild) : String = if (user.getNicknameForGuild(guild)?: "null" == "null") user.name else user.getNicknameForGuild(guild)

    companion object {
        val arr = "abcdefghijklmnopqrstufvwxyz".toCharArray()
        val emojis =  arr.mapIndexed {  index, c -> "$c" to "\uD83C${'\uDDE6' + index}"}.toMap()
    }
}