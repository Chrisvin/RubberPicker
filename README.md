# RubberPicker

[![License: MIT](https://img.shields.io/badge/License-MIT-silver.svg)](https://opensource.org/licenses/MIT) [![](https://jitpack.io/v/Chrisvin/RubberPicker.svg)](https://jitpack.io/#Chrisvin/RubberPicker) [![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=15) [![Android Arsenal]( https://img.shields.io/badge/Android%20Arsenal-RubberPicker-gold.svg?style=flat )]( https://android-arsenal.com/details/1/7867 )

<p align="center"><img src="RubberPicker-Demo.gif"/></p>

RubberPicker library contains the `RubberSeekBar` and `RubberRangePicker`, inspired by [Cuberto's rubber-range-picker](https://github.com/Cuberto/rubber-range-picker).

## Getting started
### Setting up the dependency
1. Add the JitPack repository to your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
2. Add the RubberPicker dependency in the build.gradle:
```
implementation 'com.github.Chrisvin:RubberPicker:v1.4'
```

### Demo app
To run the demo project, clone the repository and run it via Android Studio.

## Usage
### Adding directly in layout.xml
```
<com.jem.rubberpicker.RubberSeekBar
  ...
  app:minValue="20"
  app:maxValue="80"
  app:elasticBehavior="cubic"
  app:dampingRatio="0.3"
  app:stiffness="300"
  app:stretchRange="24dp"
  app:defaultThumbRadius="16dp"
  app:normalTrackWidth="4dp"
  app:highlightTrackWidth="8dp"
  app:normalTrackColor="#AAAAAA"
  app:highlightTrackColor="#BA1F33"
  app:defaultThumbInsideColor="#FFF"
  app:highlightDefaultThumbOnTouchColor="#CD5D67"/>

<!-- Similar attributes can be applied for RubberRangePicker as well-->
<com.jem.rubberpicker.RubberRangePicker
  ...
  app:minValue="0"
  app:maxValue="100"
  app:elasticBehavior="linear"
  app:dampingRatio="0.4"
  app:stiffness="400"
  app:stretchRange="36dp"
  app:defaultThumbRadius="16dp"
  app:normalTrackWidth="4dp"
  app:highlightTrackWidth="8dp"
  app:normalTrackColor="#AAAAAA"
  app:highlightTrackColor="#BA1F33"
  app:defaultThumbInsideColor="#CFCD5D67"
  app:highlightDefaultThumbOnTouchColor="#CD5D67"/>
```
### Adding/Modifying programmatically
```kotlin
val rubberSeekBar = RubberSeekBar(this)
rubberSeekBar.setMin(20)
rubberSeekBar.setMax(80)
rubberSeekBar.setElasticBehavior(ElasticBehavior.CUBIC)
rubberSeekBar.setDampingRatio(0.4F)
rubberSeekBar.setStiffness(1000F)
rubberSeekBar.setStretchRange(50f)
rubberSeekBar.setThumbRadius(32f)
rubberSeekBar.setNormalTrackWidth(2f)
rubberSeekBar.setHighlightTrackWidth(4f)
rubberSeekBar.setNormalTrackColor(Color.GRAY)
rubberSeekBar.setHighlightTrackColor(Color.BLUE)
rubberSeekBar.setHighlightThumbOnTouchColor(Color.CYAN)
rubberSeekBar.setDefaultThumbInsideColor(Color.WHITE)

val currentValue = rubberSeekBar.getCurrentValue()
rubberSeekBar.setCurrentValue(currentValue + 10)
rubberSeekBar.setOnRubberSeekBarChangeListener(object : RubberSeekBar.OnRubberSeekBarChangeListener {
    override fun onProgressChanged(seekBar: RubberSeekBar, value: Int, fromUser: Boolean) {}
    override fun onStartTrackingTouch(seekBar: RubberSeekBar) {}
    override fun onStopTrackingTouch(seekBar: RubberSeekBar) {}
})


//Similarly for RubberRangePicker
val rubberRangePicker = RubberRangePicker(this)
rubberRangePicker.setMin(20)
...
rubberRangePicker.setHighlightThumbOnTouchColor(Color.CYAN)

val startThumbValue = rubberRangePicker.getCurrentStartValue()
rubberRangePicker.setCurrentStartValue(startThumbValue + 10)
val endThumbValue = rubberRangePicker.getCurrentEndValue()
rubberRangePicker.setCurrentEndValue(endThumbValue + 10)
rubberRangePicker.setOnRubberRangePickerChangeListener(object: RubberRangePicker.OnRubberRangePickerChangeListener{
    override fun onProgressChanged(rangePicker: RubberRangePicker, startValue: Int, endValue: Int, fromUser: Boolean) {}
    override fun onStartTrackingTouch(rangePicker: RubberRangePicker, isStartThumb: Boolean) {}
    override fun onStopTrackingTouch(rangePicker: RubberRangePicker, isStartThumb: Boolean) {}
})
```

## Todo
- [ ] Refactor code to remove redundant code between RubberSeekBar & RubberRangePicker.
- [ ] Add step attribute, make necessary UI adjustments for step based value increments.
- [ ] Current library overcomes view clipping by setting the parent layout's clipChildren & clipToPadding as false. Find a better alternative to overcome view clipping.

## Bugs and Feedback
For bugs, questions and discussions please use the [Github Issues](https://github.com/Chrisvin/RubberPicker/issues).

## License
```
MIT License

Copyright (c) 2019 Jem

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
