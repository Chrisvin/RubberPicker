package com.jem.rubberpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.MotionEvent
import android.view.View


class RubberSeekBar : View {

    private val paint: Paint by lazy {
        val tempPaint = Paint()
        tempPaint.style = Paint.Style.STROKE
        tempPaint
    }
    private var path: Path = Path()
    private var valueAnimator: ValueAnimator? = null
    private var controlX: Float = width.toFloat()/2
    private var controlY: Float = height.toFloat()/2

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs)
    constructor(context: Context) :
            super(context)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = Color.RED
        paint.strokeWidth = 10F
        path.reset()
        path.moveTo(0f, height.toFloat()/2)
        path.quadTo(controlX, controlY, width.toFloat(), height.toFloat()/2)

        canvas?.drawPath(path, paint)

        //TODO - Move to draw path function
        //TODO - Add drawable function
        //TODO - Use SpringAnimation & SpringForce instead of ValueAnimator
        //TODO - Assign min, max values and get 'seekbar' values
        //TODO - Expand logic to RubberRangePicker
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return super.onTouchEvent(event)
        }
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            controlX = event.x.coerceAtMost(width.toFloat()).coerceAtLeast(0f)
            controlY = event.y.coerceAtMost(height.toFloat()).coerceAtLeast(0f)
            invalidate()
            return true
        } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL){
            valueAnimator?.cancel()
            valueAnimator = ValueAnimator.ofFloat(
                event.y.coerceAtMost(height.toFloat()).coerceAtLeast(0f),
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
}