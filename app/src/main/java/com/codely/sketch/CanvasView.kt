package com.codely.sketch

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Created by Daniel on 10/11/2017.
 */

class CanvasView : View {
    private var mBitMap: Bitmap? = null
    private var mCanvas: Canvas = Canvas()
    private var mPath: Path = Path()
    private var mPaint: Paint = Paint()
    private var mX: Float = 0f
    private var mY: Float = 0f
    private var mTolerance: Int = 5
    // Used for path expiry
    // private var mMaxPathLen: Int = 5
    // private var codeBlocks: ArrayList = ArrayList

    // Constructors
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        var width: Int = Resources.getSystem().displayMetrics.widthPixels
        var height: Int = Resources.getSystem().displayMetrics.heightPixels - 200

        mPaint.color = Color.BLACK
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 10f

        mBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitMap)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x: Float = event.x
        val y: Float = event.y

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
    private fun startTouch(x: Float, y: Float) {
        mPath.moveTo(x, y)
        mX = x
        mY = y
    }

    // smooths out the path
    private fun moveTouch(x: Float, y: Float) {
        val dx: Float = Math.abs( x - mX )
        val dy: Float = Math.abs( y - mY )
        if ( dx >= mTolerance || dy >= mTolerance) {
            mPath.quadTo( mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    // draws the path
    private fun endTouch() {
        mPath.lineTo( mX, mY )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(mPath, mPaint)
    }

}