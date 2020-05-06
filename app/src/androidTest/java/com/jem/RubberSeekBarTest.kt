package com.jem

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.jem.rubberpicker.ElasticBehavior
import com.jem.rubberpicker.RubberSeekBar
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class RubberSeekBarTest {

    private lateinit var rubberSeekBar: RubberSeekBar

    @Before
    fun setUp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        rubberSeekBar = RubberSeekBar(appContext)
    }

    @Test
    fun getCurrentValue() {
        if  (true) {
            //TODO: Convert this unit test to espresso UI test and then remove this if check
            // Since `setCurrentValue()` is tightly coupled with UI, and can't be tested without it.
            return
        }

        assertEquals(0, rubberSeekBar.getCurrentValue())
        rubberSeekBar.setCurrentValue(50)
        assertEquals(50, rubberSeekBar.getCurrentValue())
        rubberSeekBar.setCurrentValue(19)
        assertEquals(19, rubberSeekBar.getCurrentValue())
        rubberSeekBar.setCurrentValue(63)
        assertEquals(63, rubberSeekBar.getCurrentValue())
        rubberSeekBar.setCurrentValue(96)
        assertEquals(96, rubberSeekBar.getCurrentValue())

        rubberSeekBar.setMax(85)
        assertEquals(85, rubberSeekBar.getCurrentValue())
        rubberSeekBar.setCurrentValue(3)
        assertEquals(3, rubberSeekBar.getCurrentValue())
        rubberSeekBar.setMin(39)
        assertEquals(39, rubberSeekBar.getCurrentValue())
        rubberSeekBar.setCurrentValue(47)
        assertEquals(47, rubberSeekBar.getCurrentValue())
    }

    @Test
    fun getMin() {
        assertEquals(0, rubberSeekBar.getMin())
        rubberSeekBar.setMin(13)
        assertEquals(13, rubberSeekBar.getMin())
        rubberSeekBar.setMin(79)
        assertEquals(79, rubberSeekBar.getMin())
    }

    @Test
    fun getMax() {
        assertEquals(100, rubberSeekBar.getMax())
        rubberSeekBar.setMax(66)
        assertEquals(66, rubberSeekBar.getMax())
        rubberSeekBar.setMax(11)
        assertEquals(11, rubberSeekBar.getMax())
    }

    @Test
    fun getElasticBehavior() {
        rubberSeekBar.setElasticBehavior(ElasticBehavior.RIGID)
        assertEquals(ElasticBehavior.RIGID, rubberSeekBar.getElasticBehavior())
        rubberSeekBar.setElasticBehavior(ElasticBehavior.CUBIC)
        assertEquals(ElasticBehavior.CUBIC, rubberSeekBar.getElasticBehavior())
        rubberSeekBar.setElasticBehavior(ElasticBehavior.LINEAR)
        assertEquals(ElasticBehavior.LINEAR, rubberSeekBar.getElasticBehavior())
    }

    @Test
    fun getDampingRation() {
        rubberSeekBar.setDampingRatio(1.01f)
        assertEquals(1.01f, rubberSeekBar.getDampingRation())
        rubberSeekBar.setDampingRatio(4.76f)
        assertEquals(4.76f, rubberSeekBar.getDampingRation())
    }

    @Test
    fun getStiffness() {
        rubberSeekBar.setStiffness(2.34f)
        assertEquals(2.34f, rubberSeekBar.getStiffness())
        rubberSeekBar.setStiffness(4.32f)
        assertEquals(4.32f, rubberSeekBar.getStiffness())
    }

}