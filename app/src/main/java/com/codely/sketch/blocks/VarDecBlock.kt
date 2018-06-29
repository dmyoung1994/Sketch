package com.codely.sketch.blocks

import android.graphics.Path
import android.graphics.Rect

/**
 * Created by Daniel on 10/16/2017.
 */
class VarDecBlock(name: String, x: Int, y: Int) : CodeBlock {
    var varName: String = name
    var value: Int = 0

    override val type: BlockType = BlockType.VAR_DEC
    override var connectionPath: Path = Path()
    override var parentBlock: CodeBlock? = null
    override var nextBlock: CodeBlock? = null
        set(value) {
            field = value
            when (value) {
                null -> connectionPath.reset()
                else -> {
                    connectionPath.moveTo(rect.exactCenterX(), rect.exactCenterY())
                    connectionPath.lineTo(value.rect.exactCenterX(), value.rect.exactCenterY())
                }
            }
        }
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

    override fun convertToJavascript(): String {
        var line = "var %s = 0;".format(varName)
        if (nextBlock != null) {
            line += " %s".format(nextBlock!!.convertToJavascript())
        }

        return line + "\n"
    }

    override fun run() {
        nextBlock?.run()
    }

    override fun getBlockText(): String {
        return "var %s = 0".format(varName)
    }
}