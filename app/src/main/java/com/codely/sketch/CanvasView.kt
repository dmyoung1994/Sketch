package com.codely.sketch

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Looper
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.*
import com.codely.sketch.blocks.ReturnBlock
import com.codely.sketch.blocks.*
import com.codely.sketch.lines.LinePath
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.timerTask
import kotlin.concurrent.thread
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
    // TODO: Move block paint into the blocks themselves
    private var mBlockPaint: Paint = Paint()
    private var mTextPaint: Paint = Paint()
    private var mArrowPaint: Paint = Paint()
    private var mX: Int = 0
    private var mY: Int = 0
    private var mTolerance: Int = 5
    private var mBlockSelectionTolerance: Int = BlockSize.BLOCK_HEIGHT.number / 2
    private var selectedBlock: CodeBlock? = null
    private var executionBlock: CodeBlock? = null
    private var mFadeTimer: Timer = Timer()
    private var mScaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private var mScaleFactor: Float = 1f
    private val fadeStep = 20
    private val emptyPath = Path()

    var drawnLines: BlockingQueue<LinePath> = LinkedBlockingQueue()
    private val stateMachine: CodeStateMachine = CodeStateMachine.getInstance()

    // Constructors
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val width: Int = Resources.getSystem().displayMetrics.widthPixels
        val height: Int = Resources.getSystem().displayMetrics.heightPixels

        mPaint.color = Color.WHITE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 1f

        mBlockPaint.color = Color.RED
        mBlockPaint.strokeWidth = 40f
        mBlockPaint.style = Paint.Style.FILL

        mTextPaint.color = Color.BLACK
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.textSize = 40f

        mArrowPaint.color = Color.BLACK
        mArrowPaint.style = Paint.Style.STROKE
        mArrowPaint.strokeWidth = 20f

        mBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        scheduleFadeTimer()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitMap)

    }

    private fun defaultPaint(): Paint {
        var paint = Paint()
        paint.color = Color.BLACK
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f

        return paint
    }


    /**
     * Interface Handlers
     */

    fun handleRunButtonClick() {
            if (stateMachine.codeBlocks.size != 0) {
                thread(true) {
                    Looper.prepare()
                    stateMachine.codeBlocks.elementAt(0).run()
                    val resultDialog: AlertDialog.Builder = AlertDialog.Builder(this.context)
                    resultDialog.setMessage(stateMachine.terminatorBlock?.value.toString())
                            .setTitle("Code Result")
                            .show()
    //            Toast.makeText(context, stateMachine.terminatorBlock?.value.toString(), Toast.LENGTH_LONG).show()
                    Looper.loop()
                }
            } else {
                val resultDialog: AlertDialog.Builder = AlertDialog.Builder(this.context)
                resultDialog.setMessage("Drag some blocks onto the canvas to run the code!")
                    .setTitle("No Blocks!")
                    .show()
        }
    }

    fun handleVarDecButtonClick() {
        val varDecDialog: AlertDialog.Builder = AlertDialog.Builder(this.context)
        val input = EditText(this.context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "Enter a variable name"
        varDecDialog.setView(input)
                .setTitle("Declare a variable")
                .setPositiveButton("OK") { _, _ ->
                    val varName = input.text.toString()
                    val varDecBlock = VarDecBlock(varName, width / 2, height / 2)
                    // TODO: Add error checking
                    stateMachine.varNames[varName] = varDecBlock
                    stateMachine.codeBlocks.add(varDecBlock)
                    if (executionBlock == null ) executionBlock = varDecBlock
                    input.requestFocus()
                    invalidate()
                }
                .setNegativeButton("Cancel") { d, _ ->
                    d.cancel()
                }
                .create()
                .show()
    }

    fun handleModifyButtonClick() {
        val modifyDialog: AlertDialog.Builder = AlertDialog.Builder(this.context)
        val layoutGroup = LinearLayout(this.context)
        layoutGroup.orientation = LinearLayout.VERTICAL

        // Build data set for dropdown
        val varSpinner = Spinner(this.context)
        val spinnerArray: List<String> = ArrayList(stateMachine.varNames.keys)
        val varAdapter: ArrayAdapter<String> = ArrayAdapter(this.context, android.R.layout.simple_spinner_item, spinnerArray)
        varAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        varSpinner.adapter = varAdapter
        varSpinner.prompt = "What do you want to modify?"

        // Data Field for modifiers
        val modifySpinner = Spinner(this.context)
        val modifyAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this.context, R.array.modifiers, android.R.layout.simple_spinner_item)
        modifyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modifySpinner.adapter = modifyAdapter
        modifySpinner.prompt = "How should we modify it?"

        // Data field for condition target
        val input = EditText(this.context)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "By what?"

        layoutGroup.addView(varSpinner)
        layoutGroup.addView(modifySpinner)
        layoutGroup.addView(input)

        modifyDialog.setView(layoutGroup)
                .setTitle("Set up a condition")
                .setPositiveButton("OK") { _, _ ->
                    val varBlock = stateMachine.varNames[varSpinner.selectedItem.toString()]
                    val modifier = modifySpinner.selectedItem.toString()
                    val value = input.text.toString().toInt()
                    val modifyBlock = ModifyBlock(varBlock!!, modifier, value, width/2, height/2)
                    stateMachine.codeBlocks.add(modifyBlock)
                    if (executionBlock == null ) executionBlock = modifyBlock
                    invalidate()
                }
                .setNegativeButton("Cancel") { d, _ ->
                    d.cancel()
                }
                .create()
                .show()
    }

    fun handlePrintButtonClicked() {
        val printDialog: AlertDialog.Builder = AlertDialog.Builder(this.context)
        // Build data set for our dropdown
        val varSpinner = Spinner(this.context)
        val spinnerArray: List<String> = ArrayList(stateMachine.varNames.keys)
        val varAdapter: ArrayAdapter<String> = ArrayAdapter(this.context, android.R.layout.simple_spinner_item, spinnerArray)
        varAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        varSpinner.adapter = varAdapter
        varSpinner.prompt = "What do you want to print?"

        printDialog.setView(varSpinner)
                .setTitle("Print a variable")
                .setPositiveButton("OK") { _, _ ->
                    val varBlock = stateMachine.varNames[varSpinner.selectedItem.toString()]
                    val returnBlock = ReturnBlock(varBlock!!, width / 2, height / 2)
                    stateMachine.codeBlocks.add(returnBlock)
                    if (executionBlock == null ) executionBlock = returnBlock
                    invalidate()
                }
                .setNegativeButton("Cancel") { d, _ ->
                    d.cancel()
                }
                .create()
                .show()
    }

    fun handleIfElseButtonClick() {
        val ifElseDialog: AlertDialog.Builder = AlertDialog.Builder(this.context)
        val layoutGroup = LinearLayout(this.context)
        layoutGroup.orientation = LinearLayout.VERTICAL

        // Build data set for our dropdown
        val varSpinner = Spinner(this.context)
        val spinnerArray: List<String> = ArrayList(stateMachine.varNames.keys)
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
                .setPositiveButton("OK") { _, _ ->
                    val conditionBlock = stateMachine.varNames[varSpinner.selectedItem.toString()]
                    val comparator = compareSpinner.selectedItem.toString()
                    val target = input.text.toString()
                    val ifElseBlock = IfElseBlock(conditionBlock!!, comparator, target, width/2, height/2)
                    stateMachine.codeBlocks.add(ifElseBlock)
                    if (executionBlock == null ) executionBlock = ifElseBlock
                    invalidate()
                }
                .setNegativeButton("Cancel") { d, _ ->
                    d.cancel()
                }
                .create()
                .show()
    }

    /**
     * Helper Functions
     */
    private fun isDeletingBlock(codeBlock: CodeBlock): Boolean {
        val trashcan: View = rootView.findViewById(R.id.trashCan)
        val blockRect: Rect = codeBlock.rect
        val trashRect = Rect(trashcan.left, trashcan.top, trashcan.right, trashcan.bottom)
        return Rect.intersects(blockRect, trashRect)
    }

    private fun toggleTrashCan() {
        val trashCan: View = rootView.findViewById(R.id.trashCan)
        val visibility: Int = trashCan.visibility
        when ( visibility ) {
            View.VISIBLE -> {
                // TODO: fade down
                trashCan.visibility = View.INVISIBLE
            }

            View.INVISIBLE -> {
                // TODO: fade up
                trashCan.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Canvas Interaction
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x: Int = event.x.toInt()
        val y: Int = event.y.toInt()



        mScaleGestureDetector.onTouchEvent(event)

        when( event.action ) {
            MotionEvent.ACTION_DOWN -> startTouch(x, y)
            MotionEvent.ACTION_MOVE -> moveTouch(x, y)
            MotionEvent.ACTION_UP   -> endTouch()
        }

        return true
    }

    // called when a touch down even it set
    private fun startTouch(x: Int, y: Int) {
        val touchedBlock: CodeBlock? = getTouchedBlock(x, y)

        // if we didn't touch a block, just move the path
        when(touchedBlock) {
            null -> {
                val linePath = LinePath()
                mPath = Path()
                linePath.setPath(mPath)
                linePath.setPaint(defaultPaint())

                mPath.reset()
                mPath.moveTo(x.toFloat(), y.toFloat())

                drawnLines.add(linePath)
            }
            else -> {
                selectedBlock = touchedBlock
                toggleTrashCan()
            }
        }

        mX = x
        mY = y
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            if (detector != null) {
                mScaleFactor *= detector.scaleFactor
                mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10f))
                invalidate()
            }

            return true
        }
    }

    // draws the path
    private fun endTouch() {
        when (selectedBlock) {
            null         -> {
                mPath.lineTo( mX.toFloat(), mY.toFloat() )
                for (p: LinePath in drawnLines) {
                    p.setCanAnimate(true)
                }
            }
            is CodeBlock -> {
                toggleTrashCan()
                if (isDeletingBlock(selectedBlock!!)) {
                    stateMachine.codeBlocks.remove(selectedBlock!!)
                    selectedBlock!!.notifyDeleted()
                }

                selectedBlock = null
            }
        }
    }

    private fun moveTouch(x: Int, y: Int) {
        val distX: Int = abs( x - mX )
        val distY: Int = abs( y - mY )
        // If intent is to actually move
        if ( distX >= mTolerance || distY >= mTolerance) {
            // we're dragging a block
            when( selectedBlock ) {
                null -> {
                    mPath.quadTo( mX.toFloat(), mY.toFloat(), (x + mX).toFloat() / 2, (y + mY).toFloat() / 2 )
                    // TODO: Extract to function
                    var closestBlock: CodeBlock? = executionBlock
                    for (block: CodeBlock in stateMachine.codeBlocks) {
                        val bX = block.rect.centerX()
                        val bY = block.rect.centerY()

                        if ( closestBlock == block ) {
                            continue
                        }

                        if ( closestBlock != null ) {
                            // only care if we're close enough to be considered a selection
                            if (abs(x - bX) >= mBlockSelectionTolerance
                                    || abs( y - bY) >= mBlockSelectionTolerance) {
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
                            when (executionBlock) {
                                is IfElseBlock -> {
                                    val ifElseBlock = executionBlock as IfElseBlock
                                    if (ifElseBlock.nextBlock != null) {
                                        ifElseBlock.elseNextBlock = closestBlock
                                    } else {
                                        ifElseBlock.nextBlock = closestBlock
                                    }
                                }
                                is ReturnBlock -> {}
                                else -> {
                                    executionBlock!!.nextBlock = closestBlock
                                }
                            }
                            if (closestBlock!!.parentBlock == null) {
                                closestBlock.parentBlock = executionBlock
                            }
                        }
                        executionBlock = closestBlock
                    }
                }

                is CodeBlock -> {
                    val dx = x - mX
                    val dy = y - mY
                    val blockX = selectedBlock!!.rect.left
                    val blockY = selectedBlock!!.rect.top
                    val blockWidth = selectedBlock!!.rect.width()
                    val blockHeight = selectedBlock!!.rect.height()
                    // Only move the rect if we're not going off the screen
                    if ( (blockX + dx >= -40 && blockX + blockWidth + dx <= width + 40)
                            && (blockY + dy >= -40 && blockY + blockHeight + dy <= height + 40) ) {
                        selectedBlock!!.rect.left = blockX + dx
                        selectedBlock!!.rect.top = blockY + dy
                        selectedBlock!!.rect.right = selectedBlock!!.rect.left + BlockSize.BLOCK_WIDTH.number
                        selectedBlock!!.rect.bottom = selectedBlock!!.rect.top + BlockSize.BLOCK_HEIGHT.number
                        selectedBlock!!.notifyBlockMoved()
                    }
                }
            }

            mX = x
            mY = y
        }
    }

    private fun getTouchedBlock(x: Int, y: Int): CodeBlock? {
        for (block: CodeBlock in stateMachine.codeBlocks) {
            if (block.rect.contains(x, y)) {
                return block
            }
        }

        // If we didn't find anything return null
        return null
    }

    private fun scheduleFadeTimer() {
        mFadeTimer.scheduleAtFixedRate(timerTask {
            for (path: LinePath in drawnLines) {
                if (path.canAnimate()) {
                    var currentAlpha: Int = path.getPaint().alpha
                    currentAlpha -= fadeStep

                    path.setAlpha(currentAlpha)
                    path.getPaint().alpha = currentAlpha

                    if (currentAlpha <= 0) {
                        drawnLines.remove(path)
                    }
                }
            }
        }, 0, 100)
    }

    // Main Canvas Loop
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            canvas.save()
            canvas.scale(mScaleFactor, mScaleFactor)
            for (path: LinePath in drawnLines) {
                canvas.drawPath(path.getPath(), path.getPaint())
            }
            for (block: CodeBlock in stateMachine.codeBlocks) {

                // Custom connection draw logic
                when(block) {
                    is IfElseBlock -> {
                        if (!block.elseConnectionPath.isEmpty) {
                            canvas.drawPath(block.elseConnectionPath, block.pathPaint)
                        }
                    }
                }

                if (!block.connectionPath.isEmpty) {
                    canvas.drawPath(block.connectionPath, block.pathPaint)
                }

                // Draw the block
                canvas.drawRect(block.rect, block.blockPaint)
                // Draw its text
                canvas.drawText(block.getBlockText(), block.rect.left.toFloat(), block.rect.top.toFloat(), block.textPaint)
            }
            canvas.restore()
        }

    }

}