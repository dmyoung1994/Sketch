package com.codely.sketch

import android.graphics.Rect

/**
 * Created by Daniel on 10/16/2017.
 */
class VarDecBlock(name: String, x: Int, y: Int) : CodeBlock {
    var varName: String = name

    override val type: BlockType = BlockType.VAR_DEC
    override var nextBlock: CodeBlock? = null
    override var rect: Rect = Rect(x, y, x + BlockSize.BLOCK_WIDTH.number, y + BlockSize.BLOCK_HEIGHT.number)

    override fun convertToPython() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun convertToC() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun convertToJava() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun run() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBlockText(): String {
        return "var %s = 0".format(varName)
    }
}