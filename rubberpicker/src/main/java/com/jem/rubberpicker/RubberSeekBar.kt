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
        private const val drawableThumbRadius: Float = 50f
        private const val normalTrackWidth: Float = 5f
        private const val highlightTrackWidth: Float = 10f

        private const val normalTrackColor: Int = Color.GRAY
        private const val highlightTrackColor: Int = 0xFF38ACEC.toInt()
    }

    private val paint: Paint by lazy {
        val tempPaint = Paint()
        tempPaint.style = Paint.Style.STROKE
        tempPaint.color = normalTrackColor
        tempPaint.strokeWidth = 5f
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
                drawableThumbHalfWidth.toFloat()
            } else {
                drawableThumbRadius
            }
        }
    private val trackEndX: Float
        get() {
            return if (drawableThumb != null) {
                setDrawableHalfWidthAndHeight()
                width - drawableThumbHalfWidth.toFloat()
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
    private var drawableThumbHalfWidth = 0
    private var drawableThumbHalfHeight = 0
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
            paint.color = highlightTrackColor
            paint.style = Paint.Style.FILL
            canvas?.drawCircle(controlX, controlY, drawableThumbRadius, paint)
            if (drawableThumbSelected) {
                paint.color = 0xFF82CAFA.toInt()
            } else {
                paint.color = Color.WHITE
            }
            canvas?.drawCircle(controlX, controlY, drawableThumbRadius - highlightTrackWidth, paint)
            paint.style = Paint.Style.STROKE
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

    private fun drawRigidTrack(canvas: Canvas?) {
        paint.color = highlightTrackColor
        paint.strokeWidth = highlightTrackWidth
        canvas?.drawLine(trackStartX, trackY, controlX, controlY, paint)
        paint.color = normalTrackColor
        paint.strokeWidth = normalTrackWidth
        canvas?.drawLine(controlX, controlY, trackEndX, trackY, paint)
    }

    private fun drawBezierTrack(canvas: Canvas?) {
        x1 = (controlX)/2
        y1 = height.toFloat()/2
        x2 = x1
        y2 = controlY
        path.cubicTo(x1, y1, x2, y2, controlX, controlY)
        paint.color = highlightTrackColor
        paint.strokeWidth = highlightTrackWidth
        canvas?.drawPath(path, paint)

        path.reset()
        path.moveTo(controlX, controlY)
        x1 = (controlX + width.toFloat())/2
        y1 = controlY
        x2 = x1
        y2 = height.toFloat()/2
        path.cubicTo(x1, y1, x2, y2, trackEndX, trackY)
        paint.color = normalTrackColor
        paint.strokeWidth = normalTrackWidth
        canvas?.drawPath(path, paint)
    }

    private fun drawLinearTrack(canvas: Canvas?) {
        paint.color = highlightTrackColor
        paint.strokeWidth = highlightTrackWidth
        path.lineTo(controlX, controlY)
        canvas?.drawPath(path, paint)

        paint.color = normalTrackColor
        paint.strokeWidth = normalTrackWidth
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
                setDrawableHalfWidthAndHeight()
                if (x > controlX-drawableThumbHalfWidth && x < controlX+drawableThumbHalfWidth &&
                    y > controlY-drawableThumbHalfHeight && x < controlY+drawableThumbHalfHeight) {
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

    private fun setDrawableHalfWidthAndHeight() {
        if (drawableThumbHalfWidth!=0 && drawableThumbHalfHeight!=0) {
            return
        }
        drawableThumb?.let {
            drawableThumbHalfWidth = (it.bounds.right - it.bounds.left).absoluteValue / 2
            drawableThumbHalfHeight = (it.bounds.bottom - it.bounds.top).absoluteValue / 2
        }
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