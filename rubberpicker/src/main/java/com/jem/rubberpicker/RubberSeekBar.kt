package com.jem.rubberpicker

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.animation.ValueAnimator
import android.graphics.*
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.absoluteValue


class RubberSeekBar : View {

    companion object {
        private val drawableThumbRadius: Float = 60f
    }

    private val paint: Paint by lazy {
        val tempPaint = Paint()
        tempPaint.style = Paint.Style.STROKE
        tempPaint.color = Color.GRAY
        tempPaint.strokeWidth = 5f
        tempPaint
    }
    private val highlightPaint: Paint by lazy {
        val tempPaint = Paint()
        tempPaint.style = Paint.Style.FILL
        tempPaint.color = Color.parseColor("#38ACEC")
        tempPaint
    }
    private var path: Path = Path()
    private var valueAnimator: ValueAnimator? = null
    private var controlX: Float = width.toFloat()/2
    private var controlY: Float = height.toFloat()/2
    private val trackStartX: Float
        get() {
            return if (drawableThumb != null) {
                setDrawableHalfWidthAndHeight()
                drawableWidthBy2.toFloat()
            } else {
                drawableThumbRadius
            }
        }
    private val trackEndX: Float
        get() {
            return if (drawableThumb != null) {
                setDrawableHalfWidthAndHeight()
                width - drawableWidthBy2.toFloat()
            } else {
                width - drawableThumbRadius
            }
        }
    private val trackY: Float
        get() {
            return height.toFloat()/2
        }

    private var x1: Float = 0f
    private var y1: Float = 0f
    private var x2: Float = 0f
    private var y2: Float = 0f

    private var stretchRange: Float = -1f

    private var elasticBehavior: ElasticBehavior = ElasticBehavior.cubic

