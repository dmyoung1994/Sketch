package com.codely.sketch.blocks

import android.graphics.Rect

/**
 * Created by Daniel on 11/7/2017.
 */

class IfElseBlock(conditionBlock: VarDecBlock, compare: String, target: String, x: Int, y: Int) : CodeBlock {
    override val type: BlockType = BlockType.IF_ELSE
    override var nextBlock: CodeBlock? = null
    override var rect: Rect = Rect(x, y, x + BlockSize.BLOCK_WIDTH.number, y + BlockSize.BLOCK_HEIGHT.number)

    private var conditionBlock = conditionBlock
    private var compare = compare
    private var target = target

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
        return "If %s %s %s than...Otherwise...".format(conditionBlock.varName, compare, target)
    }
}