package com.codely.sketch

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import kotlin.math.abs

/**
 * Created by Daniel on 10/11/2017.
 */

// TODO: Take out the codeBlocks and put them into a model.
// TODO: Make this class the ViewController
class CanvasView : View {
    private var mBitMap: Bitmap? = null
    private var mCanvas: Canvas = Canvas()
    private var mPath: Path = Path()
    private var mPaint: Paint = Paint()
    private var mBlockPaint: Paint = Paint()
    private var mTextPaint: Paint = Paint()
    private var mX: Int = 0
    private var mY: Int = 0
    private var mTolerance: Int = 5
    private var mBlockSelectionTolerance: Int = 50
    private var codeBlocks: HashSet<CodeBlock> = HashSet()
    private var varNames: HashMap<String, VarDecBlock> = HashMap()
    private var selectedBlock: CodeBlock? = null
    private var executionBlock: CodeBlock? = null

    // Constructors
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val width: Int = Resources.getSystem().displayMetrics.widthPixels
        val height: Int = Resources.getSystem().displayMetrics.heightPixels - 200

        mPaint.color = Color.BLACK
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 10f

        mBlockPaint.color = Color.RED
        mBlockPaint.strokeWidth = 40f
        mBlockPaint.style = Paint.Style.FILL

        mTextPaint.color = Color.BLACK
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.textSize = 40f

        mBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun handleVarDecButtonClick() {
        val varDecDialog: AlertDialog.Builder = AlertDialog.Builder(this.context)
        val input = EditText(this.context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "Enter a variable name"
        varDecDialog.setView(input)
            .setTitle("Declare a variable")
            .setPositiveButton("OK", { _, _ ->
                val varName = input.text.toString()
                val varDecBlock = VarDecBlock(varName, width/2, height/2)
                // TODO: Add error checking
                varNames.put(varName, varDecBlock)
                codeBlocks.add(varDecBlock)
                if (executionBlock == null ) executionBlock = varDecBlock
            })
            .setNegativeButton("Cancel", { d, _ ->
                d.cancel()
            })
            .show()
        input.requestFocus()
    }

    fun handlePrintButtonClicked() {
        val printDialog: AlertDialog.Builder = AlertDialog.Builder(this.context)

        // Build data set for our dropdown
        val varSpinner = Spinner(this.context)
        val spinnerArray: List<String> = ArrayList(varNames.keys)
        val varAdapter: ArrayAdapter<String> = ArrayAdapter(this.context, android.R.layout.simple_spinner_item, spinnerArray)
        varAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        varSpinner.adapter = varAdapter
        varSpinner.prompt = "What do you want to print?"

        printDialog.setView(varSpinner)
            .setTitle("Print a variable")
            .setPositiveButton("OK", { _, _ ->
                val varBlock = varNames[varSpinner.selectedItem.toString()]
                val printBlock = PrintBlock(varBlock!!, width/2, height/2)
                codeBlocks.add(printBlock)
                if (executionBlock == null ) executionBlock = printBlock
            })
            .setNegativeButton("Cancel", { d, _ ->
                d.cancel()
            })
            .show()
    }

    fun handleIfElseButtonClick() {
        val ifElseDialog: AlertDialog.Builder = AlertDialog.Builder(this.context)
        val layoutGroup = LinearLayout(this.context)
        layoutGroup.orientation = LinearLayout.VERTICAL

        // Build data set for our dropdown
        val varSpinner = Spinner(this.context)
        val spinnerArray: List<String> = ArrayList(varNames.keys)
        val varAdapter: ArrayAdapter<String> = ArrayAdapter(this.context, android.R.layout.simple_spinner_item, spinnerArray)
        varAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        varSpinner.adapter = varAdapter
        varSpinner.prompt = "What variable should we compare?"

        // Data Field for conditionals
        val compareSpinner = Spinner(this.context)
        val compareAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this.context, R.array.comparators, android.R.layout.simple_spinner_item)
        compareAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        compareSpinner.adapter = compareAdapter
        compareSpinner.prompt = "How should we compare it?"

        // Data field for condition target
        val input = EditText(this.context)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "What value should we compare it to?"

        layoutGroup.addView(varSpinner)
        layoutGroup.addView(compareSpinner)
        layoutGroup.addView(input)


        ifElseDialog.setView(layoutGroup)
                .setTitle("Set up a condition")
                .setPositiveButton("OK", { _, _ ->
                    val conditionBlock = varNames[varSpinner.selectedItem.toString()]
                    val comparator = compareSpinner.selectedItem.toString()
                    val target = input.text.toString()
                    val ifElseBlock = IfElseBlock(conditionBlock!!, comparator, target, width/2, height/2)
                    codeBlocks.add(ifElseBlock)
                    if (executionBlock == null ) executionBlock = ifElseBlock
                })
                .setNegativeButton("Cancel", { d, _ ->
                    d.cancel()
                })
                .show()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitMap)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x: Int = event.x.toInt()
        val y: Int = event.y.toInt()

