package com.codely.sketch

/**
 * Created by Daniel on 10/16/2017.
 */
enum class BlockType {
    VAR_DEC, PRINT, IF_ELSE, LOOP, MODIFIER
}

interface CodeBlock {
    val type: BlockType
    var nextBlock: CodeBlock?
    fun run()
    fun convertToPython()
    fun convertToC()
    fun convertToJava()
}