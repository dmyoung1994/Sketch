package com.codely.sketch.blocks

import android.graphics.Path
import android.graphics.Rect

/**
 * Created by Daniel on 11/7/2017.
 */

class IfElseBlock(
        private var conditionBlock: VarDecBlock,
        private var comparitor: String,
        private var target: String,
        x: Int,
        y: Int)
    : CodeBlock {
    override val type: BlockType = BlockType.IF_ELSE
    override var rect: Rect = Rect(x, y, x + BlockSize.BLOCK_WIDTH.number, y + BlockSize.BLOCK_HEIGHT.number)

    override var parentBlock: CodeBlock? = null
    override var connectionPath: Path = Path()
    var elseConnectionPath: Path = Path()
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
    var elseNextBlock: CodeBlock? = null
        set(value) {
            field = value
            when (value) {
                null -> elseConnectionPath.reset()
                else -> {
                    elseConnectionPath.moveTo(rect.exactCenterX(), rect.exactCenterY())
                    elseConnectionPath.lineTo(value.rect.exactCenterX(), value.rect.exactCenterY())
                }
            }
        }

    override fun handleMoved() {
        super.handleMoved()
        if (elseNextBlock != null) {
            elseConnectionPath.reset()
            elseConnectionPath.moveTo(rect.exactCenterX(), rect.exactCenterY())
            elseConnectionPath.lineTo(elseNextBlock!!.rect.exactCenterX(), elseNextBlock!!.rect.exactCenterY())
        }
    }

    override fun handleChildMoved(child: CodeBlock) {
        if (child == nextBlock) {
            super.handleChildMoved(child)
        } else {
            elseConnectionPath.reset()
            elseConnectionPath.moveTo(rect.exactCenterX(), rect.exactCenterY())
            elseConnectionPath.lineTo(child.rect.exactCenterX(), child.rect.exactCenterY())
        }
    }

    override fun notifyDeleted() {
        super.notifyDeleted()
        if (elseNextBlock != null) {
            elseNextBlock!!.parentBlock = null
        }
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
                .format(conditionBlock.varName, translateComparitor(comparitor), target,
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
        val compareSym = translateComparitor(comparitor)
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
        return "If %s %s %s than...Otherwise...".format(conditionBlock.varName, comparitor, target)
    }
}