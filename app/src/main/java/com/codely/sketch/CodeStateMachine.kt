package com.codely.sketch

import com.codely.sketch.blocks.CodeBlock
import com.codely.sketch.blocks.IfElseBlock
import com.codely.sketch.blocks.ReturnBlock
import com.codely.sketch.blocks.VarDecBlock
import kotlin.math.abs

class CodeStateMachine private constructor() {
    var codeBlocks: ArrayList<CodeBlock> = ArrayList()
    var varNames: HashMap<String, VarDecBlock> = HashMap()
    var terminatorBlock: VarDecBlock? = null
    var selectedBlock: CodeBlock? = null
    var executionBlock: CodeBlock? = null

    fun deleteBlock(block: CodeBlock) {
        when (block) {
            is VarDecBlock -> {
                varNames.remove(block.varName)
            }
        }

        codeBlocks.remove(block)
    }

    fun getClosestBlock(x: Int, y: Int, toleranceX: Int, toleranceY: Int) : CodeBlock? {
        var closestBlock: CodeBlock? = executionBlock
        for (block: CodeBlock in stateMachine.codeBlocks) {
            val bX = block.rect.centerX()
            val bY = block.rect.centerY()

            if ( closestBlock == block ) {
                continue
            }

            if ( closestBlock != null ) {
                // only care if we're close enough to be considered a selection
                if (abs(x - bX) >= toleranceX || abs( y - bY) >= toleranceY) {
                    continue
                }

                if ( abs(x - bX) < abs(closestBlock.rect.centerX() - bX)
                        && abs(y - bY) < abs(closestBlock.rect.centerX() - bY)) {
                    closestBlock = block
                }
            }
        }

        return closestBlock
    }

    fun connectBlocks(block1: CodeBlock, block2: CodeBlock) {
        if (block2 != block1 ) {
            when (block1) {
                is IfElseBlock -> {
                    if (block1.nextBlock != null) {
                        block1.elseNextBlock = block2
                    } else {
                        block1.nextBlock = block2
                    }
                }
                is ReturnBlock -> {}
                else -> {
                    block1.nextBlock = block2
                }
            }
            if (block2.parentBlock == null) {
                block2.parentBlock = block1
            }
            executionBlock = block2
        }
    }

    companion object {
        private val stateMachine = CodeStateMachine()

        fun getInstance(): CodeStateMachine {
            return stateMachine
        }
    }
}