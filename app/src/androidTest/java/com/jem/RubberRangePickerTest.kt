package com.jem

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.jem.rubberpicker.ElasticBehavior
import com.jem.rubberpicker.RubberRangePicker
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class RubberRangePickerTest {

    private lateinit var rubberRangePicker: RubberRangePicker

    @Before
    fun setUp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        rubberRangePicker = RubberRangePicker(appContext)
    }

    @Test
    fun getCurrentValue() {
        if  (true) {
            //TODO: Convert this unit test to espresso UI test and then remove this if check
            // Since `setCurrentValue()` is tightly coupled with UI, and can't be tested without it.
            return
        }

        assertEquals(0, rubberRangePicker.getCurrentStartValue())
        assertEquals(100, rubberRangePicker.getCurrentEndValue())
        rubberRangePicker.setCurrentStartValue(50)
        assertEquals(50, rubberRangePicker.getCurrentStartValue())
        rubberRangePicker.setCurrentStartValue(19)
        assertEquals(19, rubberRangePicker.getCurrentStartValue())
        rubberRangePicker.setCurrentEndValue(63)
        assertEquals(63, rubberRangePicker.getCurrentEndValue())
        rubberRangePicker.setCurrentEndValue(96)
        assertEquals(96, rubberRangePicker.getCurrentEndValue())

        rubberRangePicker.setMax(85)
        assertEquals(85, rubberRangePicker.getCurrentEndValue())
        assertEquals(19, rubberRangePicker.getCurrentStartValue())

        rubberRangePicker.setCurrentStartValue(3)
        assertEquals(3, rubberRangePicker.getCurrentStartValue())
        rubberRangePicker.setCurrentEndValue(30)
        assertEquals(30, rubberRangePicker.getCurrentEndValue())

        rubberRangePicker.setMin(39)
        assertEquals(39, rubberRangePicker.getCurrentStartValue())
        assertEquals(39, rubberRangePicker.getCurrentEndValue())
        rubberRangePicker.setCurrentEndValue(47)
        assertEquals(47, rubberRangePicker.getCurrentEndValue())
    }

    @Test
    fun getMin() {
        assertEquals(0, rubberRangePicker.getMin())
        rubberRangePicker.setMin(17)
        assertEquals(17, rubberRangePicker.getMin())
        rubberRangePicker.setMin(93)
        assertEquals(93, rubberRangePicker.getMin())
    }

    @Test
    fun getMax() {
        assertEquals(100, rubberRangePicker.getMax())
        rubberRangePicker.setMax(58)
        assertEquals(58, rubberRangePicker.getMax())
        rubberRangePicker.setMax(29)
        assertEquals(29, rubberRangePicker.getMax())
    }

    @Test
    fun getElasticBehavior() {
        rubberRangePicker.setElasticBehavior(ElasticBehavior.RIGID)
        assertEquals(ElasticBehavior.RIGID, rubberRangePicker.getElasticBehavior())
        rubberRangePicker.setElasticBehavior(ElasticBehavior.CUBIC)
        assertEquals(ElasticBehavior.CUBIC, rubberRangePicker.getElasticBehavior())
        rubberRangePicker.setElasticBehavior(ElasticBehavior.LINEAR)
        assertEquals(ElasticBehavior.LINEAR, rubberRangePicker.getElasticBehavior())
    }

    @Test
    fun getDampingRation() {
        rubberRangePicker.setDampingRatio(1.81f)
        assertEquals(1.81f, rubberRangePicker.getDampingRation())
        rubberRangePicker.setDampingRatio(7.77f)
        assertEquals(7.77f, rubberRangePicker.getDampingRation())
    }

    @Test
    fun getStiffness() {
        rubberRangePicker.setStiffness(1.134f)
        assertEquals(1.134f, rubberRangePicker.getStiffness())
        rubberRangePicker.setStiffness(4.311f)
        assertEquals(4.311f, rubberRangePicker.getStiffness())
    }

}