        when( event.action ) {
            MotionEvent.ACTION_DOWN -> startTouch(x, y)
            MotionEvent.ACTION_MOVE -> moveTouch(x, y)
            MotionEvent.ACTION_UP   -> endTouch()
        }

        // cause a redraw
        invalidate()
        return true
    }

    // called when a touch down even it set
    private fun startTouch(x: Int, y: Int) {
        val touchedBlock: CodeBlock? = getTouchedBlock(x, y)

        // if we didn't touch a block, just move the path
        when(touchedBlock) {
            null -> mPath.moveTo(x.toFloat(), y.toFloat())
            else -> {selectedBlock = touchedBlock}
        }

        mX = x
        mY = y
    }

    // smooths out the path
    private fun moveTouch(x: Int, y: Int) {
        val distX: Int = abs( x - mX )
        val distY: Int = abs( y - mY )
        // If intent is to actually move
        if ( distX >= mTolerance || distY >= mTolerance) {
            // we're dragging a block
            when( selectedBlock ) {
                null -> {
                    mPath.quadTo( mX.toFloat(), mY.toFloat(), (x + mX).toFloat() / 2, (y + mY).toFloat() / 2)
                    // TODO: Find closest block to finger
                    var closestBlock: CodeBlock? = executionBlock
                    for (block: CodeBlock in codeBlocks) {
                        val bX = block.rect.centerX()
                        val bY = block.rect.centerY()

                        if ( closestBlock != null ) {
                            // only care if we're close enough to be considered a selection
                            if (abs(x - bX) >= mBlockSelectionTolerance
                                    && abs( y - bY) >= mBlockSelectionTolerance) {
                                continue
                            }

                            if ( abs(x - bX) < abs(closestBlock.rect.centerX() - bX)
                                    && abs(y - bY) < abs(closestBlock.rect.centerX() - bY)) {
                                closestBlock = block
                            }
                        }
                    }

                    if (closestBlock != executionBlock ) {
                        if (executionBlock != null) {
                            executionBlock!!.nextBlock = closestBlock
                        }

                        executionBlock = closestBlock

                        print("Changing closest block")
                    }
                }

                is CodeBlock    -> {
                    val dx = x - mX
                    val dy = y - mY
                    val blockX = selectedBlock!!.rect.left
                    val blockY = selectedBlock!!.rect.top
                    val blockWidth = selectedBlock!!.rect.width()
                    val blockHeight = selectedBlock!!.rect.height()
                    // Only move the rect if we're not going off the screen
                    if ( (blockX + dx >= 0 && blockX + blockWidth + dx <= width)
                            && (blockY + dy >= 0 && blockY + blockHeight + dy <= height)) {
                        selectedBlock!!.rect.left = blockX + dx
                        selectedBlock!!.rect.top = blockY + dy
                        selectedBlock!!.rect.right = selectedBlock!!.rect.left + BlockSize.BLOCK_WIDTH.number
                        selectedBlock!!.rect.bottom = selectedBlock!!.rect.top + BlockSize.BLOCK_HEIGHT.number
                    }
                }
            }

            mX = x
            mY = y
        }
    }

    // draws the path
    private fun endTouch() {
        when (selectedBlock) {
            null            -> mPath.lineTo( mX.toFloat(), mY.toFloat() )
            is CodeBlock    -> {
                selectedBlock = null
            }
        }
    }

    private fun getTouchedBlock(x: Int, y: Int): CodeBlock? {
        for (block: CodeBlock in codeBlocks) {
            if (block.rect.contains(x, y)) {
                Log.d("DEBUG", "Registered touch in block: %s".format(block.type))
                return block
            }
        }

        // If we didn't find anything return null
        return null
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(mPath, mPaint)
        for (block: CodeBlock in codeBlocks) {
            // Draw the block
            canvas?.drawRect(block.rect, mBlockPaint)
            // Draw its text
            canvas?.drawText(block.getBlockText(), block.rect.left.toFloat(), block.rect.top.toFloat(), mTextPaint)
        }
    }

}