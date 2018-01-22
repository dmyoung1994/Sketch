package com.codely.sketch

import android.graphics.Rect

/**
 * Created by Daniel on 10/16/2017.
 */
enum class BlockType {
    VAR_DEC, PRINT, IF_ELSE, LOOP, MODIFIER
}

enum class BlockSize(val number:Int) {
    BLOCK_WIDTH(500), BLOCK_HEIGHT(200)
}

interface CodeBlock {
    val type: BlockType
    var nextBlock: CodeBlock?
    var rect: Rect

    fun run()
    fun convertToPython()
    fun convertToC()
    fun convertToJava()
    fun getBlockText(): String
}