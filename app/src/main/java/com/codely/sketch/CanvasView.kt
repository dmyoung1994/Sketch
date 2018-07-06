package com.codely.sketch

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.codely.sketch.blocks.*
import com.codely.sketch.lines.LinePath
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.timerTask
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
    private var mX: Int = 0
    private var mY: Int = 0
    private var mTolerance: Int = 5
    private var mBlockSelectionToleranceX: Int = BlockSize.BLOCK_HEIGHT.number / 2
    private var mBlockSelectionToleranceY: Int = BlockSize.BLOCK_WIDTH.number / 2
    private var mFadeTimer: Timer = Timer()
    private var mScaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private var mScaleFactor: Float = 1f
    private val fadeStep = 20

    var drawnLines: BlockingQueue<LinePath> = LinkedBlockingQueue()
    private val stateMachine: CodeStateMachine = CodeStateMachine.getInstance()

    // Constructors
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val width: Int = Resources.getSystem().displayMetrics.widthPixels
        val height: Int = Resources.getSystem().displayMetrics.heightPixels

        mBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        scheduleFadeTimer()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitMap)

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

    private fun defaultPaint(): Paint {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f

        return paint
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
                stateMachine.selectedBlock = touchedBlock
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
        when (stateMachine.selectedBlock) {
            is CodeBlock -> {
                toggleTrashCan()
                if (isDeletingBlock(stateMachine.selectedBlock!!)) {
                    if (stateMachine.executionBlock == stateMachine.selectedBlock) {
                        stateMachine.executionBlock = stateMachine.selectedBlock!!.parentBlock
                    }
                    stateMachine.deleteBlock(stateMachine.selectedBlock!!)
                    stateMachine.selectedBlock!!.notifyDeleted()
                }

                stateMachine.selectedBlock = null
            }

            null -> {
                mPath.lineTo( mX.toFloat(), mY.toFloat() )
                for (p: LinePath in drawnLines) {
                    p.setCanAnimate(true)
                }
            }
        }
    }

    private fun moveTouch(x: Int, y: Int) {
        val distX: Int = abs( x - mX )
        val distY: Int = abs( y - mY )
        // If intent is to actually move
        if ( distX >= mTolerance || distY >= mTolerance) {
            // we're dragging a block
            when(stateMachine.selectedBlock) {
                is CodeBlock -> {
                    val dx = x - mX
                    val dy = y - mY
                    val blockX = stateMachine.selectedBlock!!.rect.left
                    val blockY = stateMachine.selectedBlock!!.rect.top
                    val blockWidth = stateMachine.selectedBlock!!.rect.width()
                    val blockHeight = stateMachine.selectedBlock!!.rect.height()
                    // Only move the rect if we're not going off the screen
                    if ( (blockX + dx >= -40 && blockX + blockWidth + dx <= width + 40)
                            && (blockY + dy >= -40 && blockY + blockHeight + dy <= height + 40) ) {
                        moveBlock(stateMachine.selectedBlock!!, blockX + dx, blockY + dy)
                    }
                }

                null -> {
                    mPath.quadTo( mX.toFloat(), mY.toFloat(), (x + mX).toFloat() / 2, (y + mY).toFloat() / 2 )
                    val closestBlock = stateMachine.getClosestBlock(x, y, mBlockSelectionToleranceX, mBlockSelectionToleranceY)
                    if (closestBlock != null && stateMachine.executionBlock != null) {
                        stateMachine.connectBlocks(stateMachine.executionBlock!!, closestBlock)
                    }
                }
            }

            mX = x
            mY = y
        }
    }

    private fun moveBlock(block: CodeBlock, x: Int, y: Int) {
        block.rect.left = x
        block.rect.top = y
        block.rect.right = stateMachine.selectedBlock!!.rect.left + BlockSize.BLOCK_WIDTH.number
        block.rect.bottom = stateMachine.selectedBlock!!.rect.top + BlockSize.BLOCK_HEIGHT.number
        block.notifyBlockMoved()
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