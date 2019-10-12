package com.jem.rubberpicker

import android.content.Context
import android.util.TypedValue

fun convertDpToPx(context: Context, dpValue: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dpValue,
        context.resources.displayMetrics
    )
}
 
