package com.codely.sketch

import com.codely.sketch.blocks.CodeBlock
import com.codely.sketch.blocks.VarDecBlock

class CodeStateMachine private constructor() {
    var codeBlocks: ArrayList<CodeBlock> = ArrayList()
    var varNames: HashMap<String, VarDecBlock> = HashMap()
    var terminatorBlock: VarDecBlock? = null

    companion object {
        private val stateMachine = CodeStateMachine()

        fun getInstance(): CodeStateMachine {
            return stateMachine
        }
    }
}