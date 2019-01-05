package com.carson.commands

import java.lang.StringBuilder
import java.util.*

class Chain{
    private val map :MutableMap<String,MutableList<String>> = mutableMapOf()
    /** feeds another message into the chain */
    fun feed(input: String) {
        val list :List<String> = parse(input)
        for(i in 1 until list.size){
            if(!map.containsKey(list[i-1]))
                map[list[i-1]] = mutableListOf()
            map[list[i-1]]!!+=list[i]
        }
    }

    private fun parse(input: String): List<String> = ("$START $input $END").split(" ").filter { it.isNotBlank() }

    fun generateSentance() :String{
        val str = mutableListOf<String>()
        str+= START
        while(!str.isEmpty() && str.last() != END){
            val mapPos = str.last()
            if(map[mapPos] == null){
                str+= END
                break
            }
            val rand = (Math.random() * map[mapPos]!!.size).toInt()
            str+=map[mapPos]!![rand]
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
}