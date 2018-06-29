package com.codely.sketch.blocks

import android.graphics.Path
import android.graphics.Rect

class ModifyBlock(private var toModify: VarDecBlock, private var modifier: String, private var value: Int, x: Int, y: Int) : CodeBlock {
    override val type: BlockType = BlockType.RETURN
    override var connectionPath: Path = Path()
    override var rect: Rect = Rect(x, y, x + BlockSize.BLOCK_WIDTH.number, y + BlockSize.BLOCK_HEIGHT.number)
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
        return "%s %s %s;".format(toModify.varName, modifier, value.toString())
    }

    private fun modifyBlockValue() {
        return when(modifier) {
            "+"    -> toModify.value += value
            "-"    -> toModify.value -= value
            "*"    -> toModify.value *= value
            "/"    -> toModify.value /= value
            "%"    -> toModify.value %= value
            else -> {}
        }
    }

    override fun run() {
        modifyBlockValue()
        nextBlock?.run()
    }

    override fun getBlockText(): String {
        return "%s %s %s".format(toModify.varName, modifier, value.toString())
    }
}