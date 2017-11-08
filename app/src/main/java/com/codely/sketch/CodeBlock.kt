package com.codely.sketch

/**
 * Created by Daniel on 10/16/2017.
 */
enum class BlockType {
    VAR_DEC, PRINT, IF_ELSE, LOOP, MODIFIER
}

interface CodeBlock {
    var xCord: Float
    var yCord: Float
    var height: Int
    var width: Int
    val type: BlockType
    var nextBlock: CodeBlock?


    fun run()
    fun convertToPython()
    fun convertToC()
    fun convertToJava()
    fun getBlockText(): String
}