package com.codely.sketch.blocks

import android.graphics.Path
import android.graphics.Rect
import com.codely.sketch.CodeStateMachine

class ReturnBlock(private var returnBlock: VarDecBlock, x: Int, y: Int) : CodeBlock {
    override val type: BlockType = BlockType.RETURN
    override var connectionPath: Path = Path()
    override var rect: Rect = Rect(x, y, x + BlockSize.BLOCK_WIDTH.number, y + BlockSize.BLOCK_HEIGHT.number)
    override var nextBlock: CodeBlock? = null
        set(value) {
            field = value
            connectionPath.moveTo(rect.exactCenterX(), rect.exactCenterY())
            connectionPath.lineTo(value!!.rect.exactCenterX(), value.rect.exactCenterY())
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
        return "Return %s;".format(returnBlock.varName)
    }

    override fun run() {
        CodeStateMachine.getInstance().terminatorBlock = returnBlock
    }

    override fun getBlockText(): String {
        return "Return %s".format(returnBlock.varName)
    }
}