package com.codely.sketch.blocks

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect

/**
 * Created by Daniel on 10/16/2017.
 */
enum class BlockType {
    VAR_DEC, RETURN, IF_ELSE, LOOP, MODIFIER
}

enum class BlockSize(val number:Int) {
    BLOCK_WIDTH(500), BLOCK_HEIGHT(200)
}

interface CodeBlock {
    val type: BlockType
    var rect: Rect
    var connectionPath: Path
    var nextBlock: CodeBlock?
    var parentBlock: CodeBlock?
    var blockPaint: Paint
    var textPaint: Paint
    var pathPaint: Paint

    fun run()
    fun convertToPython()
    fun convertToC()
    fun convertToJava()
    fun convertToJavascript(): String
    fun getBlockText(): String

    // Notifiers
    fun notifyBlockMoved() {
        if (parentBlock != null) {
            parentBlock!!.handleChildMoved(this)
        }

        handleMoved()
    }

    fun notifyDeleted() {
        if (parentBlock != null) {
            if (parentBlock!!.type != BlockType.IF_ELSE) {
                parentBlock!!.nextBlock = null
            } else {
                val parentIfElseBlock: IfElseBlock? = parentBlock as? IfElseBlock
                if (parentIfElseBlock != null) {
                    if (parentIfElseBlock.nextBlock == this) {
                        parentIfElseBlock.nextBlock = null
                    } else if (parentIfElseBlock.elseNextBlock == this) {
                        parentIfElseBlock.elseNextBlock = null
                    }
                }
            }
        }

        if (nextBlock != null) {
            nextBlock!!.parentBlock = null
        }
    }

    // TODO: Generate based off type of block
    fun generateBlockPaint() : Paint {
        val paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 40f
        paint.style = Paint.Style.FILL
        return paint
    }

    // TODO: Generate based off type of block
    fun generatePathPaint() : Paint {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 40f
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.STROKE
        return paint
    }

    fun generateTextPaint() : Paint {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.textSize = 40f
        return paint
    }

    // Handlers
    fun handleMoved() {
        if (nextBlock != null) {
            connectionPath.reset()
            connectionPath.moveTo(rect.exactCenterX(), rect.exactCenterY())
            connectionPath.lineTo(nextBlock!!.rect.exactCenterX(), nextBlock!!.rect.exactCenterY())
        }
    }

    fun handleChildMoved(child: CodeBlock) {
        connectionPath.reset()
        connectionPath.moveTo(rect.exactCenterX(), rect.exactCenterY())
        connectionPath.lineTo(child.rect.exactCenterX(), child.rect.exactCenterY())
    }
}