    private var drawableThumb: Drawable? = null
    private var drawableThumbSelected: Boolean = false

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs)
    constructor(context: Context) :
            super(context)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if(controlX<trackStartX) {
            controlX = trackStartX
        }
        controlY = height.toFloat() / 2
        if (stretchRange==-1f) {
            this.stretchRange = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16F,
                context.resources.displayMetrics
            )
        }
        this.stretchRange = this.stretchRange.coerceAtMost(height.toFloat()/2)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawTrack(canvas)
        drawThumb(canvas)
        //TODO - Use SpringAnimation & SpringForce instead of ValueAnimator
        //TODO - Assign min, max values and get 'seekbar' values
        //TODO - Expand logic to RubberRangePicker
    }

    private fun drawThumb(canvas: Canvas?) {
        if (drawableThumb != null) {
            canvas?.let {
                canvas.translate(controlX, controlY)
                drawableThumb?.draw(it)
            }
        } else {
            highlightPaint.color = 0xFF38ACEC.toInt()
            canvas?.drawCircle(controlX, controlY, drawableThumbRadius, highlightPaint)
            if (drawableThumbSelected) {
                highlightPaint.color = 0xFF82CAFA.toInt()
            } else {
                highlightPaint.color = Color.WHITE
            }
            canvas?.drawCircle(controlX, controlY, drawableThumbRadius - 5f, highlightPaint)
        }
    }

    private fun drawTrack(canvas: Canvas?) {
        path.reset()
        path.moveTo(trackStartX, trackY)

        when (elasticBehavior) {
            ElasticBehavior.linear -> drawLinearTrack(canvas)
            ElasticBehavior.cubic -> drawBezierTrack(canvas)
        }
    }

    private fun drawBezierTrack(canvas: Canvas?) {
        x1 = (controlX)/2
        y1 = height.toFloat()/2
        x2 = x1
        y2 = controlY
        path.cubicTo(x1, y1, x2, y2, controlX, controlY)

        x1 = (controlX + width.toFloat())/2
        y1 = controlY
        x2 = x1
        y2 = height.toFloat()/2
        path.cubicTo(x1, y1, x2, y2, trackEndX, trackY)

        canvas?.drawPath(path, paint)
    }

    private fun drawLinearTrack(canvas: Canvas?) {
        path.lineTo(controlX, controlY)
        path.lineTo(width.toFloat(), height.toFloat()/2)
        canvas?.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return super.onTouchEvent(event)
        }
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isTouchPointInDrawableThumb(x,y)) {
                    drawableThumbSelected = true
                    controlX = x.coerceHorizontal()
                    controlY = y.coerceVertical().coerceToStretchRange(controlX)
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (drawableThumbSelected) {
                    controlX = x.coerceHorizontal()
                    controlY = y.coerceVertical().coerceToStretchRange(controlX)
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (drawableThumbSelected) {
                    drawableThumbSelected = false
                    controlX = x.coerceHorizontal()
                    controlY = y.coerceVertical().coerceToStretchRange(controlX)
                    valueAnimator?.cancel()
                    valueAnimator = ValueAnimator.ofFloat(
                        controlY,
                        height.toFloat() / 2
                    )
                    valueAnimator?.interpolator = CustomBounceInterpolator(0.5, 30.0)
                    valueAnimator?.addUpdateListener {
                        controlY = it.animatedValue as Float
                        invalidate()
                    }
                    valueAnimator?.start()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isTouchPointInDrawableThumb(x: Float, y: Float): Boolean {
        if (drawableThumb != null) {
            drawableThumb?.let {
                val drawableWidthBy2 = (it.bounds.right - it.bounds.left).absoluteValue / 2
                val drawableHeightBy2 = (it.bounds.bottom - it.bounds.top).absoluteValue / 2
                if (x > controlX-drawableWidthBy2 && x < controlX+drawableWidthBy2 &&
                    y > controlY-drawableHeightBy2 && x < controlY+drawableHeightBy2) {
                    return true
                }
            }
        } else {
            if ((x - controlX) * (x - controlX) +
                (y - controlY) * (y - controlY) <= drawableThumbRadius * drawableThumbRadius) {
                return true
            }
        }
        return false
    }

    private fun Float.coerceHorizontal(): Float {
        return this.coerceAtMost(trackEndX).coerceAtLeast(trackStartX)
    }

    private fun Float.coerceVertical(): Float {
        return this.coerceAtMost(height.toFloat()).coerceAtLeast(0f)
    }

    private fun Float.coerceToStretchRange(x: Float): Float {
        return if (this<=height/2) {
            this.coerceAtLeast(
                if (x <= width/2) {
                    -(((2*(stretchRange + height/2) - height)*(x - trackStartX))/(width-(2*trackStartX)))+(height/2)
                } else {
                    -(((2*(stretchRange + height/2) - height)*(x - trackEndX))/(width-(2*trackEndX)))+(height/2)
                }
            )
        } else {
            this.coerceAtMost(
                if (x <= width/2) {
                    (((2*(stretchRange + height/2) - height)*(x - trackStartX))/(width-(2*trackStartX)))+(height/2)
                } else {
                    (((2*(stretchRange + height/2) - height)*(x - trackEndX))/(width-(2*trackEndX)))+(height/2)
                }
            )
        }
    }

    //region Public functions
    /**
     * Set the Elastic Behavior for the SeekBar.
     */
    fun setElasticBehavior(elasticBehavior: ElasticBehavior) {
        this.elasticBehavior = elasticBehavior
    }

    /**
     * Set the maximum Stretch Range in dp.
     */
    @Throws(IllegalArgumentException::class)
    fun setStretchRange(stretchRangeInDp: Float) {
        if (stretchRangeInDp < 0) {
            throw IllegalArgumentException("Stretch range value can not be negative")
        }
        this.stretchRange = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            stretchRangeInDp,
            context.resources.displayMetrics)
        if (height!=0) {
            this.stretchRange = this.stretchRange.coerceAtMost(height.toFloat()/2)
        }
    }
    //endregion

}