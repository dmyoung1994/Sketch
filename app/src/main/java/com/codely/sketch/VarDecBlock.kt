package com.codely.sketch

/**
 * Created by Daniel on 10/16/2017.
 */
class VarDecBlock(name: String, x: Float, y: Float) : CodeBlock {
    var varName: String = name

    override val type: BlockType = BlockType.VAR_DEC
    override var nextBlock: CodeBlock? = null
    override var xCord: Float = x
    override var yCord: Float = y
    override var width: Int = 500
    override var height: Int = 500

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

    override fun getBlockText(): String {
        return "var %s = 0".format(varName)
    }
}