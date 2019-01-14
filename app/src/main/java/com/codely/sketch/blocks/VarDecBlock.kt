package com.codely.sketch.blocks

import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect

/**
 * Created by Daniel on 10/16/2017.
 */

enum class VarType {
    NUMBER, STRING, ARRAY;

    companion object {
        fun fromString(type: String): VarType {
            val normalized = type.toLowerCase()
            when (normalized) {
                "number" -> return NUMBER
                "string" -> return STRING
                "array" -> return ARRAY
                else -> throw Throwable("TYPE NOT SUPPORTED")
            }
        }
    }
}

class VarDecBlock(name: String, initValue: Any, x: Int, y: Int, varType: VarType) : CodeBlock {
    var varName: String = name
    var varType: VarType = varType
    var value: Any = initValue

    override val type: BlockType = BlockType.VAR_DEC
    override var connectionPath: Path = Path()
    override var blockPaint: Paint = generateBlockPaint()
    override var pathPaint: Paint = generatePathPaint()
    override var textPaint: Paint = generateTextPaint()

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