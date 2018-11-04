package com.carson.commands

import java.util.regex.Pattern
import javax.management.monitor.StringMonitor


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
helpraw||Raw json help menu|Returns all infomation about the commands in JSON format. ⚠Mainly for testing⚠
***
poll|[question]&&<&:&>&&[answers]|Create a poll|
Create a poll with different options. Question must be seperated from the answers with a `&`,and answers must be split up with a `|` character
***
invite||get an invite link|Get an invite link
***
contribute||get info on how to contribute|get info on how to contribute
***
mc||hahahaha|change everyone's nickname so it starts with Mc. Has an approval stage, just running the command won't change anything until an admin approves
***

""".trimIndent().replace("\n","")

var HELP :List<Entry> = mutableListOf()
get() {
    if(field.isNotEmpty())
        return field
    var arr = mutableListOf<Entry>()
    val strEntrys = STR.split("***").filter { !it.isEmpty() }
    for(strEntry in strEntrys){
        val entrySplit = strEntry.split("|", limit = 4)
        val name = entrySplit[0]
        val argsStr = entrySplit[1]
        val short = entrySplit[2]
        val long = entrySplit[3]

        var argumentList = mutableListOf<Argument>()
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