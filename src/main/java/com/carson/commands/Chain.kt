package com.carson.commands

import java.lang.StringBuilder
import java.util.*

class Chain{
    private val map :MutableMap<String,MutableList<String>> = mutableMapOf()
    /** feeds another message into the chain */
    fun feed(input: String) {
        if(input.startsWith("!"))return
        if(input.contains(START) || input.contains(END))return
        if(input.isBlank())return
        val list :List<String> = parse(input)
        for(i in 1 until list.size){
            if(!map.containsKey(list[i-1]))
                map[list[i-1]] = mutableListOf()
            map[list[i-1]]!!+=list[i]
        }
    }

    private fun parse(input: String): List<String> = ("$START $input $END").split(" ").filter { it.isNotBlank() }


    fun generateSentenceChecked() :String{
        var str :String
        var i = 0//forever blocking calls won't block forever
        do{
            i++
            str = generateSentence()
        }while(i < 100 && !isSentenceValid(str))
        return str
    }

    private fun generateSentence() :String{
        val str = mutableListOf<String>()
        str+= START
        while(!str.isEmpty() && str.last() != END){
            val mapPos = str.last()
            if(map[mapPos] == null){
                str+= END
                break
            }
            //hopefully makes longer sentences
            str+= (if(str.size <= 2) map[mapPos]!!.filter { it != END } else map[mapPos]!!).random() ?: END

        }
        str.removeIf { it == END || it == START }
        return str.fold("") {fold,one -> "$fold $one"}.trim()
    }

    override fun toString(): String {
        val b = StringBuilder()
        map.forEach { s,l ->
            b.append('[').append(s).append("] : ").append(Arrays.toString(l.toTypedArray())).append('\n')
        }
        return b.toString()
    }

    fun getMap(): MutableMap<String, MutableList<String>> {
        val map :MutableMap<String,MutableList<String>> = mutableMapOf()
        map.putAll(this.map.map { Pair(it.key,it.value.map { w -> w }.toMutableList()) })
        return map
    }


}

fun <T> List<T>.random() :T? = if(size == 0) null else this[(Math.random()*size).toInt()]


const val VALID = "!VALID!"
fun isSentenceValid(str :String):Boolean = isSentenceValidString(str) == VALID

fun isSentenceValidString(str :String) :String{
    val split = str.split(" ").map { it.trim() }
//    var i = 0
//    split.forEach { println("${i++} : \"${it.replace("\n","\\n")}\"") }
    if(str.toCharArray().count { it == ' '} < 2) return "Too little words"
    if(split.distinct().size <= split.size / 2) return "Too many duplicates" //if more then half the words are duplicates
    if(split.count { it[0] == ':' && it[it.length - 1] == ':' } > split.size - 2) return "Too many emojis" //if less than two words are real words
    if(split.count { it.length == 1 } > split.size - 2) return "Too many one-letter words" //if the count of single-letter words makes up length-2 of the sentence
    if(str.contains("`"))return "Can't include backticks"
    return VALID
}