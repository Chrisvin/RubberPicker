package com.jem.rubberpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.animation.ValueAnimator
import android.graphics.Color
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


class RubberSeekBar : View {

    private val paint: Paint by lazy {
        val tempPaint = Paint()
        tempPaint.style = Paint.Style.STROKE
        tempPaint.color = Color.GRAY
        tempPaint.strokeWidth = 5f
        tempPaint
    }
    private var path: Path = Path()
    private var valueAnimator: ValueAnimator? = null
    private var controlX: Float = width.toFloat()/2
    private var controlY: Float = height.toFloat()/2

    private var x1: Float = 0f
    private var y1: Float = 0f
    private var x2: Float = 0f
    private var y2: Float = 0f

    private var stretchRange: Float = 50f

    private var elasticBehavior: ElasticBehavior = ElasticBehavior.cubic

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs)
    constructor(context: Context) :
            super(context)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        stretchRange = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16F,
            context.resources.displayMetrics
        ).coerceAtMost(height.toFloat()/2)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawTrack(canvas)
        //TODO - Add drawable function
        //TODO - Use SpringAnimation & SpringForce instead of ValueAnimator
        //TODO - Assign min, max values and get 'seekbar' values
        //TODO - Expand logic to RubberRangePicker
    }

    private fun drawTrack(canvas: Canvas?) {
        path.reset()
        path.moveTo(0f, height.toFloat()/2)

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
        path.cubicTo(x1, y1, x2, y2, width.toFloat(), height.toFloat()/2)

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
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            controlX = event.x.coerceHorizontal()
            controlY = event.y.coerceVertical().coerceToStretchRange(controlX)
            invalidate()
            return true
        } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL){
            controlX = event.x.coerceHorizontal()
            controlY = event.y.coerceVertical().coerceToStretchRange(controlX)
            valueAnimator?.cancel()
            valueAnimator = ValueAnimator.ofFloat(
                controlY,
                height.toFloat()/2)
            valueAnimator?.interpolator = CustomBounceInterpolator(0.5, 30.0)
            valueAnimator?.addUpdateListener {
                controlY = it.animatedValue as Float
                invalidate()
            }
            valueAnimator?.start()
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun Float.coerceHorizontal(): Float {
        return this.coerceAtMost(width.toFloat()).coerceAtLeast(0f)
    }

    private fun Float.coerceVertical(): Float {
        return this.coerceAtMost(height.toFloat()).coerceAtLeast(0f)
    }

    private fun Float.coerceToStretchRange(x: Float): Float {
        return if (this<=height/2) {
            this.coerceAtLeast(
                if (x <= width/2) {
                    -(((2*(stretchRange + height/2) - height)*(x))/width)+(height/2)
                } else {
                    -(((2*(stretchRange + height/2) - height)*(width - x))/width)+(height/2)
                }
            )
        } else {
            this.coerceAtMost(
                if (x <= width/2) {
                    (((2*(stretchRange + height/2) - height)*(x))/width)+(height/2)
                } else {
                    (((2*(stretchRange + height/2) - height)*(width - x))/width)+(height/2)
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
     * Set the maximum Stretch Range in dp
     */
    fun setStretchRange(stretchRangeInDp: Float) {
        this.stretchRange = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            stretchRangeInDp,
            context.resources.displayMetrics
        ).coerceAtMost(height.toFloat()/2)
    }
    //endregion

}