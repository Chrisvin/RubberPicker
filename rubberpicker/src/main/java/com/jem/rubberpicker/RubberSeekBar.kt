package com.jem.rubberpicker

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import java.util.concurrent.ArrayBlockingQueue
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
    private var controlX: Float = -1f
    private var controlY: Float = -1f
    private val initialControlXPositionQueue = ArrayBlockingQueue<Int>(1)
    // Used to determine the start and end points of the track.
    // Useful for drawing and also for other calculations.
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
            return height.toFloat() / 2
        }

    private var x1: Float = 0f
    private var y1: Float = 0f
    private var x2: Float = 0f
    private var y2: Float = 0f

    private val canvasRect = Rect()

    private var stretchRange: Float = -1f

    private var elasticBehavior: ElasticBehavior = ElasticBehavior.CUBIC

    private var drawableThumb: Drawable? = null
    private var drawableThumbHalfWidth = 0
    private var drawableThumbHalfHeight = 0
    private var drawableThumbSelected: Boolean = false

    private var minValue: Int = 0
    private var maxValue: Int = 100

    private var onChangeListener: OnRubberSeekBarChangeListener? = null

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs)

    constructor(context: Context) :
            super(context)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (controlX < trackStartX) {
            if (initialControlXPositionQueue.isEmpty()) {
                controlX = trackStartX
            } else {
                setCurrentValue(initialControlXPositionQueue.poll())
            }
            controlY = trackY
        }
        if (stretchRange == -1f) {
            this.stretchRange = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16F,
                context.resources.displayMetrics
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minimumHeight: Int = if (drawableThumb != null) {
            setDrawableHalfWidthAndHeight()
            drawableThumbHalfHeight*2
        } else {
            (drawableThumbRadius*2).toInt()
        }
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            measureDimension(minimumHeight + paddingTop + paddingBottom, heightMeasureSpec)
        )
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }

        return result
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            canvas?.getClipBounds(canvasRect)
            if (drawableThumb != null) {
                setDrawableHalfWidthAndHeight()
                canvasRect.inset(0, -(drawableThumbHalfHeight + stretchRange.toInt()))
            } else {
                canvasRect.inset(0, -((drawableThumbRadius + stretchRange).toInt()))
            }
            canvas?.clipRect(canvasRect, Region.Op.REPLACE)
        } else {
            // TODO - Try to figure out a better way to overcome view clipping
            // Workaround since Region.Op.REPLACE won't work in Android P & above
            (parent as? ViewGroup)?.clipChildren = false
            (parent as? ViewGroup)?.clipToPadding = false
        }
        drawTrack(canvas)
        drawThumb(canvas)
        //TODO - Determine default values for all the attributes, use dp to ensure similar sizing in all devices
        //TODO - Consider using SpringAnimation & SpringForce instead of ValueAnimator?
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
        if (controlY == trackY) {
            drawRigidTrack(canvas)
            return
        }

        path.reset()
        path.moveTo(trackStartX, trackY)

        when (elasticBehavior) {
            ElasticBehavior.LINEAR -> drawLinearTrack(canvas)
            ElasticBehavior.CUBIC -> drawBezierTrack(canvas)
            ElasticBehavior.RIGID -> drawRigidTrack(canvas)
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
        x1 = (controlX+trackStartX) / 2
        y1 = height.toFloat() / 2
        x2 = x1
        y2 = controlY
        path.cubicTo(x1, y1, x2, y2, controlX, controlY)
        paint.color = highlightTrackColor
        paint.strokeWidth = highlightTrackWidth
        canvas?.drawPath(path, paint)

        path.reset()
        path.moveTo(controlX, controlY)
        x1 = (controlX + trackEndX) / 2
        y1 = controlY
        x2 = x1
        y2 = height.toFloat() / 2
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
        path.lineTo(width.toFloat(), height.toFloat() / 2)
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
                if (isTouchPointInDrawableThumb(x, y)) {
                    drawableThumbSelected = true
                    controlX = x.coerceHorizontal()
                    controlY = y.coerceVertical().coerceToStretchRange(controlX)
                    onChangeListener?.onStartTrackingTouch(this)
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (drawableThumbSelected) {
                    controlX = x.coerceHorizontal()
                    controlY = y.coerceVertical().coerceToStretchRange(controlX)
                    onChangeListener?.onProgressChanged(this, getCurrentValue(), true)
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (drawableThumbSelected) {
                    drawableThumbSelected = false
                    controlX = x.coerceHorizontal()
                    controlY = y.coerceVertical().coerceToStretchRange(controlX)
                    onChangeListener?.onStopTrackingTouch(this)
                    valueAnimator?.cancel()
                    valueAnimator = ValueAnimator.ofFloat(
                        controlY,
                        trackY
                    )
                    valueAnimator?.interpolator = CustomBounceInterpolator(0.5, 30.0)
                    valueAnimator?.addUpdateListener {
                        controlY = it.animatedValue as Float
                        invalidate()
                    }
                    valueAnimator?.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {}
                        override fun onAnimationRepeat(animation: Animator?) {}
                        override fun onAnimationEnd(animation: Animator?) {
                            controlY = trackY
                            invalidate()
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            controlY = trackY
                            invalidate()
                        }
                    })
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
                if (x > controlX - drawableThumbHalfWidth && x < controlX + drawableThumbHalfWidth &&
                    y > controlY - drawableThumbHalfHeight && x < controlY + drawableThumbHalfHeight
                ) {
                    return true
                }
            }
        } else {
            if ((x - controlX) * (x - controlX) +
                (y - controlY) * (y - controlY) <= drawableThumbRadius * drawableThumbRadius
            ) {
                return true
            }
        }
        return false
    }

    private fun setDrawableHalfWidthAndHeight() {
        if (drawableThumbHalfWidth != 0 && drawableThumbHalfHeight != 0) {
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
        return this.coerceAtMost(trackY + stretchRange).coerceAtLeast(trackY - stretchRange)
    }

    private fun Float.coerceToStretchRange(x: Float): Float {
        return if (this <= height / 2) {
            this.coerceAtLeast(
                if (x <= width / 2) {
                    -(((2 * (stretchRange + height / 2) - height) * (x - trackStartX)) / (width - (2 * trackStartX))) + (height / 2)
                } else {
                    -(((2 * (stretchRange + height / 2) - height) * (x - trackEndX)) / (width - (2 * trackEndX))) + (height / 2)
                }
            )
        } else {
            this.coerceAtMost(
                if (x <= width / 2) {
                    (((2 * (stretchRange + height / 2) - height) * (x - trackStartX)) / (width - (2 * trackStartX))) + (height / 2)
                } else {
                    (((2 * (stretchRange + height / 2) - height) * (x - trackEndX)) / (width - (2 * trackEndX))) + (height / 2)
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
            context.resources.displayMetrics
        )
    }

    fun setMin(value: Int) {
        minValue = value
    }

    fun setMax(value: Int) {
        maxValue = value
    }

    fun getCurrentValue(): Int {
        if(controlX <= trackStartX) {
            return minValue
        } else if (controlX >= trackEndX) {
            return maxValue
        }
        return (((controlX - trackStartX)/(trackEndX-trackStartX)) * (maxValue - minValue)).toInt()
    }

    fun setCurrentValue(value: Int) {
        if (trackEndX < 0) {
            //If this function gets called before the view gets layed out and learns what it's width value is
            initialControlXPositionQueue.offer(value)
            return
        }
        controlX = ((value).toFloat()/(maxValue - minValue)) * (trackEndX - trackStartX)
        onChangeListener?.onProgressChanged(this, value, false)
        invalidate()
    }

    fun setOnRubberSeekBarChangeListener(listener: OnRubberSeekBarChangeListener) {
        onChangeListener = listener
    }
    //endregion

    // TODO - Fill out the necessary comments and descriptions

    //region Interfaces
    /**
     * Based on the SeekBar.onSeekBarChangeListener
     */
    interface OnRubberSeekBarChangeListener {
        fun onProgressChanged(seekBar: RubberSeekBar, progress: Int, fromUser: Boolean)
        fun onStartTrackingTouch(seekBar: RubberSeekBar)
        fun onStopTrackingTouch(seekBar: RubberSeekBar)
    }
    //endregion
}