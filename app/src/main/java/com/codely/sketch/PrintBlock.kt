package com.codely.sketch

import android.graphics.Rect

class PrintBlock(printBlock: VarDecBlock, x: Int, y: Int) : CodeBlock {
    override val type: BlockType = BlockType.PRINT
    override var nextBlock: CodeBlock? = null
    override var rect: Rect = Rect(x, y, x + BlockSize.BLOCK_WIDTH.number, y + BlockSize.BLOCK_HEIGHT.number)

    private var printBlock = printBlock

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
        return "Print %s".format(printBlock.varName)
    }
}