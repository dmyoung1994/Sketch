package com.codely.sketch

import android.graphics.Path
import android.graphics.Rect

/**
 * Created by Daniel on 11/7/2017.
 */

class IfElseBlock(conditionBlock: VarDecBlock, compare: String, target: String, x: Int, y: Int) : CodeBlock {
    override val type: BlockType = BlockType.IF_ELSE
    override var rect: Rect = Rect(x, y, x + BlockSize.BLOCK_WIDTH.number, y + BlockSize.BLOCK_HEIGHT.number)

    private var conditionBlock = conditionBlock
    private var compare = compare
    private var target = target

    override var connectionPath: Path = Path()
    override var nextBlock: CodeBlock? = null
        set(value) {
            field = value
            connectionPath.moveTo(rect.exactCenterX(), rect.exactCenterY())
            connectionPath.lineTo(value!!.rect.exactCenterX(), value.rect.exactCenterY())
        }
    var elseNextBlock: CodeBlock? = null
        set(value) {
            field = value
            connectionPath.moveTo(rect.exactCenterX(), rect.exactCenterY())
            connectionPath.lineTo(value!!.rect.exactCenterX(), value.rect.exactCenterY())
        }

    private fun translateComparitor(compare: String): String {
        return when(compare) {
            "is equal to"                 -> "=="
            "is not equal to"             -> "!="
            "is less than"                -> "<"
            "is greater than"             -> ">"
            "is greater than or equal to" -> ">="
            "is less than or equal to"    -> "<="
            else                          -> ""
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
        return "if (%s %s %s) { %s } else { %s }"
                .format(conditionBlock.varName, translateComparitor(compare), target,
                        nextBlock?.convertToJavascript(), elseNextBlock?.convertToJavascript())
    }

    private fun runHelper(isTrue: Boolean) {
        if (isTrue) {
            nextBlock?.run()
        } else {
            elseNextBlock?.run()
        }
    }

    override fun run() {
        val compareSym = translateComparitor(compare)
        when (compareSym) {
            "==" -> runHelper(conditionBlock.value.toString() == target)
            "!=" -> runHelper(conditionBlock.value.toString() != target)
            "<" -> runHelper(conditionBlock.value.toString() < target)
            ">" -> runHelper(conditionBlock.value.toString() > target)
            "<=" -> runHelper(conditionBlock.value.toString() <= target)
            ">=" -> runHelper(conditionBlock.value.toString() >= target)
        }

    }

    override fun getBlockText(): String {
        return "If %s %s %s than...Otherwise...".format(conditionBlock.varName, compare, target)
    }
}