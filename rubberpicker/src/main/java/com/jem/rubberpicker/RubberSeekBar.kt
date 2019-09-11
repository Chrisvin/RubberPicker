package com.jem.rubberpicker

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.animation.FloatValueHolder
import android.support.animation.SpringAnimation
import android.support.animation.SpringForce
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import java.util.concurrent.ArrayBlockingQueue
import kotlin.math.absoluteValue


class RubberSeekBar : View {

    private val paint: Paint by lazy {
        val tempPaint = Paint()
        tempPaint.style = Paint.Style.STROKE
        tempPaint.color = normalTrackColor
        tempPaint.strokeWidth = 5f
        tempPaint
    }
    private var path: Path = Path()
    private var springAnimation: SpringAnimation? = null
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

    private var drawableThumbRadius: Float = 0.0f
    private var normalTrackWidth: Float = 0.0f
    private var highlightTrackWidth: Float = 0.0f

    private var normalTrackColor: Int = 0
    private var highlightTrackColor: Int = 0
    private var highlightThumbOnTouchColor: Int = 0
    private var dampingRatio: Float = 0f
    private var stiffness: Float = 0f

    private var minValue: Int = 0
    private var maxValue: Int = 100

    private var onChangeListener: OnRubberSeekBarChangeListener? = null

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context) :
            super(context) {
        init(null)
    }

    private fun init(attrs: AttributeSet?) {
        stretchRange = convertDpToPx(24f)
        drawableThumbRadius = convertDpToPx(16f)
        normalTrackWidth = convertDpToPx(2f)
        highlightTrackWidth = convertDpToPx(4f)
        normalTrackColor = Color.GRAY
        highlightTrackColor = 0xFF38ACEC.toInt()
        highlightThumbOnTouchColor = 0xFF82CAFA.toInt()
        dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
        stiffness = SpringForce.STIFFNESS_LOW

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RubberSeekBar, 0, 0)
            stretchRange = typedArray.getDimensionPixelSize(R.styleable.RubberSeekBar_stretchRange,
                convertDpToPx(24f).toInt()).toFloat()
            drawableThumbRadius = typedArray.getDimensionPixelSize(R.styleable.RubberSeekBar_defaultThumbRadius,
                convertDpToPx(16f).toInt()).toFloat()
            normalTrackWidth = typedArray.getDimensionPixelSize(R.styleable.RubberSeekBar_normalTrackWidth,
                convertDpToPx(2f).toInt()).toFloat()
            highlightTrackWidth = typedArray.getDimensionPixelSize(R.styleable.RubberSeekBar_highlightTrackWidth,
                convertDpToPx(4f).toInt()).toFloat()
            drawableThumb = typedArray.getDrawable(R.styleable.RubberSeekBar_thumbDrawable)
            normalTrackColor = typedArray.getColor(R.styleable.RubberSeekBar_normalTrackColor, Color.GRAY)
            highlightTrackColor = typedArray.getColor(R.styleable.RubberSeekBar_highlightTrackColor, 0xFF38ACEC.toInt())
            highlightThumbOnTouchColor = typedArray.getColor(R.styleable.RubberSeekBar_highlightDefaultThumbOnTouchColor, 0xFF82CAFA.toInt())
            dampingRatio = typedArray.getFloat(R.styleable.RubberSeekBar_dampingRatio, SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
            stiffness = typedArray.getFloat(R.styleable.RubberSeekBar_stiffness, SpringForce.STIFFNESS_LOW)
            minValue = typedArray.getInt(R.styleable.RubberSeekBar_minValue, 0)
            maxValue = typedArray.getInt(R.styleable.RubberSeekBar_maxValue, 100)
            elasticBehavior = typedArray.getInt(R.styleable.RubberSeekBar_elasticBehavior, 1).run {
                when (this) {
                    0 -> ElasticBehavior.LINEAR
                    1 -> ElasticBehavior.CUBIC
                    2 -> ElasticBehavior.RIGID
                    else -> ElasticBehavior.CUBIC
                }
            }
            typedArray.recycle()
        }
    }

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
        //TODO - Expand logic to RubberRangePicker
    }

    private fun drawThumb(canvas: Canvas?) {
        if (elasticBehavior == ElasticBehavior.RIGID) {
            controlY = trackY
        }
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
                paint.color = highlightThumbOnTouchColor
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
        canvas?.drawLine(trackStartX, trackY, controlX, trackY, paint)
        paint.color = normalTrackColor
        paint.strokeWidth = normalTrackWidth
        canvas?.drawLine(controlX, trackY, trackEndX, trackY, paint)
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

        path.reset()
        path.moveTo(controlX, controlY)
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
                    springAnimation?.cancel()
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
                    springAnimation =
                        SpringAnimation(FloatValueHolder(trackY))
                            .setStartValue(controlY)
                            .setSpring(SpringForce(trackY)
                                .setDampingRatio(dampingRatio)
                                .setStiffness(stiffness))
                            .addUpdateListener { _, value, _ ->
                                controlY = value
                                invalidate()
                            }
                            .addEndListener { _, _, _, _ ->
                                controlY = trackY
                                invalidate()
                            }
                    springAnimation?.start()
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

    private fun convertDpToPx(dpValue: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            context.resources.displayMetrics)
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
        invalidate()
    }

    /**
     * Set the maximum Stretch Range in dp.
     */
    @Throws(IllegalArgumentException::class)
    fun setStretchRange(stretchRangeInDp: Float) {
        if (stretchRangeInDp < 0) {
            throw IllegalArgumentException("Stretch range value can not be negative")
        }
        this.stretchRange = convertDpToPx(stretchRangeInDp)
        invalidate()
    }

    fun setThumbRadius(dpValue: Float) {
        drawableThumbRadius = convertDpToPx(dpValue)
        invalidate()
    }

    fun setNormalTrackWidth(dpValue: Float) {
        normalTrackWidth = convertDpToPx(dpValue)
        invalidate()
    }

    fun setHighlightTrackWidth(dpValue: Float) {
        highlightTrackWidth = convertDpToPx(dpValue)
        invalidate()
    }

    fun setNormalTrackColor(value: Int) {
        normalTrackColor = value
        invalidate()
    }

    fun setHighlightTrackColor(value: Int) {
        highlightTrackColor = value
        invalidate()
    }

    fun setHighlightThumbOnTouchColor(value: Int) {
        highlightThumbOnTouchColor = value
        invalidate()
    }

    fun setDampingRatio(value: Float) {
        if (value < 0.0f) run { throw IllegalArgumentException("Damping ratio must be non-negative") }
        dampingRatio = value
        invalidate()
    }

    fun setStiffness(value: Float) {
        if (value <= 0.0f) run { throw IllegalArgumentException("Spring stiffness constant must be positive.") }
        stiffness = value
        invalidate()
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