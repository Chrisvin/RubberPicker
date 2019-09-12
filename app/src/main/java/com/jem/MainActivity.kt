package com.jem

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.RadioButton
import android.widget.SeekBar
import com.jem.rubberpicker.ElasticBehavior
import com.jem.rubberpicker.RubberSeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        elasticBehavior.setOnCheckedChangeListener { _, checkedId ->
            when (findViewById<RadioButton>(checkedId).text) {
                "Cubic" -> rubberSeekBar.setElasticBehavior(ElasticBehavior.CUBIC)
                "Linear" -> rubberSeekBar.setElasticBehavior(ElasticBehavior.LINEAR)
                "Rigid" -> rubberSeekBar.setElasticBehavior(ElasticBehavior.RIGID)
            }
        }

        stretchRange.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                stretchRangeValue.text = progress.toString()
                rubberSeekBar.setStretchRange(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dampingRatio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                dampingRatioValue.text = (progress.toFloat()/10).toString()
                rubberSeekBar.setDampingRatio(progress.toFloat()/10)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        stiffness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressValue = if (progress!=0) progress * 50 else 1
                stiffnessValue.text = (progressValue).toString()
                rubberSeekBar.setStiffness(progressValue.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        defaultThumbRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressValue = if (progress == 0) 1 else progress
                defaultThumbRadiusValue.text = progressValue.toString()
                rubberSeekBar.setThumbRadius(progressValue.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        normalTrackWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                normalTrackWidthValue.text = progress.toString()
                rubberSeekBar.setNormalTrackWidth(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        highlightTrackWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                highlightTrackWidthValue.text = progress.toString()
                rubberSeekBar.setHighlightTrackWidth(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        rubberSeekBar.setOnRubberSeekBarChangeListener(object: RubberSeekBar.OnRubberSeekBarChangeListener{
            override fun onProgressChanged(seekBar: RubberSeekBar, progress: Int, fromUser: Boolean) {
                rubberSeekBarValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: RubberSeekBar) {}
            override fun onStopTrackingTouch(seekBar: RubberSeekBar) {}
        })
    }
}