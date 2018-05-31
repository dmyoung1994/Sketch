package com.codely.sketch.blocks

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

    fun run()
    fun convertToPython()
    fun convertToC()
    fun convertToJava()
    fun convertToJavascript(): String
    fun getBlockText(): String

    // Notifiers
    fun notifyBlockMoved() {
        if (parentBlock != null) {
            parentBlock!!.handleChildMoved(rect)
        }

        handleMoved()
    }

    fun notifyDeleted() {
        if (parentBlock != null) {
            parentBlock!!.nextBlock = null
        }
    }

    // Handlers
    fun handleMoved() {
        if (nextBlock != null) {
            connectionPath.reset()
            connectionPath.moveTo(rect.exactCenterX(), rect.exactCenterY())
            connectionPath.lineTo(nextBlock!!.rect.exactCenterX(), nextBlock!!.rect.exactCenterY())
        }
    }

    fun handleChildMoved(childRect: Rect) {
        connectionPath.reset()
        connectionPath.moveTo(rect.exactCenterX(), rect.exactCenterY())
        connectionPath.lineTo(childRect.exactCenterX(), childRect.exactCenterY())
    }
}