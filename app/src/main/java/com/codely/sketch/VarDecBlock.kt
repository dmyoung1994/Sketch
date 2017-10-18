package com.codely.sketch

/**
 * Created by Daniel on 10/16/2017.
 */
class VarDecBlock: CodeBlock {
    var varName: String = ""

    override val type: BlockType
        get() = BlockType.VAR_DEC

    override var nextBlock: CodeBlock? = null

    constructor(name: String) {
        varName = name
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

    override fun run() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}