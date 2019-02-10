package com.carson.commands



//style:
//name | args | short desc | long desc ***
//use *** to mark the end of the command
//newlines will be deleted. Don't use spaces around the |

//split arguments with &
//ABC will stand for the argument name
//use (ABC) for optional args
//use <ABC:OP_X/OP_Y/NULL> for a limited amount of options
//   use NULL for the option to leave it blank
//use [ABC] for a required argument
//

//when mentioning an argument in the long desc, format it the exact same way in order to highlight it when printed
val STR = """
help|(command name)|Get the help menu|Gets the general help menu, if (command name) is specified this will get the help menu for that command
***
invite||get an invite link|Get an invite link
***
ping||test the ping|test how long it takes for a message to be sent with the bot, in milliseconds
***
status||get the bot status|Gets bot uptime, server uptime, guild count, user count, ect
***
chain||generate a Markov chain based on arguments|use the command without arguments for more information
***
save|[id]|save the id of a chain-generated message|Saves the message to your ~saved list
***
asdf|[message]|test a message|Tests the possibility of message [message] occurring. Not very accurate
***
word||Generate a word|Generates a word using a nice Markov chain using the entirety of the english language
""".trimIndent().replace("\n","")

var HELP :List<Entry> = mutableListOf()
get() {
    if(field.isNotEmpty())
        return field
    val arr = mutableListOf<Entry>()
    val strEntrys = STR.split("***").filter { !it.isEmpty() }
    for(strEntry in strEntrys){
        val entrySplit = strEntry.split("|", limit = 4)
        val name = entrySplit[0]
        val argsStr = entrySplit[1]
        val short = entrySplit[2]
        val long = entrySplit[3]

        val argumentList = mutableListOf<Argument>()
        if(!argsStr.isEmpty()) {
            for (argStr in argsStr.split("&&")) {
                val subString = argStr.substring(1, argStr.length - 1)
                when (argStr.toCharArray()[0]) {
                    '(' -> argumentList.add(Argument(subString, argStr, Type.OPTIONAL))
                    '[' -> argumentList.add(Argument(subString, argStr, Type.REQUIRED))
                    '<' -> argumentList.add(Set(subString.split(":")[0], argStr,
                            subString.split(":", limit = 2)
                                    [1]
                                    .split("/")))
                }
            }
        }
        arr.add(Entry(name, argumentList, short,long))
    }
    field = arr
    return field
}

data class Entry(val name :String,
                 val args :List<Argument>,
                 val short :String,
                 val long :String)

open class Argument(open val name :String,
                    open val raw :String,
                    val type :Type)

class Set(@Transient override val name :String,
          @Transient override val raw :String,
          val options: List<String>) :Argument(name,raw,Type.SET)

enum class Type{
    OPTIONAL,REQUIRED,SET
}