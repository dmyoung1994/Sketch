package com.codely.sketch.blocks

import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import java.lang.Exception

class ModifyBlock(private var toModify: VarDecBlock, private var modifier: String, private var value: Any, x: Int, y: Int) : CodeBlock {
    override val type: BlockType = BlockType.MODIFIER
    override var connectionPath: Path = Path()
    override var rect: Rect = Rect(x, y, x + BlockSize.BLOCK_WIDTH.number, y + BlockSize.BLOCK_HEIGHT.number)
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
        val modValue = toModify.value
        return when(modifier) {
            "+"    -> {
                when(toModify.varType) {
                    VarType.ARRAY -> toModify.value = mutableListOf(modValue, value)
                    VarType.STRING -> toModify.value = modValue.toString() + value.toString()
                    VarType.NUMBER -> {
                        val intCastModValue = modValue as Float
                        val intCastValue = value as Float
                        toModify.value = intCastModValue + intCastValue
                    }
                }
            }
            "-"    -> {
                when(toModify.varType) {
                    VarType.ARRAY -> {
                        val listCast = modValue as MutableList<*>
                        repeat(value as Int) {
                            listCast.removeAt(listCast.size)
                        }
                    }
                    VarType.STRING -> throw Exception("Unsupported operation: %s %s".format(toModify.varType, modifier))
                    VarType.NUMBER -> {
                        val intCastModValue = modValue as Float
                        val intCastValue = value as Float
                        toModify.value = intCastModValue - intCastValue
                    }
                }
            }
            "*"    ->
                when(toModify.varType) {
                    VarType.ARRAY ->  throw Exception("Unsupported operation: %s %s".format(toModify.varType, modifier))
                    VarType.STRING -> throw Exception("Unsupported operation: %s %s".format(toModify.varType, modifier))
                    VarType.NUMBER -> {
                        val intCastModValue = modValue as Float
                        val intCastValue = value as Float
                        toModify.value = intCastModValue * intCastValue
                    }
                }
            "/"    ->
                when(toModify.varType) {
                    VarType.ARRAY ->  throw Exception("Unsupported operation: %s %s".format(toModify.varType, modifier))
                    VarType.STRING -> throw Exception("Unsupported operation: %s %s".format(toModify.varType, modifier))
                    VarType.NUMBER -> {
                        val intCastModValue = modValue as Float
                        val intCastValue = value as Float
                        toModify.value = intCastModValue / intCastValue
                    }
                }
            "%"    ->
                when(toModify.varType) {
                    VarType.ARRAY ->  throw Exception("Unsupported operation: %s %s".format(toModify.varType, modifier))
                    VarType.STRING -> throw Exception("Unsupported operation: %s %s".format(toModify.varType, modifier))
                    VarType.NUMBER -> {
                        val intCastModValue = modValue as Float
                        val intCastValue = value as Float
                        toModify.value = intCastModValue % intCastValue
                    }
                }
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