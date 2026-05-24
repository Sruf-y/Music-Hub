package ui.components

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalPointerSlopOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.lerp
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2
import com.aacomposetest.Log
import com.aacomposetest.append
import com.aacomposetest.countIf
import com.aacomposetest.roundToIntOffset
import com.aacomposetest.toPx
import com.aacomposetest.ui.theme.MyColor
import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ui.components.Text.OutlinedTextFieldExpanded
import java.util.ArrayList

/**
 * #DISCLAIMER
 *
 * Most of this code has been copied from the Jetpack Compose open source project.
 * I have made a few modifications like removing the functionality of ticks or "steps" but the general idea is the same.
 * I needed to be able to mess around with the inner workings of the Slider and use VerticalSlider (which was internal
 * to the Jetpack Compose project and perfectly stable but inaccessible to me due to unknown reasons since again, it is stable)
 */

/**
 * [Material Design slider](https://m3.material.io/components/sliders/overview)
 *
 * Sliders allow users to make selections from a range of values.
 *
 * It uses [SliderDefaults.Thumb] and [SliderDefaults.Track] as the thumb and track.
 *
 * Sliders reflect a range of values along a horizontal bar, from which users may select a single
 * value. They are ideal for adjusting settings such as volume, brightness, or applying image
 * filters.
 *
 * ![Sliders
 * image](https://firebasestorage.googleapis.com/v0/b/design-spec/o/projects%2Fgoogle-material-3%2Fimages%2Flqe2zb2b-1.png?alt=media)
 *
 * Use continuous sliders to allow users to make meaningful selections that don’t require a specific
 * value:
 *
 * @sample androidx.compose.material3.samples.SliderSample
 *
 * You can allow the user to choose only between predefined set of values by specifying the amount
 * of steps between min and max values:
 *
 * @sample androidx.compose.material3.samples.StepsSliderSample
 * @param value current value of the slider. If outside of [valueRange] provided, value will be
 *   coerced to this range.
 * @param onValueChange callback in which value should be updated
 * @param modifier the [Modifier] to be applied to this slider
 * @param enabled controls the enabled state of this slider. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param valueRange range of values that this slider can take. The passed [value] will be coerced
 *   to this range.
 * @param steps if positive, specifies the amount of discrete allowable values between the endpoints
 *   of [valueRange]. For example, a range from 0 to 10 with 4 [steps] allows 4 values evenly
 *   distributed between 0 and 10 (i.e., 2, 4, 6, 8). If [steps] is 0, the slider will behave
 *   continuously and allow any value from the range. Must not be negative.
 * @param onValueChangeFinished called when value change has ended. This should not be used to
 *   update the slider value (use [onValueChange] instead), but rather to know when the user has
 *   completed selecting a new value by ending a drag or a click.
 * @param colors [SliderColors] that will be used to resolve the colors used for this slider in
 *   different states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 *   for this slider. You can create and pass in your own `remember`ed instance to observe
 *   [Interaction]s and customize the appearance / behavior of this slider in different states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Slider(
   value: Float,
   onValueChange: (Float) -> Unit,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
   @IntRange(from = 0) steps: Int = 0,
   onValueChangeFinished: (() -> Unit)? = null,
   colors: SliderColors = SliderDefaults.colors(),
   interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
   Slider(
      value = value,
      onValueChange = onValueChange,
      modifier = modifier,
      enabled = enabled,
      onValueChangeFinished = onValueChangeFinished,
      colors = colors,
      interactionSource = interactionSource,
      steps = steps,
      thumb = {
         SliderDefaults.Thumb(
            interactionSource = interactionSource,
            colors = colors,
            enabled = enabled,
         )
      },
      track = { sliderState ->
         SliderDefaults.Track(colors = colors, enabled = enabled, sliderState = sliderState)
      },
      valueRange = valueRange,
   )
}

/**
 * [Material Design slider](https://m3.material.io/components/sliders/overview)
 *
 * Sliders allow users to make selections from a range of values.
 *
 * Sliders reflect a range of values along a horizontal bar, from which users may select a single
 * value. They are ideal for adjusting settings such as volume, brightness, or applying image
 * filters.
 *
 * ![Sliders
 * image](https://firebasestorage.googleapis.com/v0/b/design-spec/o/projects%2Fgoogle-material-3%2Fimages%2Flqe2zb2b-1.png?alt=media)
 *
 * Use continuous sliders to allow users to make meaningful selections that don’t require a specific
 * value:
 *
 * @sample androidx.compose.material3.samples.SliderSample
 *
 * You can allow the user to choose only between predefined set of values by specifying the amount
 * of steps between min and max values:
 *
 * @sample androidx.compose.material3.samples.StepsSliderSample
 *
 * Slider using a custom thumb:
 *
 * @sample androidx.compose.material3.samples.SliderWithCustomThumbSample
 *
 * Slider using custom track and thumb:
 *
 * @sample androidx.compose.material3.samples.SliderWithCustomTrackAndThumbSample
 * @param value current value of the slider. If outside of [valueRange] provided, value will be
 *   coerced to this range.
 * @param onValueChange callback in which value should be updated
 * @param modifier the [Modifier] to be applied to this slider
 * @param enabled controls the enabled state of this slider. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param onValueChangeFinished called when value change has ended. This should not be used to
 *   update the slider value (use [onValueChange] instead), but rather to know when the user has
 *   completed selecting a new value by ending a drag or a click.
 * @param colors [SliderColors] that will be used to resolve the colors used for this slider in
 *   different states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 *   for this slider. You can create and pass in your own `remember`ed instance to observe
 *   [Interaction]s and customize the appearance / behavior of this slider in different states.
 * @param steps if positive, specifies the amount of discrete allowable values between the endpoints
 *   of [valueRange]. For example, a range from 0 to 10 with 4 [steps] allows 4 values evenly
 *   distributed between 0 and 10 (i.e., 2, 4, 6, 8). If [steps] is 0, the slider will behave
 *   continuously and allow any value from the range. Must not be negative.
 * @param thumb the thumb to be displayed on the slider, it is placed on top of the track. The
 *   lambda receives a [SliderState] which is used to obtain the current active track.
 * @param track the track to be displayed on the slider, it is placed underneath the thumb. The
 *   lambda receives a [SliderState] which is used to obtain the current active track.
 * @param valueRange range of values that this slider can take. The passed [value] will be coerced
 *   to this range.
 */
@Composable
@ExperimentalMaterial3Api
fun Slider(
   value: Float,
   onValueChange: (Float) -> Unit,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   onValueChangeFinished: (() -> Unit)? = null,
   colors: SliderColors = SliderDefaults.colors(),
   interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
   @IntRange(from = 0) steps: Int = 0,
   thumb: @Composable (SliderState) -> Unit = {
      SliderDefaults.Thumb(
         interactionSource = interactionSource,
         colors = colors,
         enabled = enabled,
      )
   },
   track: @Composable (SliderState) -> Unit = { sliderState ->
      SliderDefaults.Track(colors = colors, enabled = enabled, sliderState = sliderState)
   },
   valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
   val state =
      remember(steps, valueRange) { SliderState(value, onValueChangeFinished, valueRange) }
   state.onValueChangeFinished = onValueChangeFinished
   state.onValueChange = onValueChange
   state.value = value
   
   Slider(
      state = state,
      modifier = modifier,
      enabled = enabled,
      interactionSource = interactionSource,
      thumb = thumb,
      track = track,
   )
}

/**
 * [Material Design slider](https://m3.material.io/components/sliders/overview)
 *
 * Sliders allow users to make selections from a range of values.
 *
 * Sliders reflect a range of values along a horizontal bar, from which users may select a single
 * value. They are ideal for adjusting settings such as volume, brightness, or applying image
 * filters.
 *
 * ![Sliders
 * image](https://firebasestorage.googleapis.com/v0/b/design-spec/o/projects%2Fgoogle-material-3%2Fimages%2Flqe2zb2b-1.png?alt=media)
 *
 * Use continuous sliders to allow users to make meaningful selections that don’t require a specific
 * value:
 *
 * @sample androidx.compose.material3.samples.SliderSample
 *
 * You can allow the user to choose only between predefined set of values by specifying the amount
 * of steps between min and max values:
 *
 * @sample androidx.compose.material3.samples.StepsSliderSample
 *
 * Slider using a custom thumb:
 *
 * @sample androidx.compose.material3.samples.SliderWithCustomThumbSample
 *
 * Slider using custom track and thumb:
 *
 * @sample androidx.compose.material3.samples.SliderWithCustomTrackAndThumbSample
 * @param state [SliderState] which contains the slider's current value.
 * @param modifier the [Modifier] to be applied to this slider
 * @param enabled controls the enabled state of this slider. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param colors [SliderColors] that will be used to resolve the colors used for this slider in
 *   different states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 *   for this slider. You can create and pass in your own `remember`ed instance to observe
 *   [Interaction]s and customize the appearance / behavior of this slider in different states.
 * @param thumb the thumb to be displayed on the slider, it is placed on top of the track. The
 *   lambda receives a [SliderState] which is used to obtain the current active track.
 * @param track the track to be displayed on the slider, it is placed underneath the thumb. The
 *   lambda receives a [SliderState] which is used to obtain the current active track.
 */
@Composable
@ExperimentalMaterial3Api
fun Slider(
   state: SliderState,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   colors: SliderColors = SliderDefaults.colors(),
   interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
   thumb: @Composable (SliderState) -> Unit = {
      SliderDefaults.Thumb(
         interactionSource = interactionSource,
         colors = colors,
         enabled = enabled,
      )
   },
   track: @Composable (SliderState) -> Unit = { sliderState ->
      SliderDefaults.Track(colors = colors, enabled = enabled, sliderState = sliderState)
   }
) {
   
   SliderImpl(
      state = state,
      modifier = modifier,
      enabled = enabled,
      interactionSource = interactionSource,
      thumb = thumb,
      track = track
   )
}

/**
 * [Material Design slider](https://m3.material.io/components/sliders/overview)
 *
 * Vertical Sliders allow users to make selections from a range of values.
 *
 * Vertical Sliders reflect a range of values along a vertical bar, from which users may select a
 * single value. They are ideal for adjusting settings such as volume, brightness, or applying image
 * filters.
 *
 * ![Sliders
 * image](https://firebasestorage.googleapis.com/v0/b/design-spec/o/projects%2Fgoogle-material-3%2Fimages%2Flqe2zb2b-1.png?alt=media)
 *
 * @param state [SliderState] which contains the slider's current value.
 * @param modifier the [Modifier] to be applied to this slider
 * @param enabled controls the enabled state of this slider. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param reverseDirection controls the direction of this slider. Default is top to bottom.
 * @param colors [SliderColors] that will be used to resolve the colors used for this slider in
 *   different states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 *   for this slider. You can create and pass in your own `remember`ed instance to observe
 *   [Interaction]s and customize the appearance / behavior of this slider in different states.
 * @param thumb the thumb to be displayed on the slider, it is placed on top of the track. The
 *   lambda receives a [SliderState] which is used to obtain the current active track.
 * @param track the track to be displayed on the slider, it is placed underneath the thumb. The
 *   lambda receives a [SliderState] which is used to obtain the current active track.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalSlider(
   state: SliderState,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   reverseDirection: Boolean = false,
   colors: SliderColors = SliderDefaults.colors(),
   interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
   thumb: @Composable (SliderState) -> Unit = { sliderState ->
      SliderDefaults.Thumb(
         interactionSource = interactionSource,
         sliderState = sliderState,
         colors = colors,
         enabled = enabled,
         thumbSize = VerticalThumbSize,
      )
   },
   track: @Composable (SliderState) -> Unit = { sliderState ->
      SliderDefaults.Track(
         colors = colors,
         enabled = enabled,
         sliderState = sliderState,
         trackCornerSize = Dp.Unspecified,
      )
   },
) {
   
   
   state.orientation = Vertical
   state.reverseVerticalDirection = reverseDirection
   
   SliderImpl(
      state = state,
      modifier = modifier,
      enabled = enabled,
      interactionSource = interactionSource,
      thumb = thumb,
      track = track
   )
}

/**
 * [Material Design range slider](https://m3.material.io/components/sliders/overview)
 *
 * Range Sliders expand upon [Slider] using the same concepts but allow the user to select 2 values.
 *
 * The two values are still bounded by the value range but they also cannot cross each other.
 *
 * Use continuous Range Sliders to allow users to make meaningful selections that don’t require a
 * specific values:
 *
 * @sample androidx.compose.material3.samples.RangeSliderSample
 *
 * You can allow the user to choose only between predefined set of values by specifying the amount
 * of steps between min and max values:
 *
 * @sample androidx.compose.material3.samples.StepRangeSliderSample
 * @param value current values of the RangeSlider. If either value is outside of [valueRange]
 *   provided, it will be coerced to this range.
 * @param onValueChange lambda in which values should be updated
 * @param modifier modifiers for the Range Slider layout
 * @param enabled whether or not component is enabled and can we interacted with or not
 * @param valueRange range of values that Range Slider values can take. Passed [value] will be
 *   coerced to this range
 * @param steps if positive, specifies the amount of discrete allowable values between the endpoints
 *   of [valueRange]. For example, a range from 0 to 10 with 4 [steps] allows 4 values evenly
 *   distributed between 0 and 10 (i.e., 2, 4, 6, 8). If [steps] is 0, the slider will behave
 *   continuously and allow any value from the range. Must not be negative.
 * @param onValueChangeFinished lambda to be invoked when value change has ended. This callback
 *   shouldn't be used to update the range slider values (use [onValueChange] for that), but rather
 *   to know when the user has completed selecting a new value by ending a drag or a click.
 * @param colors [SliderColors] that will be used to determine the color of the Range Slider parts
 *   in different state. See [SliderDefaults.colors] to customize.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeSlider(
   value: ClosedFloatingPointRange<Float>,
   onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
   @IntRange(from = 0) steps: Int = 0,
   onValueChangeFinished: (() -> Unit)? = null,
   colors: SliderColors = SliderDefaults.colors(),
) {
   val startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
   val endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
   
   RangeSlider(
      value = value,
      onValueChange = onValueChange,
      modifier = modifier,
      enabled = enabled,
      valueRange = valueRange,
      steps = steps,
      onValueChangeFinished = onValueChangeFinished,
      startInteractionSource = startInteractionSource,
      endInteractionSource = endInteractionSource,
      startThumb = {
         SliderDefaults.Thumb(
            interactionSource = startInteractionSource,
            colors = colors,
            enabled = enabled,
         )
      },
      endThumb = {
         SliderDefaults.Thumb(
            interactionSource = endInteractionSource,
            colors = colors,
            enabled = enabled,
         )
      },
      track = { rangeSliderState ->
         SliderDefaults.Track(
            colors = colors,
            enabled = enabled,
            rangeSliderState = rangeSliderState,
         )
      },
   )
}

/**
 * [Material Design range slider](https://m3.material.io/components/sliders/overview)
 *
 * Range Sliders expand upon [Slider] using the same concepts but allow the user to select 2 values.
 *
 * The two values are still bounded by the value range but they also cannot cross each other.
 *
 * It uses the provided startThumb for the slider's start thumb and endThumb for the slider's end
 * thumb. It also uses the provided track for the slider's track. If nothing is passed for these
 * parameters, it will use [SliderDefaults.Thumb] and [SliderDefaults.Track] for the thumbs and
 * track.
 *
 * Use continuous Range Sliders to allow users to make meaningful selections that don’t require a
 * specific values:
 *
 * @sample androidx.compose.material3.samples.RangeSliderSample
 *
 * You can allow the user to choose only between predefined set of values by specifying the amount
 * of steps between min and max values:
 *
 * @sample androidx.compose.material3.samples.StepRangeSliderSample
 *
 * A custom start/end thumb and track can be provided:
 *
 * @sample androidx.compose.material3.samples.RangeSliderWithCustomComponents
 * @param value current values of the RangeSlider. If either value is outside of [valueRange]
 *   provided, it will be coerced to this range.
 * @param onValueChange lambda in which values should be updated
 * @param modifier modifiers for the Range Slider layout
 * @param enabled whether or not component is enabled and can we interacted with or not
 * @param onValueChangeFinished lambda to be invoked when value change has ended. This callback
 *   shouldn't be used to update the range slider values (use [onValueChange] for that), but rather
 *   to know when the user has completed selecting a new value by ending a drag or a click.
 * @param colors [SliderColors] that will be used to determine the color of the Range Slider parts
 *   in different state. See [SliderDefaults.colors] to customize.
 * @param startInteractionSource the [MutableInteractionSource] representing the stream of
 *   [Interaction]s for the start thumb. You can create and pass in your own `remember`ed instance
 *   to observe.
 * @param endInteractionSource the [MutableInteractionSource] representing the stream of
 *   [Interaction]s for the end thumb. You can create and pass in your own `remember`ed instance to
 *   observe.
 * @param steps if positive, specifies the amount of discrete allowable values between the endpoints
 *   of [valueRange]. For example, a range from 0 to 10 with 4 [steps] allows 4 values evenly
 *   distributed between 0 and 10 (i.e., 2, 4, 6, 8). If [steps] is 0, the slider will behave
 *   continuously and allow any value from the range. Must not be negative.
 * @param startThumb the start thumb to be displayed on the Range Slider. The lambda receives a
 *   [RangeSliderState] which is used to obtain the current active track.
 * @param endThumb the end thumb to be displayed on the Range Slider. The lambda receives a
 *   [RangeSliderState] which is used to obtain the current active track.
 * @param track the track to be displayed on the range slider, it is placed underneath the thumb.
 *   The lambda receives a [RangeSliderState] which is used to obtain the current active track.
 * @param valueRange range of values that Range Slider values can take. Passed [value] will be
 *   coerced to this range.
 */
@Composable
@ExperimentalMaterial3Api
fun RangeSlider(
   value: ClosedFloatingPointRange<Float>,
   onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
   onValueChangeFinished: (() -> Unit)? = null,
   colors: SliderColors = SliderDefaults.colors(),
   startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
   endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
   startThumb: @Composable (RangeSliderState) -> Unit = {
      SliderDefaults.Thumb(
         interactionSource = startInteractionSource,
         colors = colors,
         enabled = enabled,
      )
   },
   endThumb: @Composable (RangeSliderState) -> Unit = {
      SliderDefaults.Thumb(
         interactionSource = endInteractionSource,
         colors = colors,
         enabled = enabled,
      )
   },
   track: @Composable (RangeSliderState) -> Unit = { rangeSliderState ->
      SliderDefaults.Track(
         colors = colors,
         enabled = enabled,
         rangeSliderState = rangeSliderState,
      )
   },
   @IntRange(from = 0) steps: Int = 0,
) {
   val state =
      remember(steps, valueRange) {
         RangeSliderState(
            value.start,
            value.endInclusive,
            steps,
            onValueChangeFinished,
            valueRange,
         )
      }
   
   state.onValueChangeFinished = onValueChangeFinished
   state.onValueChange = { onValueChange(it.start..it.endInclusive) }
   state.activeRangeStart = value.start
   state.activeRangeEnd = value.endInclusive
   
   RangeSlider(
      modifier = modifier,
      state = state,
      enabled = enabled,
      startInteractionSource = startInteractionSource,
      endInteractionSource = endInteractionSource,
      startThumb = startThumb,
      endThumb = endThumb,
      track = track,
   )
}

/**
 * [Material Design range slider](https://m3.material.io/components/sliders/overview)
 *
 * Range Sliders expand upon [Slider] using the same concepts but allow the user to select 2 values.
 *
 * The two values are still bounded by the value range but they also cannot cross each other.
 *
 * It uses the provided startThumb for the slider's start thumb and endThumb for the slider's end
 * thumb. It also uses the provided track for the slider's track. If nothing is passed for these
 * parameters, it will use [SliderDefaults.Thumb] and [SliderDefaults.Track] for the thumbs and
 * track.
 *
 * Use continuous Range Sliders to allow users to make meaningful selections that don’t require a
 * specific values:
 *
 * @sample androidx.compose.material3.samples.RangeSliderSample
 *
 * You can allow the user to choose only between predefined set of values by specifying the amount
 * of steps between min and max values:
 *
 * @sample androidx.compose.material3.samples.StepRangeSliderSample
 *
 * A custom start/end thumb and track can be provided:
 *
 * @sample androidx.compose.material3.samples.RangeSliderWithCustomComponents
 * @param state [RangeSliderState] which contains the current values of the RangeSlider.
 * @param modifier modifiers for the Range Slider layout
 * @param enabled whether or not component is enabled and can we interacted with or not
 * @param colors [SliderColors] that will be used to determine the color of the Range Slider parts
 *   in different state. See [SliderDefaults.colors] to customize.
 * @param startInteractionSource the [MutableInteractionSource] representing the stream of
 *   [Interaction]s for the start thumb. You can create and pass in your own `remember`ed instance
 *   to observe.
 * @param endInteractionSource the [MutableInteractionSource] representing the stream of
 *   [Interaction]s for the end thumb. You can create and pass in your own `remember`ed instance to
 *   observe.
 * @param startThumb the start thumb to be displayed on the Range Slider. The lambda receives a
 *   [RangeSliderState] which is used to obtain the current active track.
 * @param endThumb the end thumb to be displayed on the Range Slider. The lambda receives a
 *   [RangeSliderState] which is used to obtain the current active track.
 * @param track the track to be displayed on the range slider, it is placed underneath the thumb.
 *   The lambda receives a [RangeSliderState] which is used to obtain the current active track.
 */
@Composable
@ExperimentalMaterial3Api
fun RangeSlider(
   state: RangeSliderState,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   colors: SliderColors = SliderDefaults.colors(),
   startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
   endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
   startThumb: @Composable (RangeSliderState) -> Unit = {
      SliderDefaults.Thumb(
         interactionSource = startInteractionSource,
         colors = colors,
         enabled = enabled,
      )
   },
   endThumb: @Composable (RangeSliderState) -> Unit = {
      SliderDefaults.Thumb(
         interactionSource = endInteractionSource,
         colors = colors,
         enabled = enabled,
      )
   },
   track: @Composable (RangeSliderState) -> Unit = { rangeSliderState ->
      SliderDefaults.Track(
         colors = colors,
         enabled = enabled,
         rangeSliderState = rangeSliderState,
      )
   },
) {
   require(state.steps >= 0) { "steps should be >= 0" }
   
   RangeSliderImpl(
      modifier = modifier,
      state = state,
      enabled = enabled,
      startInteractionSource = startInteractionSource,
      endInteractionSource = endInteractionSource,
      startThumb = startThumb,
      endThumb = endThumb,
      track = track,
   )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SliderImpl(
   modifier: Modifier,
   state: SliderState,
   enabled: Boolean,
   interactionSource: MutableInteractionSource,
   thumb: @Composable (SliderState) -> Unit,
   track: @Composable (SliderState) -> Unit,
) {
   state.isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
   val reverseDirection =
      (state.orientation == Horizontal && state.isRtl) ||
         (state.orientation == Vertical && state.reverseVerticalDirection)
   val press = Modifier.sliderTapModifier(state, interactionSource, enabled)
   val drag =
      Modifier.draggable(
         orientation = state.orientation,
         reverseDirection = reverseDirection,
         enabled = enabled,
         interactionSource = interactionSource,
         onDragStopped = { state.gestureEndAction() },
         startDragImmediately = state.isDragging,
         state = state,
      )
   val thumbModifier =
      if (state.orientation == Vertical) {
         Modifier
            .layoutId(SliderComponents.THUMB)
            .wrapContentHeight()
      } else {
         Modifier
            .layoutId(SliderComponents.THUMB)
            .wrapContentWidth()
      }
   
   Layout(
      {
         Box(
            modifier =
               thumbModifier.onSizeChanged {
                  state.thumbWidth = it.width
                  state.thumbHeight = it.height
               }
         ) {
            thumb(state)
         }
         Box(modifier = Modifier.layoutId(SliderComponents.TRACK)) { track(state) }
      },
      modifier =
         modifier
            //.minimumInteractiveComponentSize()
            .requiredSizeIn(
               minWidth = if (state.orientation == Vertical) TrackHeight else ThumbWidth,
               minHeight = if (state.orientation == Vertical) ThumbWidth else TrackHeight,
            )
            .sliderSemantics(state, enabled)
            .focusable(enabled, interactionSource)
            .slideOnKeyEvents(
               enabled,
               state.valueRange,
               state.onValueChange,
               state.onValueChangeFinished,
            )
            .then(press)
            .then(drag),
   ) { measurables, constraints ->
      val thumbPlaceable =
         measurables.fastFirst { it.layoutId == SliderComponents.THUMB }.measure(constraints)
      
      val trackMeasurable = measurables.fastFirst { it.layoutId == SliderComponents.TRACK }
      val trackPlaceable =
         if (state.orientation == Vertical) {
            trackMeasurable.measure(
               constraints.offset(vertical = -thumbPlaceable.height).copy(minWidth = 0)
            )
         } else {
            trackMeasurable.measure(
               constraints.offset(horizontal = -thumbPlaceable.width).copy(minHeight = 0)
            )
         }
      
      val sliderWidth: Int
      val sliderHeight: Int
      val trackOffsetX: Int
      val trackOffsetY: Int
      val thumbOffsetX: Int
      var thumbOffsetY: Int
      val valueAsFraction = state.coercedValueAsFraction
      
      val trackCornerSize =
         trackPlaceable[CornerSizeAlignmentLine].let {
            if (it != AlignmentLine.Unspecified) it else 0
         }
      
      if (state.orientation == Vertical) {
         sliderWidth = max(trackPlaceable.width, thumbPlaceable.width)
         sliderHeight = thumbPlaceable.height + trackPlaceable.height
         trackOffsetX = (sliderWidth - trackPlaceable.width) / 2
         trackOffsetY = thumbPlaceable.height / 2
         thumbOffsetX = (sliderWidth - thumbPlaceable.width) / 2
         thumbOffsetY = (trackPlaceable.height * valueAsFraction).roundToInt()
         
         if (state.reverseVerticalDirection) {
            thumbOffsetY = trackPlaceable.height - thumbOffsetY
         }
      } else {
         sliderWidth = thumbPlaceable.width + trackPlaceable.width
         sliderHeight = max(trackPlaceable.height, thumbPlaceable.height)
         trackOffsetX = thumbPlaceable.width / 2
         trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
         thumbOffsetX = (trackPlaceable.width * valueAsFraction).roundToInt()
         
         thumbOffsetY = (sliderHeight - thumbPlaceable.height) / 2
      }
      
      state.updateDimensions(newTotalWidth = sliderWidth, newTotalHeight = sliderHeight)
      
      layout(sliderWidth, sliderHeight) {
         trackPlaceable.placeRelative(trackOffsetX, trackOffsetY)
         thumbPlaceable.placeRelative(thumbOffsetX, thumbOffsetY)
      }
   }
}

private fun Modifier.slideOnKeyEvents(
   enabled: Boolean,
   valueRange: ClosedFloatingPointRange<Float>,
   onValueChangeState: ((Float) -> Unit)?,
   onValueChangeFinishedState: (() -> Unit)?,
): Modifier {
   
   return this.onKeyEvent {
      if (!enabled) return@onKeyEvent false
      if (onValueChangeState == null) return@onKeyEvent false
      when (it.type) {
         KeyEventType.KeyDown -> {
            val rangeLength = abs(valueRange.endInclusive - valueRange.start)
            // When steps == 0, it means that a user is not limited by a step length (delta)
            // when using touch or mouse. But it is not possible to adjust the value
            // continuously when using keyboard buttons - the delta has to be discrete.
            // In this case, 1% of the valueRange seems to make sense.
            
            when (it.key) {
               Key.MoveHome -> {
                  onValueChangeState(valueRange.start)
                  true
               }
               Key.MoveEnd -> {
                  onValueChangeState(valueRange.endInclusive)
                  true
               }
               else -> false
            }
         }
         KeyEventType.KeyUp -> {
            when (it.key) {
               Key.DirectionUp,
               Key.DirectionDown,
               Key.DirectionRight,
               Key.DirectionLeft,
               Key.MoveHome,
               Key.MoveEnd,
               Key.PageUp,
               Key.PageDown -> {
                  onValueChangeFinishedState?.invoke()
                  true
               }
               else -> false
            }
         }
         else -> false
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeSliderImpl(
   modifier: Modifier,
   state: RangeSliderState,
   enabled: Boolean,
   startInteractionSource: MutableInteractionSource,
   endInteractionSource: MutableInteractionSource,
   startThumb: @Composable ((RangeSliderState) -> Unit),
   endThumb: @Composable ((RangeSliderState) -> Unit),
   track: @Composable ((RangeSliderState) -> Unit),
) {
   state.isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
   
   val pressDrag =
      Modifier.rangeSliderPressDragModifier(
         state,
         startInteractionSource,
         endInteractionSource,
         enabled,
      )
   
   val startContentDescription = "SliderRangeStart"
   val endContentDescription = "SliderRangeEnd"
   
   Layout(
      {
         Box(
            modifier =
               Modifier
                  .layoutId(RangeSliderComponents.STARTTHUMB)
                  .wrapContentWidth()
                  .onSizeChanged {
                     state.startThumbWidth = it.width.toFloat()
                     state.startThumbHeight = it.height.toFloat()
                  }
                  .rangeSliderStartThumbSemantics(state, enabled)
                  .semantics(mergeDescendants = true) {
                     contentDescription = startContentDescription
                  }
                  .focusable(enabled, startInteractionSource)
         ) {
            startThumb(state)
         }
         Box(
            modifier =
               Modifier
                  .layoutId(RangeSliderComponents.ENDTHUMB)
                  .wrapContentWidth()
                  .onSizeChanged {
                     state.endThumbWidth = it.width.toFloat()
                     state.endThumbHeight = it.height.toFloat()
                  }
                  .rangeSliderEndThumbSemantics(state, enabled)
                  .semantics(mergeDescendants = true) {
                     contentDescription = endContentDescription
                  }
                  .focusable(enabled, endInteractionSource)
         ) {
            endThumb(state)
         }
         Box(modifier = Modifier.layoutId(RangeSliderComponents.TRACK)) { track(state) }
      },
      modifier =
         modifier
            //.minimumInteractiveComponentSize()
            .requiredSizeIn(minWidth = ThumbWidth, minHeight = TrackHeight)
            .then(pressDrag),
   ) { measurables, constraints ->
      val startThumbPlaceable =
         measurables
            .fastFirst { it.layoutId == RangeSliderComponents.STARTTHUMB }
            .measure(constraints)
      
      val endThumbPlaceable =
         measurables
            .fastFirst { it.layoutId == RangeSliderComponents.ENDTHUMB }
            .measure(constraints)
      
      val trackPlaceable =
         measurables
            .fastFirst { it.layoutId == RangeSliderComponents.TRACK }
            .measure(
               constraints
                  .offset(
                     horizontal = -(startThumbPlaceable.width + endThumbPlaceable.width) / 2
                  )
                  .copy(minHeight = 0)
            )
      
      val sliderWidth =
         trackPlaceable.width + (startThumbPlaceable.width + endThumbPlaceable.width) / 2
      val sliderHeight =
         maxOf(trackPlaceable.height, startThumbPlaceable.height, endThumbPlaceable.height)
      
      state.totalWidth = sliderWidth
      
      state.updateMinMaxPx()
      
      val startValueAsFraction = state.coercedActiveRangeStartAsFraction
      val isStartOnFirstOrLastStep =
         startValueAsFraction == state.tickFractions.firstOrNull() ||
            startValueAsFraction == state.tickFractions.lastOrNull()
      val endValueAsFraction = state.coercedActiveRangeEndAsFraction
      val isEndOnFirstOrLastStep =
         endValueAsFraction == state.tickFractions.firstOrNull() ||
            endValueAsFraction == state.tickFractions.lastOrNull()
      val trackOffsetX = startThumbPlaceable.width / 2
      val trackCornerSize =
         trackPlaceable[CornerSizeAlignmentLine].let {
            if (it != AlignmentLine.Unspecified) it else 0
         }
      
      val startThumbOffsetX =
         if (state.steps > 0 && !isStartOnFirstOrLastStep) {
            ((trackPlaceable.width - trackCornerSize * 2) * startValueAsFraction).roundToInt() +
               trackCornerSize
         } else {
            (trackPlaceable.width * startValueAsFraction).roundToInt()
         }
      // When start thumb and end thumb have different widths,
      // we need to add a correction for the centering of the slider.
      val endCorrection = (startThumbPlaceable.width - endThumbPlaceable.width) / 2
      val endThumbOffsetX =
         if (state.steps > 0 && !isEndOnFirstOrLastStep) {
            ((trackPlaceable.width - trackCornerSize * 2) * endValueAsFraction + endCorrection)
               .roundToInt() + trackCornerSize
         } else {
            (trackPlaceable.width * endValueAsFraction + endCorrection).roundToInt()
         }
      val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
      val startThumbOffsetY = (sliderHeight - startThumbPlaceable.height) / 2
      val endThumbOffsetY = (sliderHeight - endThumbPlaceable.height) / 2
      
      layout(sliderWidth, sliderHeight) {
         trackPlaceable.placeRelative(trackOffsetX, trackOffsetY)
         startThumbPlaceable.placeRelative(startThumbOffsetX, startThumbOffsetY)
         endThumbPlaceable.placeRelative(endThumbOffsetX, endThumbOffsetY)
      }
   }
}

/** Object to hold defaults used by [Slider] */
@Stable
object SliderDefaults {
   
   /**
    * Creates a [SliderColors] that represents the different colors used in parts of the [Slider]
    * in different states.
    */
   @Composable fun colors() = MaterialTheme.colorScheme.defaultSliderColors
   
   /**
    * Creates a [SliderColors] that represents the different colors used in parts of the [Slider]
    * in different states.
    *
    * For the name references below the words "active" and "inactive" are used. Active part of the
    * slider is filled with progress, so if slider's progress is 30% out of 100%, left (or right in
    * RTL) 30% of the track will be active, while the rest is inactive.
    *
    * @param thumbColor thumb color when enabled
    * @param activeTrackColor color of the track in the part that is "active", meaning that the
    *   thumb is ahead of it
    * @param activeTickColor colors to be used to draw tick marks on the active track, if `steps`
    *   is specified
    * @param inactiveTrackColor color of the track in the part that is "inactive", meaning that the
    *   thumb is before it
    * @param inactiveTickColor colors to be used to draw tick marks on the inactive track, if
    *   `steps` are specified on the Slider is specified
    * @param disabledThumbColor thumb colors when disabled
    * @param disabledActiveTrackColor color of the track in the "active" part when the Slider is
    *   disabled
    * @param disabledActiveTickColor colors to be used to draw tick marks on the active track when
    *   Slider is disabled and when `steps` are specified on it
    * @param disabledInactiveTrackColor color of the track in the "inactive" part when the Slider
    *   is disabled
    * @param disabledInactiveTickColor colors to be used to draw tick marks on the inactive part of
    *   the track when Slider is disabled and when `steps` are specified on it
    */
   @Composable
   fun colors(
      thumbColor: Color = Color.Unspecified,
      activeTrackColor: Color = Color.Unspecified,
      activeTickColor: Color = Color.Unspecified,
      inactiveTrackColor: Color = Color.Unspecified,
      inactiveTickColor: Color = Color.Unspecified,
      disabledThumbColor: Color = Color.Unspecified,
      disabledActiveTrackColor: Color = Color.Unspecified,
      disabledActiveTickColor: Color = Color.Unspecified,
      disabledInactiveTrackColor: Color = Color.Unspecified,
      disabledInactiveTickColor: Color = Color.Unspecified,
   ): SliderColors =
      MaterialTheme.colorScheme.defaultSliderColors.copy(
         thumbColor = thumbColor,
         activeTrackColor = activeTrackColor,
         activeTickColor = activeTickColor,
         inactiveTrackColor = inactiveTrackColor,
         inactiveTickColor = inactiveTickColor,
         disabledThumbColor = disabledThumbColor,
         disabledActiveTrackColor = disabledActiveTrackColor,
         disabledActiveTickColor = disabledActiveTickColor,
         disabledInactiveTrackColor = disabledInactiveTrackColor,
         disabledInactiveTickColor = disabledInactiveTickColor,
      )
   
   internal val ColorScheme.defaultSliderColors: SliderColors
      get() = SliderColors(
         MyColor.Green,
         MyColor.Blue,
         MyColor.Red,
         MyColor.SeparatorGray,
         MyColor.PurpleGrey40,
         MyColor.SeparatorGray,
         MyColor.SeparatorGray,
         MyColor.SeparatorGray,
         MyColor.SeparatorGray,
         MyColor.SeparatorGray
      )
   
   val handleShape = RoundedCornerShape(15.dp)
   
   /**
    * The Default thumb for [Slider] and [RangeSlider]
    *
    * @param interactionSource the [MutableInteractionSource] representing the stream of
    *   [Interaction]s for this thumb. You can create and pass in your own `remember`ed instance to
    *   observe
    * @param modifier the [Modifier] to be applied to the thumb.
    * @param colors [SliderColors] that will be used to resolve the colors used for this thumb in
    *   different states. See [SliderDefaults.colors].
    * @param enabled controls the enabled state of this slider. When `false`, this component will
    *   not respond to user input, and it will appear visually disabled and disabled to
    *   accessibility services.
    * @param thumbSize the size of the thumb.
    */
   @Composable
   fun Thumb(
      interactionSource: MutableInteractionSource,
      modifier: Modifier = Modifier,
      colors: SliderColors = colors(),
      enabled: Boolean = true,
      thumbSize: DpSize = ThumbSize,
   ) {
      val interactions = remember { mutableStateListOf<Interaction>() }
      LaunchedEffect(interactionSource) {
         interactionSource.interactions.collect { interaction ->
            when (interaction) {
               is PressInteraction.Press -> interactions.add(interaction)
               is PressInteraction.Release -> interactions.remove(interaction.press)
               is PressInteraction.Cancel -> interactions.remove(interaction.press)
               is DragInteraction.Start -> interactions.add(interaction)
               is DragInteraction.Stop -> interactions.remove(interaction.start)
               is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
         }
      }
      
      val size =
         if (interactions.isNotEmpty()) {
            thumbSize.copy(width = thumbSize.width / 2)
         } else {
            thumbSize
         }
      Spacer(
         modifier
            .size(size)
            .hoverable(interactionSource = interactionSource)
            .background(colors.thumbColor(enabled),)
      )
   }
   
   /**
    * The Default thumb for [Slider], [VerticalSlider] and [RangeSlider]
    *
    * @param interactionSource the [MutableInteractionSource] representing the stream of
    *   [Interaction]s for this thumb. You can create and pass in your own `remember`ed instance to
    *   observe
    * @param sliderState [SliderState] which is used to obtain the current active track.
    * @param modifier the [Modifier] to be applied to the thumb.
    * @param colors [SliderColors] that will be used to resolve the colors used for this thumb in
    *   different states. See [SliderDefaults.colors].
    * @param enabled controls the enabled state of this slider. When `false`, this component will
    *   not respond to user input, and it will appear visually disabled and disabled to
    *   accessibility services.
    * @param thumbSize the size of the thumb.
    */
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   internal fun Thumb(
      interactionSource: MutableInteractionSource,
      sliderState: SliderState,
      modifier: Modifier = Modifier,
      colors: SliderColors = colors(),
      enabled: Boolean = true,
      thumbSize: DpSize = ThumbSize,
   ) {
      val interactions = remember { mutableStateListOf<Interaction>() }
      LaunchedEffect(interactionSource) {
         interactionSource.interactions.collect { interaction ->
            when (interaction) {
               is PressInteraction.Press -> interactions.add(interaction)
               is PressInteraction.Release -> interactions.remove(interaction.press)
               is PressInteraction.Cancel -> interactions.remove(interaction.press)
               is DragInteraction.Start -> interactions.add(interaction)
               is DragInteraction.Stop -> interactions.remove(interaction.start)
               is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
         }
      }
      
      val size =
         if (interactions.isNotEmpty()) {
            if (sliderState.orientation == Vertical) {
               thumbSize.copy(height = thumbSize.height / 2)
            } else {
               thumbSize.copy(width = thumbSize.width / 2)
            }
         } else {
            thumbSize
         }
      Spacer(
         modifier
            .size(size)
            .hoverable(interactionSource = interactionSource)
            .background(colors.thumbColor(enabled), handleShape)
      )
   }
   
   
   
   
   /**
    * The Default track for [Slider]
    *
    * @param sliderState [SliderState] which is used to obtain the current active track.
    * @param modifier the [Modifier] to be applied to the track.
    * @param enabled controls the enabled state of this slider. When `false`, this component will
    *   not respond to user input, and it will appear visually disabled and disabled to
    *   accessibility services.
    * @param colors [SliderColors] that will be used to resolve the colors used for this track in
    *   different states. See [SliderDefaults.colors].
    * @param drawStopIndicator lambda that will be called to draw the stop indicator at the end of
    *   the track.
    * @param drawTick lambda that will be called to draw the ticks if steps are greater than 0.
    * @param thumbTrackGapSize size of the gap between the thumb and the track.
    * @param trackInsideCornerSize size of the corners towards the thumb when a gap is set.
    */
   @ExperimentalMaterial3Api
   @Composable
   fun Track(
      sliderState: SliderState,
      modifier: Modifier = Modifier,
      enabled: Boolean = true,
      colors: SliderColors = colors(),
      drawStopIndicator: (DrawScope.(Offset) -> Unit)? = {
         drawStopIndicator(
            offset = it,
            color = colors.trackColor(enabled, active = true),
            size = TrackStopIndicatorSize,
         )
      },
      drawTick: DrawScope.(Offset, Color) -> Unit = { offset, color ->
         drawStopIndicator(offset = offset, color = color, size = TickSize)
      },
      thumbTrackGapSize: Dp = ThumbTrackGapSize,
      trackInsideCornerSize: Dp = TrackInsideCornerSize,
      orientation: Orientation = Horizontal,
      reverseDirection: Boolean = LocalLayoutDirection.current == LayoutDirection.Rtl
   ) {
      TrackImpl(
         sliderState = sliderState,
         trackCornerSize = Dp.Unspecified,
         modifier = modifier,
         enabled = enabled,
         colors = colors,
         thumbTrackGapSize = thumbTrackGapSize,
         trackInsideCornerSize = trackInsideCornerSize,
         enableCornerShrinking = false,
         isCentered = false,
         orientation = orientation,
         reverseDirection = reverseDirection
      )
   }
   
   /**
    * The Default track for [Slider] and [VerticalSlider]
    *
    * This track has a different corner treatment where the corner size decreases as the thumb gets
    * closer.
    *
    * @param sliderState [SliderState] which is used to obtain the current active track.
    * @param trackCornerSize size of the external corners.
    * @param modifier the [Modifier] to be applied to the track.
    * @param enabled controls the enabled state of this slider. When `false`, this component will
    *   not respond to user input, and it will appear visually disabled and disabled to
    *   accessibility services.
    * @param colors [SliderColors] that will be used to resolve the colors used for this track in
    *   different states. See [SliderDefaults.colors].
    * @param drawStopIndicator lambda that will be called to draw the stop indicator at the end of
    *   the track.
    * @param drawTick lambda that will be called to draw the ticks if steps are greater than 0.
    * @param thumbTrackGapSize size of the gap between the thumb and the track.
    * @param trackInsideCornerSize size of the corners towards the thumb when a gap is set.
    */
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   internal fun Track(
      sliderState: SliderState,
      trackCornerSize: Dp,
      modifier: Modifier = Modifier,
      enabled: Boolean = true,
      colors: SliderColors = colors(),
      thumbTrackGapSize: Dp = ThumbTrackGapSize,
      trackInsideCornerSize: Dp = TrackInsideCornerSize,
      orientation: Orientation = Horizontal,
      reverseDirection: Boolean = LocalLayoutDirection.current == LayoutDirection.Rtl
   ) {
      TrackImpl(
         sliderState = sliderState,
         trackCornerSize = trackCornerSize,
         modifier = modifier,
         enabled = enabled,
         colors = colors,
         thumbTrackGapSize = thumbTrackGapSize,
         trackInsideCornerSize = trackInsideCornerSize,
         enableCornerShrinking = true,
         isCentered = false,
         orientation = orientation,
         reverseDirection = reverseDirection
      )
   }
   
   /**
    * The Default centered track for [Slider] and [VerticalSlider]
    *
    * This track starts from the center of the slider.
    *
    * @param sliderState [SliderState] which is used to obtain the current active track.
    * @param modifier the [Modifier] to be applied to the track.
    * @param enabled controls the enabled state of this slider. When `false`, this component will
    *   not respond to user input, and it will appear visually disabled and disabled to
    *   accessibility services.
    * @param colors [SliderColors] that will be used to resolve the colors used for this track in
    *   different states. See [SliderDefaults.colors].
    * @param drawStopIndicator lambda that will be called to draw the stop indicator at the end of
    *   the track.
    * @param drawTick lambda that will be called to draw the ticks if steps are greater than 0.
    * @param thumbTrackGapSize size of the gap between the thumb and the track.
    * @param trackInsideCornerSize size of the corners towards the thumb when a gap is set.
    * @param trackCornerSize size of the external corners.
    */
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   internal fun CenteredTrack(
      sliderState: SliderState,
      modifier: Modifier = Modifier,
      enabled: Boolean = true,
      colors: SliderColors = colors(),
      drawStopIndicator: (DrawScope.(Offset) -> Unit)? = {
         drawStopIndicator(
            offset = it,
            color = colors.trackColor(enabled, active = true),
            size = TrackStopIndicatorSize,
         )
      },
      drawTick: DrawScope.(Offset, Color) -> Unit = { offset, color ->
         drawStopIndicator(offset = offset, color = color, size = TickSize)
      },
      thumbTrackGapSize: Dp = ThumbTrackGapSize,
      trackInsideCornerSize: Dp = TrackInsideCornerSize,
      trackCornerSize: Dp = Dp.Unspecified,
   ) {
      TrackImpl(
         sliderState = sliderState,
         trackCornerSize = trackCornerSize,
         modifier = modifier,
         enabled = enabled,
         colors = colors,
         thumbTrackGapSize = thumbTrackGapSize,
         trackInsideCornerSize = trackInsideCornerSize,
         enableCornerShrinking = true,
         isCentered = true,
         orientation = Horizontal,
         reverseDirection = LocalLayoutDirection.current == LayoutDirection.Rtl
      )
   }
   
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   private fun TrackImpl(
      sliderState: SliderState,
      trackCornerSize: Dp,
      modifier: Modifier,
      enabled: Boolean,
      colors: SliderColors,
      thumbTrackGapSize: Dp,
      trackInsideCornerSize: Dp,
      enableCornerShrinking: Boolean,
      isCentered: Boolean,
      orientation:Orientation,
      reverseDirection:Boolean
   ) {
      
      val inactiveTrackColor = colors.trackColor(enabled = enabled, active = false)
      val activeTrackColor = colors.trackColor(enabled = enabled, active = true)
      Canvas(
         if (orientation == Vertical) {
            modifier.width(TrackHeight).fillMaxHeight().let {
               if (reverseDirection) it.scale(1f, -1f) else it
            }
         } else {
            modifier
               .fillMaxWidth()
               .height(TrackHeight)
         }
            .then(
               Modifier.layout { measurable, constraints ->
                  val placeable = measurable.measure(constraints)
                  val cornerSize =
                     if (trackCornerSize == Dp.Unspecified) {
                        if (orientation == Vertical) {
                           placeable.width / 2
                        } else {
                           placeable.height / 2
                        }
                     } else {
                        trackCornerSize.roundToPx()
                     }
                  layout(
                     width = placeable.width,
                     height = placeable.height,
                     alignmentLines = mapOf(CornerSizeAlignmentLine to cornerSize),
                  ) {
                     placeable.place(0, 0)
                  }
               }
            )
      ) {
         val cornerSize: Float =
            if (trackCornerSize == Dp.Unspecified) {
               if (orientation == Vertical) {
                  size.width / 2
               } else {
                  size.height / 2
               }
            } else {
               trackCornerSize.toPx()
            }
         drawTrack(
            activeRangeStart = 0f,
            activeRangeEnd = sliderState.coercedValueAsFraction,
            inactiveTrackColor = inactiveTrackColor,
            activeTrackColor = activeTrackColor,
            startThumbWidth = 0.toDp(),
            startThumbHeight = 0.toDp(),
            endThumbWidth = sliderState.thumbWidth.toDp(),
            endThumbHeight = sliderState.thumbHeight.toDp(),
            thumbTrackGapSize = thumbTrackGapSize,
            trackInsideCornerSize = trackInsideCornerSize,
            trackCornerSize = cornerSize.toDp(),
            isRangeSlider = false,
            enableCornerShrinking = enableCornerShrinking,
            orientation = sliderState.orientation,
            isCentered = isCentered,
         )
      }
   }
   
   
   
   /**
    * The Default track for [RangeSlider]
    *
    * @param rangeSliderState [RangeSliderState] which is used to obtain the current active track.
    * @param modifier the [Modifier] to be applied to the track.
    * @param enabled controls the enabled state of this slider. When `false`, this component will
    *   not respond to user input, and it will appear visually disabled and disabled to
    *   accessibility services.
    * @param colors [SliderColors] that will be used to resolve the colors used for this track in
    *   different states. See [SliderDefaults.colors].
    * @param drawStopIndicator lambda that will be called to draw the stop indicator at the
    *   start/end of the track.
    * @param drawTick lambda that will be called to draw the ticks if steps are greater than 0.
    * @param thumbTrackGapSize size of the gap between the thumbs and the track.
    * @param trackInsideCornerSize size of the corners towards the thumbs when a gap is set.
    */
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   fun Track(
      rangeSliderState: RangeSliderState,
      modifier: Modifier = Modifier,
      enabled: Boolean = true,
      colors: SliderColors = colors(),
      drawStopIndicator: (DrawScope.(Offset) -> Unit)? = {
         drawStopIndicator(
            offset = it,
            color = colors.trackColor(enabled, active = true),
            size = TrackStopIndicatorSize,
         )
      },
      drawTick: DrawScope.(Offset, Color) -> Unit = { offset, color ->
         drawStopIndicator(offset = offset, color = color, size = TickSize)
      },
      thumbTrackGapSize: Dp = ThumbTrackGapSize,
      trackInsideCornerSize: Dp = TrackInsideCornerSize,
   ) {
      TrackImpl(
         rangeSliderState = rangeSliderState,
         trackCornerSize = Dp.Unspecified,
         modifier = modifier,
         enabled = enabled,
         colors = colors,
         drawStopIndicator = drawStopIndicator,
         thumbTrackGapSize = thumbTrackGapSize,
         trackInsideCornerSize = trackInsideCornerSize,
      )
   }
   
   /**
    * The Default track for [RangeSlider]
    *
    * @param rangeSliderState [RangeSliderState] which is used to obtain the current active track.
    * @param trackCornerSize size of the external corners.
    * @param modifier the [Modifier] to be applied to the track.
    * @param enabled controls the enabled state of this slider. When `false`, this component will
    *   not respond to user input, and it will appear visually disabled and disabled to
    *   accessibility services.
    * @param colors [SliderColors] that will be used to resolve the colors used for this track in
    *   different states. See [SliderDefaults.colors].
    * @param drawStopIndicator lambda that will be called to draw the stop indicator at the
    *   start/end of the track.
    * @param drawTick lambda that will be called to draw the ticks if steps are greater than 0.
    * @param thumbTrackGapSize size of the gap between the thumbs and the track.
    * @param trackInsideCornerSize size of the corners towards the thumbs when a gap is set.
    */
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   internal fun Track(
      rangeSliderState: RangeSliderState,
      trackCornerSize: Dp,
      modifier: Modifier = Modifier,
      enabled: Boolean = true,
      colors: SliderColors = colors(),
      drawStopIndicator: (DrawScope.(Offset) -> Unit)? = {
         drawStopIndicator(
            offset = it,
            color = colors.trackColor(enabled, active = true),
            size = TrackStopIndicatorSize,
         )
      },
      drawTick: DrawScope.(Offset, Color) -> Unit = { offset, color ->
         drawStopIndicator(offset = offset, color = color, size = TickSize)
      },
      thumbTrackGapSize: Dp = ThumbTrackGapSize,
      trackInsideCornerSize: Dp = TrackInsideCornerSize,
   ) {
      TrackImpl(
         rangeSliderState = rangeSliderState,
         trackCornerSize = trackCornerSize,
         modifier = modifier,
         enabled = enabled,
         colors = colors,
         drawStopIndicator = drawStopIndicator,
         thumbTrackGapSize = thumbTrackGapSize,
         trackInsideCornerSize = trackInsideCornerSize,
      )
   }
   
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   private fun TrackImpl(
      rangeSliderState: RangeSliderState,
      trackCornerSize: Dp,
      modifier: Modifier,
      enabled: Boolean,
      colors: SliderColors,
      drawStopIndicator: (DrawScope.(Offset) -> Unit)?,
      thumbTrackGapSize: Dp,
      trackInsideCornerSize: Dp,
   ) {
      val inactiveTrackColor = colors.trackColor(enabled, active = false)
      val activeTrackColor = colors.trackColor(enabled, active = true)
      Canvas(
         modifier
            .fillMaxWidth()
            .height(TrackHeight)
            .layout { measurable, constraints ->
               val placeable = measurable.measure(constraints)
               val trackCornerSize = placeable.height / 2
               layout(
                  width = placeable.width,
                  height = placeable.height,
                  alignmentLines = mapOf(CornerSizeAlignmentLine to trackCornerSize),
               ) {
                  placeable.place(0, 0)
               }
            }
      ) {
         val cornerSize: Float =
            if (trackCornerSize == Dp.Unspecified) {
               size.height / 2
            } else {
               trackCornerSize.toPx()
            }
         drawTrack(
            activeRangeStart = rangeSliderState.coercedActiveRangeStartAsFraction,
            activeRangeEnd = rangeSliderState.coercedActiveRangeEndAsFraction,
            inactiveTrackColor = inactiveTrackColor,
            activeTrackColor = activeTrackColor,
            startThumbWidth = rangeSliderState.startThumbWidth.toDp(),
            startThumbHeight = rangeSliderState.startThumbHeight.toDp(),
            endThumbWidth = rangeSliderState.endThumbWidth.toDp(),
            endThumbHeight = rangeSliderState.endThumbHeight.toDp(),
            thumbTrackGapSize = thumbTrackGapSize,
            trackInsideCornerSize = trackInsideCornerSize,
            trackCornerSize = cornerSize.toDp(),
            isRangeSlider = true,
         )
      }
   }
   
   private fun DrawScope.drawTrack(
      activeRangeStart: Float,
      activeRangeEnd: Float,
      inactiveTrackColor: Color,
      activeTrackColor: Color,
      startThumbWidth: Dp,
      startThumbHeight: Dp,
      endThumbWidth: Dp,
      endThumbHeight: Dp,
      thumbTrackGapSize: Dp,
      trackInsideCornerSize: Dp,
      trackCornerSize: Dp,
      isRangeSlider: Boolean,
      enableCornerShrinking: Boolean = false,
      orientation: Orientation = Horizontal,
      isCentered: Boolean = false,
      reverseDirection:Boolean = layoutDirection == LayoutDirection.Rtl
   ) {
      val isVertical = orientation == Vertical
      val isRtl = reverseDirection
      val isRtlHorizontal = isRtl && !isVertical
      val cornerSize = trackCornerSize.toPx()
      val sliderStart = 0f
      val sliderEnd = if (isVertical) size.height else size.width
      
      
      val sliderValueEnd = sliderStart + (sliderEnd - sliderStart) * activeRangeEnd
      
      val sliderValueStart = sliderStart + (sliderEnd - sliderStart) * activeRangeStart
      
      
      val insideCornerSize = trackInsideCornerSize.toPx()
      var startGap = 0f
      var endGap = 0f
      if (thumbTrackGapSize > 0.dp) {
         if (isVertical) {
            startGap = startThumbHeight.toPx() / 2 + thumbTrackGapSize.toPx()
            endGap = endThumbHeight.toPx() / 2 + thumbTrackGapSize.toPx()
         } else {
            startGap = startThumbWidth.toPx() / 2 + thumbTrackGapSize.toPx()
            endGap = endThumbWidth.toPx() / 2 + thumbTrackGapSize.toPx()
         }
      }
      val centerAxis = if (isVertical) center.y else center.x
      
      // inactive track (centered or range slider)
      var rangeInactiveTrackThreshold = sliderStart + startGap
      if (!enableCornerShrinking) {
         rangeInactiveTrackThreshold += cornerSize
      }
      val adjustedSliderValueEnd =
         if (isCentered) {
            min(sliderValueEnd, centerAxis)
         } else {
            sliderValueStart
         }
      if ((isCentered || isRangeSlider) && adjustedSliderValueEnd > rangeInactiveTrackThreshold) {
         val startCornerRadius = if (isRtlHorizontal) insideCornerSize else cornerSize
         val endCornerRadius = if (isRtlHorizontal) cornerSize else insideCornerSize
         val start = sliderStart
         val end = adjustedSliderValueEnd - startGap
         val trackOffset =
            if (isRtlHorizontal) {
               Offset(size.width - end, 0f)
            } else {
               Offset(0f, 0f)
            }
         val trackSize =
            if (isVertical) {
               Size(size.width, end - start)
            } else {
               Size(end - start, size.height)
            }
         drawTrackPath(
            orientation,
            trackOffset,
            trackSize,
            inactiveTrackColor,
            startCornerRadius,
            endCornerRadius,
         )
         
      }
      // inactive track
      var inactiveTrackThreshold = sliderEnd - endGap
      if (!enableCornerShrinking) {
         inactiveTrackThreshold -= cornerSize
      }
      val adjustedSliderValueStart =
         if (isCentered) {
            max(sliderValueEnd, centerAxis)
         } else {
            sliderValueEnd
         }
      if (adjustedSliderValueStart < inactiveTrackThreshold) {
         val startCornerRadius = if (isRtlHorizontal) cornerSize else insideCornerSize
         val endCornerRadius = if (isRtlHorizontal) insideCornerSize else cornerSize
         val start = adjustedSliderValueStart + endGap
         val end = sliderEnd
         val inactiveTrackWidth = end - start
         val trackOffset =
            if (isVertical) {
               Offset(0f, start)
            } else if (isRtl) {
               Offset(0f, 0f)
            } else {
               Offset(start, 0f)
            }
         val size =
            if (isVertical) {
               Size(size.width, inactiveTrackWidth)
            } else if (isRtl && !isRangeSlider) {
               Size(size.width - start, size.height)
            } else {
               Size(inactiveTrackWidth, size.height)
            }
         drawTrackPath(
            orientation,
            trackOffset,
            size,
            inactiveTrackColor,
            startCornerRadius,
            endCornerRadius,
         )
         
      }
      // active track
      val activeTrackStart =
         if (isCentered) {
            adjustedSliderValueEnd + if (adjustedSliderValueEnd < centerAxis) startGap else 0f
         } else if (isRangeSlider) {
            sliderValueStart + startGap
         } else {
            0f
         }
      val activeTrackEnd =
         if (isCentered) {
            adjustedSliderValueStart - if (adjustedSliderValueStart > centerAxis) endGap else 0f
         } else {
            sliderValueEnd - endGap
         }
      val startCornerRadius =
         if (isRtlHorizontal || isCentered || isRangeSlider) insideCornerSize else cornerSize
      val endCornerRadius =
         if (isRtlHorizontal && !isCentered && !isRangeSlider) cornerSize else insideCornerSize
      val activeTrackWidth =
         if (isRtlHorizontal && !isCentered && !isRangeSlider) activeTrackEnd
         else activeTrackEnd - activeTrackStart
      
      val activeTrackThreshold =
         if (!enableCornerShrinking) startCornerRadius else 0f
      if (activeTrackWidth > activeTrackThreshold) {
         val trackOffset =
            if (isVertical) {
               Offset(0f, activeTrackStart)
            } else if (isRtl) {
               Offset(size.width - activeTrackEnd, 0f)
            } else {
               Offset(activeTrackStart, 0f)
            }
         
         val size =
            if (isVertical) {
               Size(size.width, activeTrackWidth)
            } else if (isRtl && !isCentered && !isRangeSlider) {
               Size(activeTrackEnd, size.height)
            } else {
               Size(activeTrackWidth, size.height)
            }
         drawTrackPath(
            orientation,
            trackOffset,
            size,
            activeTrackColor,
            startCornerRadius,
            endCornerRadius,
         )
      }
      
      val start = sliderStart + cornerSize
      val end = sliderEnd - cornerSize
      val activeTrack = activeTrackStart..activeTrackEnd
      val tickCenterGap = centerAxis - endGap..centerAxis + endGap
      val tickStartGap = sliderValueStart - startGap..sliderValueStart + startGap
      val tickEndGap = sliderValueEnd - endGap..sliderValueEnd + endGap
      
   }
   
   private fun DrawScope.drawTrackPath(
      orientation: Orientation,
      offset: Offset,
      size: Size,
      color: Color,
      startCornerRadius: Float,
      endCornerRadius: Float,
   ) {
      val startCorner = CornerRadius(startCornerRadius, startCornerRadius)
      val endCorner = CornerRadius(endCornerRadius, endCornerRadius)
      val track =
         if (orientation == Vertical) {
            RoundRect(
               rect = Rect(offset, size = Size(size.width, size.height)),
               topLeft = startCorner,
               topRight = startCorner,
               bottomRight = endCorner,
               bottomLeft = endCorner,
            )
         } else {
            RoundRect(
               rect = Rect(offset, size = Size(size.width, size.height)),
               topLeft = startCorner,
               topRight = endCorner,
               bottomRight = endCorner,
               bottomLeft = startCorner,
            )
         }
      trackPath.addRoundRect(track)
      drawPath(trackPath, color)
      trackPath.rewind()
   }
   
   /**
    * The Default stop indicator.
    *
    * @param offset the coordinate where the indicator is to be drawn.
    * @param size the size of the indicator.
    * @param color the color of the indicator.
    */
   fun DrawScope.drawStopIndicator(offset: Offset, size: Dp, color: Color) {
      drawCircle(color = color, center = offset, radius = size.toPx() / 2f)
   }
   
   /** The default size for the stop indicator at the end of the track. */
   val TrackStopIndicatorSize: Dp = 5.dp
   
   /** The default size for the ticks if steps are greater than 0. */
   val TickSize: Dp = 5.dp
   
   private val trackPath = Path()
}

private fun snapValueToTick(
   current: Float,
   tickFractions: FloatArray,
   minPx: Float,
   maxPx: Float,
): Float {
   // target is a closest anchor to the `current`, if exists
   return tickFractions
      .minByOrNull { abs(lerp(minPx, maxPx, it) - current) }
      ?.run { lerp(minPx, maxPx, this) } ?: current
}

private suspend fun AwaitPointerEventScope.awaitSlop(
   id: PointerId,
   type: PointerType,
): Pair<PointerInputChange, Float>? {
   var initialDelta = 0f
   val postPointerSlop = { pointerInput: PointerInputChange, offset: Float ->
      pointerInput.consume()
      initialDelta = offset
   }
   val afterSlopResult = awaitHorizontalPointerSlopOrCancellation(id, type, postPointerSlop)
   return if (afterSlopResult != null) afterSlopResult to initialDelta else null
}

private fun stepsToTickFractions(steps: Int): FloatArray {
   return if (steps == 0) floatArrayOf() else FloatArray(steps + 2) { it.toFloat() / (steps + 1) }
}

// Scale x1 from a1..b1 range to a2..b2 range
private fun scale(a1: Float, b1: Float, x1: Float, a2: Float, b2: Float) =
   lerp(a2, b2, calcFraction(a1, b1, x1))

// Scale x.start, x.endInclusive from a1..b1 range to a2..b2 range
private fun scale(
   isStart: Boolean,
   a1: Float,
   b1: Float,
   x: SliderRange,
   a2: Float,
   b2: Float,
): SliderRange {
   val start = scale(a1, b1, x.start, a2, b2)
   val end = scale(a1, b1, x.endInclusive, a2, b2)
   
   return if (isStart) {
      SliderRange(start.coerceAtMost(end), end)
   } else {
      SliderRange(start, end.coerceAtLeast(start))
   }
}

// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
private fun calcFraction(a: Float, b: Float, pos: Float) =
   (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

@OptIn(ExperimentalMaterial3Api::class)
private fun Modifier.sliderSemantics(state: SliderState, enabled: Boolean): Modifier {
   return semantics {
      if (!enabled) disabled()
      stateDescription = state.value.formatForSemantics()
      setProgress(
         action = { targetValue ->
            var newValue =
               targetValue.coerceIn(state.valueRange.start, state.valueRange.endInclusive)
            val originalVal = newValue
            val resolvedValue = newValue
            
            
            // This is to keep it consistent with AbsSeekbar.java: return false if no
            // change from current.
            if (resolvedValue == state.value) {
               false
            } else {
               if (resolvedValue != state.value) {
                  if (state.onValueChange != null) {
                     state.onValueChange?.let { it(resolvedValue) }
                  } else {
                     state.value = resolvedValue
                  }
               }
               state.onValueChangeFinished?.invoke()
               true
            }
         }
      )
   }
      .progressSemantics(
         state.value,
         state.valueRange.start..state.valueRange.endInclusive
      )
}

@OptIn(ExperimentalMaterial3Api::class)
private fun Modifier.rangeSliderStartThumbSemantics(
   state: RangeSliderState,
   enabled: Boolean,
): Modifier {
   val valueRange = state.valueRange.start..state.activeRangeEnd
   return semantics {
      if (!enabled) disabled()
      stateDescription = state.activeRangeStart.formatForSemantics()
      setProgress(
         action = { targetValue ->
            var newValue = targetValue.coerceIn(valueRange.start, valueRange.endInclusive)
            val originalVal = newValue
            val resolvedValue =
               if (state.startSteps > 0) {
                  var distance: Float = newValue
                  for (i in 0..state.startSteps + 1) {
                     val stepValue =
                        lerp(
                           valueRange.start,
                           valueRange.endInclusive,
                           i.toFloat() / (state.startSteps + 1),
                        )
                     if (abs(stepValue - originalVal) <= distance) {
                        distance = abs(stepValue - originalVal)
                        newValue = stepValue
                     }
                  }
                  newValue
               } else {
                  newValue
               }
            
            // This is to keep it consistent with AbsSeekbar.java: return false if no
            // change from current.
            if (resolvedValue == state.activeRangeStart) {
               false
            } else {
               val resolvedRange = SliderRange(resolvedValue, state.activeRangeEnd)
               val activeRange = SliderRange(state.activeRangeStart, state.activeRangeEnd)
               if (resolvedRange != activeRange) {
                  if (state.onValueChange != null) {
                     state.onValueChange?.let { it(resolvedRange) }
                  } else {
                     state.activeRangeStart = resolvedRange.start
                     state.activeRangeEnd = resolvedRange.endInclusive
                  }
               }
               state.onValueChangeFinished?.invoke()
               true
            }
         }
      )
   }
      .progressSemantics(state.activeRangeStart, valueRange, state.startSteps)
}

@OptIn(ExperimentalMaterial3Api::class)
private fun Modifier.rangeSliderEndThumbSemantics(
   state: RangeSliderState,
   enabled: Boolean,
): Modifier {
   val valueRange = state.activeRangeStart..state.valueRange.endInclusive
   return semantics {
      if (!enabled) disabled()
      stateDescription = state.activeRangeEnd.formatForSemantics()
      setProgress(
         action = { targetValue ->
            var newValue = targetValue.coerceIn(valueRange.start, valueRange.endInclusive)
            val originalVal = newValue
            val resolvedValue =
               if (state.endSteps > 0) {
                  var distance: Float = newValue
                  for (i in 0..state.endSteps + 1) {
                     val stepValue =
                        lerp(
                           valueRange.start,
                           valueRange.endInclusive,
                           i.toFloat() / (state.endSteps + 1),
                        )
                     if (abs(stepValue - originalVal) <= distance) {
                        distance = abs(stepValue - originalVal)
                        newValue = stepValue
                     }
                  }
                  newValue
               } else {
                  newValue
               }
            
            // This is to keep it consistent with AbsSeekbar.java: return false if no
            // change from current.
            if (resolvedValue == state.activeRangeEnd) {
               false
            } else {
               val resolvedRange = SliderRange(state.activeRangeStart, resolvedValue)
               val activeRange = SliderRange(state.activeRangeStart, state.activeRangeEnd)
               if (resolvedRange != activeRange) {
                  if (state.onValueChange != null) {
                     state.onValueChange?.let { it(resolvedRange) }
                  } else {
                     state.activeRangeStart = resolvedRange.start
                     state.activeRangeEnd = resolvedRange.endInclusive
                  }
               }
               state.onValueChangeFinished?.invoke()
               true
            }
         }
      )
   }
      .progressSemantics(state.activeRangeEnd, valueRange, state.endSteps)
}

private fun Float.formatForSemantics() = "${(this * 100).roundToInt() / 100f}"

@OptIn(ExperimentalMaterial3Api::class)
@Stable
private fun Modifier.sliderTapModifier(
   state: SliderState,
   interactionSource: MutableInteractionSource,
   enabled: Boolean,
) =
   if (enabled) {
      pointerInput(state, interactionSource) {
         detectTapGestures(
            onPress = { state.onPress(it) },
            onTap = {
               state.dispatchRawDelta(0f)
               state.gestureEndAction()
            },
         )
      }
   } else {
      this
   }

@OptIn(ExperimentalMaterial3Api::class)
@Stable
private fun Modifier.rangeSliderPressDragModifier(
   state: RangeSliderState,
   startInteractionSource: MutableInteractionSource,
   endInteractionSource: MutableInteractionSource,
   enabled: Boolean,
): Modifier =
   if (enabled) {
      pointerInput(startInteractionSource, endInteractionSource, state) {
         val rangeSliderLogic =
            RangeSliderLogic(state, startInteractionSource, endInteractionSource)
         coroutineScope {
            awaitEachGesture {
               val event = awaitFirstDown(requireUnconsumed = false)
               val interaction = DragInteraction.Start()
               var posX =
                  if (state.isRtl) state.totalWidth - event.position.x else event.position.x
               val compare = rangeSliderLogic.compareOffsets(posX)
               var draggingStart =
                  if (compare != 0) {
                     compare < 0
                  } else {
                     state.rawOffsetStart > posX
                  }
               
               awaitSlop(event.id, event.type)?.let {
                  val slop = viewConfiguration.pointerSlop(event.type)
                  val shouldUpdateCapturedThumb =
                     abs(state.rawOffsetEnd - posX) < slop &&
                        abs(state.rawOffsetStart - posX) < slop
                  if (shouldUpdateCapturedThumb) {
                     val dir = it.second
                     draggingStart = if (state.isRtl) dir >= 0f else dir < 0f
                     posX += it.first.positionChange().x
                  }
               }
               
               rangeSliderLogic.captureThumb(
                  draggingStart,
                  posX,
                  interaction,
                  this@coroutineScope,
               )
               
               val finishInteraction =
                  try {
                     state.isDragging = true
                     val success =
                        horizontalDrag(pointerId = event.id) {
                           val deltaX = it.positionChange().x
                           state.onDrag(
                              draggingStart,
                              if (state.isRtl) -deltaX else deltaX,
                           )
                        }
                     if (success) {
                        DragInteraction.Stop(interaction)
                     } else {
                        DragInteraction.Cancel(interaction)
                     }
                  } catch (e: CancellationException) {
                     DragInteraction.Cancel(interaction)
                  } finally {
                     state.isDragging = false
                  }
               
               state.gestureEndAction(draggingStart)
               launch {
                  rangeSliderLogic.activeInteraction(draggingStart).emit(finishInteraction)
               }
            }
         }
      }
   } else {
      this
   }

@OptIn(ExperimentalMaterial3Api::class)
private class RangeSliderLogic(
   val state: RangeSliderState,
   val startInteractionSource: MutableInteractionSource,
   val endInteractionSource: MutableInteractionSource,
)
{
   fun activeInteraction(draggingStart: Boolean): MutableInteractionSource =
      if (draggingStart) startInteractionSource else endInteractionSource
   
   fun compareOffsets(eventX: Float): Int {
      val diffStart = abs(state.rawOffsetStart - eventX)
      val diffEnd = abs(state.rawOffsetEnd - eventX)
      return diffStart.compareTo(diffEnd)
   }
   
   fun captureThumb(
      draggingStart: Boolean,
      posX: Float,
      interaction: Interaction,
      scope: CoroutineScope,
   ) {
      state.onDrag(
         draggingStart,
         posX - if (draggingStart) state.rawOffsetStart else state.rawOffsetEnd,
      )
      scope.launch { activeInteraction(draggingStart).emit(interaction) }
   }
}

/**
 * Represents the color used by a [Slider] in different states.
 *
 * @param thumbColor thumb color when enabled
 * @param activeTrackColor color of the track in the part that is "active", meaning that the thumb
 *   is ahead of it
 * @param activeTickColor colors to be used to draw tick marks on the active track, if `steps` is
 *   specified
 * @param inactiveTrackColor color of the track in the part that is "inactive", meaning that the
 *   thumb is before it
 * @param inactiveTickColor colors to be used to draw tick marks on the inactive track, if `steps`
 *   are specified on the Slider is specified
 * @param disabledThumbColor thumb colors when disabled
 * @param disabledActiveTrackColor color of the track in the "active" part when the Slider is
 *   disabled
 * @param disabledActiveTickColor colors to be used to draw tick marks on the active track when
 *   Slider is disabled and when `steps` are specified on it
 * @param disabledInactiveTrackColor color of the track in the "inactive" part when the Slider is
 *   disabled
 * @param disabledInactiveTickColor colors to be used to draw tick marks on the inactive part of the
 *   track when Slider is disabled and when `steps` are specified on it
 * @constructor create an instance with arbitrary colors. See [SliderDefaults.colors] for the
 *   default implementation that follows Material specifications.
 */
@Immutable
class SliderColors(
   val thumbColor: Color,
   val activeTrackColor: Color,
   val activeTickColor: Color,
   val inactiveTrackColor: Color,
   val inactiveTickColor: Color,
   val disabledThumbColor: Color,
   val disabledActiveTrackColor: Color,
   val disabledActiveTickColor: Color,
   val disabledInactiveTrackColor: Color,
   val disabledInactiveTickColor: Color,
)
{
   
   /**
    * Returns a copy of this SelectableChipColors, optionally overriding some of the values. This
    * uses the Color.Unspecified to mean “use the value from the source”
    */
   fun copy(
      thumbColor: Color = this.thumbColor,
      activeTrackColor: Color = this.activeTrackColor,
      activeTickColor: Color = this.activeTickColor,
      inactiveTrackColor: Color = this.inactiveTrackColor,
      inactiveTickColor: Color = this.inactiveTickColor,
      disabledThumbColor: Color = this.disabledThumbColor,
      disabledActiveTrackColor: Color = this.disabledActiveTrackColor,
      disabledActiveTickColor: Color = this.disabledActiveTickColor,
      disabledInactiveTrackColor: Color = this.disabledInactiveTrackColor,
      disabledInactiveTickColor: Color = this.disabledInactiveTickColor,
   ) =
      SliderColors(
         thumbColor.takeOrElse { this.thumbColor },
         activeTrackColor.takeOrElse { this.activeTrackColor },
         activeTickColor.takeOrElse { this.activeTickColor },
         inactiveTrackColor.takeOrElse { this.inactiveTrackColor },
         inactiveTickColor.takeOrElse { this.inactiveTickColor },
         disabledThumbColor.takeOrElse { this.disabledThumbColor },
         disabledActiveTrackColor.takeOrElse { this.disabledActiveTrackColor },
         disabledActiveTickColor.takeOrElse { this.disabledActiveTickColor },
         disabledInactiveTrackColor.takeOrElse { this.disabledInactiveTrackColor },
         disabledInactiveTickColor.takeOrElse { this.disabledInactiveTickColor },
      )
   
   @Stable
   internal fun thumbColor(enabled: Boolean): Color =
      if (enabled) thumbColor else disabledThumbColor
   
   @Stable
   internal fun trackColor(enabled: Boolean, active: Boolean): Color =
      if (enabled) {
         if (active) activeTrackColor else inactiveTrackColor
      } else {
         if (active) disabledActiveTrackColor else disabledInactiveTrackColor
      }
   
   @Stable
   internal fun tickColor(enabled: Boolean, active: Boolean): Color =
      if (enabled) {
         if (active) activeTickColor else inactiveTickColor
      } else {
         if (active) disabledActiveTickColor else disabledInactiveTickColor
      }
   
   override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || other !is SliderColors) return false
      
      if (thumbColor != other.thumbColor) return false
      if (activeTrackColor != other.activeTrackColor) return false
      if (activeTickColor != other.activeTickColor) return false
      if (inactiveTrackColor != other.inactiveTrackColor) return false
      if (inactiveTickColor != other.inactiveTickColor) return false
      if (disabledThumbColor != other.disabledThumbColor) return false
      if (disabledActiveTrackColor != other.disabledActiveTrackColor) return false
      if (disabledActiveTickColor != other.disabledActiveTickColor) return false
      if (disabledInactiveTrackColor != other.disabledInactiveTrackColor) return false
      if (disabledInactiveTickColor != other.disabledInactiveTickColor) return false
      
      return true
   }
   
   override fun hashCode(): Int {
      var result = thumbColor.hashCode()
      result = 31 * result + activeTrackColor.hashCode()
      result = 31 * result + activeTickColor.hashCode()
      result = 31 * result + inactiveTrackColor.hashCode()
      result = 31 * result + inactiveTickColor.hashCode()
      result = 31 * result + disabledThumbColor.hashCode()
      result = 31 * result + disabledActiveTrackColor.hashCode()
      result = 31 * result + disabledActiveTickColor.hashCode()
      result = 31 * result + disabledInactiveTrackColor.hashCode()
      result = 31 * result + disabledInactiveTickColor.hashCode()
      return result
   }
}

// Internal to be referred to in tests
// Default values
internal val TrackHeight = 15.dp
internal val ThumbWidth = 5.dp
private val ThumbHeight = 30.dp
private val ThumbSize = DpSize(ThumbWidth, ThumbHeight)
private val VerticalThumbSize = DpSize(ThumbHeight, ThumbWidth)
private val ThumbTrackGapSize: Dp = 5.dp
private val TrackInsideCornerSize: Dp = 2.dp

private enum class SliderComponents {
   THUMB,
   TRACK,
}

private enum class RangeSliderComponents {
   ENDTHUMB,
   STARTTHUMB,
   TRACK,
}




/**
 * Class that holds information about [Slider]'s active range.
 *
 * @param value [Float] that indicates the initial position of the thumb. If outside of [valueRange]
 *   provided, value will be coerced to this range.
 * @param steps if positive, specifies the amount of discrete allowable values between the endpoints
 *   of [valueRange]. For example, a range from 0 to 10 with 4 [steps] allows 4 values evenly
 *   distributed between 0 and 10 (i.e., 2, 4, 6, 8). If [steps] is 0, the slider will behave
 *   continuously and allow any value from the range. Must not be negative.
 * @param onValueChangeFinished lambda to be invoked when value change has ended. This callback
 *   shouldn't be used to update the range slider values (use [onValueChange] for that), but rather
 *   to know when the user has completed selecting a new value by ending a drag or a click.
 * @param valueRange range of values that Slider values can take. [value] will be coerced to this
 *   range.
 */
@ExperimentalMaterial3Api
open class SliderState(
   value: Float = 0f,
   var onValueChangeFinished: (() -> Unit)? = null,
   val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) : DraggableState
{
   
   internal var valueState by mutableFloatStateOf(value)
   
   /** [Float] that indicates the value that the thumb currently is in respect to the track. */
   var value: Float
      set(newVal) {
         valueState = newVal
      }
      get() = valueState
   
   var onDispatchRawDelta:(Float)->Unit = {}
   
   override suspend fun drag(
      dragPriority: MutatePriority,
      block: suspend DragScope.() -> Unit,
   ): Unit = coroutineScope {
      isDragging = true
      scrollMutex.mutateWith(dragScope, dragPriority, block)
      isDragging = false
   }
   
   var lastValue:Float = 0f
   
   override fun dispatchRawDelta(delta: Float) {
      val maxPx: Float
      val minPx: Float
      if (orientation == Vertical) {
         maxPx = max(totalHeight - thumbHeight / 2f, 0f)
         minPx = min(thumbHeight / 2f, maxPx)
      } else {
         maxPx = max(totalWidth - thumbWidth / 2f, 0f)
         minPx = min(thumbWidth / 2f, maxPx)
      }
      rawOffset = (rawOffset + delta + pressOffset)
      pressOffset = 0f
      val scaledUserValue = scaleToUserValue(minPx, maxPx, rawOffset)
      
      if (scaledUserValue != this.value) {
         if (onValueChange != null) {
            onValueChange?.let { it(scaledUserValue) }
         } else {
            this.value = scaledUserValue
         }
      }
      
      val maxTrackPx: Float
      val minTrackPx: Float
      
      if(orientation == Vertical){
         maxTrackPx = max(totalHeight.toFloat(), 0f)
         minTrackPx = min(0f, maxPx)
      } else {
         maxTrackPx = max(totalWidth.toFloat(), 0f)
         minTrackPx = min(0f, maxPx)
      }
      if(scaledUserValue <= valueRange.endInclusive && scaledUserValue >= valueRange.start)
         onDispatchRawDelta.invoke(rawOffset.coerceIn(minTrackPx,maxTrackPx))
      
      lastValue = scaledUserValue
   }
   
   /** Callback in which value should be updated. */
   var onValueChange: ((Float) -> Unit)? = null
   
   
   private var totalWidth by mutableIntStateOf(0)
   private var totalHeight by mutableIntStateOf(0)
   internal var isRtl = false
   internal var thumbWidth by mutableIntStateOf(0)
   internal var thumbHeight by mutableIntStateOf(0)
   internal var orientation by mutableStateOf( Horizontal)
   internal var reverseVerticalDirection by mutableStateOf(false)
   
   /** The fraction of the track that the thumb currently is in. */
   val coercedValueAsFraction: Float
      get() =
         calcFraction(
            valueRange.start,
            valueRange.endInclusive,
            value.coerceIn(valueRange.start, valueRange.endInclusive),
         )
   
   var isDragging by mutableStateOf(false)
      private set
   
   internal fun updateDimensions(newTotalWidth: Int, newTotalHeight: Int) {
      totalWidth = newTotalWidth
      totalHeight = newTotalHeight
   }
   
   internal val gestureEndAction = {
      if (!isDragging) {
         // check isDragging in case the change is still in progress (touch -> drag case)
         onValueChangeFinished?.invoke()
      }
   }
   
   internal fun onPress(pos: Offset) {
      val to =
         if (orientation == Vertical) {
            if (reverseVerticalDirection) totalHeight - pos.y else pos.y
         } else {
            if (isRtl) totalWidth - pos.x else pos.x
         }
      pressOffset = to - rawOffset
   }
   
   private var rawOffset by mutableFloatStateOf(scaleToOffset(0f, 0f, value))
   private var pressOffset by mutableFloatStateOf(0f)
   private val dragScope: DragScope =
      object : DragScope {
         override fun dragBy(pixels: Float): Unit = dispatchRawDelta(pixels)
      }
   
   private val scrollMutex = MutatorMutex()
   
   private fun scaleToUserValue(minPx: Float, maxPx: Float, offset: Float) =
      scale(minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)
   
   private fun scaleToOffset(minPx: Float, maxPx: Float, userValue: Float) =
      scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)
   
   companion object {
      /**
       * The default [Saver] implementation for [SliderState].
       *
       * @param onValueChangeFinished lambda to be invoked when value change has ended. This
       *   callback shouldn't be used to update the range slider values (use [onValueChange] for
       *   that), but rather to know when the user has completed selecting a new value by ending a
       *   drag or a click.
       * @param valueRange range of values that Slider values can take. [value] will be coerced to
       *   this range.
       */
      fun Saver(
         onValueChangeFinished: (() -> Unit)?,
         valueRange: ClosedFloatingPointRange<Float>,
      ): Saver<SliderState, *> =
         listSaver(
            save = { listOf(it.value) },
            restore = {
               SliderState(
                  value = it[0],
                  onValueChangeFinished = onValueChangeFinished,
                  valueRange = valueRange,
               )
            },
         )
   }
}

/**
 * Creates a [SliderState] that is remembered across compositions.
 *
 * Changes to the provided initial values will **not** result in the state being recreated or
 * changed in any way if it has already been created.
 *
 * @param value [Float] that indicates the initial position of the thumb. If outside of [valueRange]
 *   provided, value will be coerced to this range.
 * @param steps if positive, specifies the amount of discrete allowable values between the endpoints
 *   of [valueRange]. For example, a range from 0 to 10 with 4 [steps] allows 4 values evenly
 *   distributed between 0 and 10 (i.e., 2, 4, 6, 8). If [steps] is 0, the slider will behave
 *   continuously and allow any value from the range. Must not be negative.
 * @param onValueChangeFinished lambda to be invoked when value change has ended. This callback
 *   shouldn't be used to update the range slider values (use [SliderState.onValueChange] for that),
 *   but rather to know when the user has completed selecting a new value by ending a drag or a
 *   click.
 * @param valueRange range of values that Slider values can take. [value] will be coerced to this
 *   range.
 */
@ExperimentalMaterial3Api
@Composable
fun rememberSliderState(
   value: Float = 0f,
   @IntRange(from = 0) steps: Int = 0,
   onValueChangeFinished: (() -> Unit)? = null,
   valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
): SliderState {
   return rememberSaveable(saver = SliderState.Saver(onValueChangeFinished, valueRange)) {
      SliderState(
         value = value,
         onValueChangeFinished = onValueChangeFinished,
         valueRange = valueRange,
      )
   }
}

/**
 * Class that holds information about [RangeSlider]'s active range.
 *
 * @param activeRangeStart [Float] that indicates the initial start of the active range of the
 *   slider. If outside of [valueRange] provided, value will be coerced to this range.
 * @param activeRangeEnd [Float] that indicates the initial end of the active range of the slider.
 *   If outside of [valueRange] provided, value will be coerced to this range.
 * @param steps if positive, specifies the amount of discrete allowable values between the endpoints
 *   of [valueRange]. For example, a range from 0 to 10 with 4 [steps] allows 4 values evenly
 *   distributed between 0 and 10 (i.e., 2, 4, 6, 8). If [steps] is 0, the slider will behave
 *   continuously and allow any value from the range. Must not be negative.
 * @param onValueChangeFinished lambda to be invoked when value change has ended. This callback
 *   shouldn't be used to update the range slider values (use [onValueChange] for that), but rather
 *   to know when the user has completed selecting a new value by ending a drag or a click.
 * @param valueRange range of values that Range Slider values can take. [activeRangeStart] and
 *   [activeRangeEnd] will be coerced to this range.
 */
@ExperimentalMaterial3Api
class RangeSliderState(
   activeRangeStart: Float = 0f,
   activeRangeEnd: Float = 1f,
   @IntRange(from = 0) val steps: Int = 0,
   var onValueChangeFinished: (() -> Unit)? = null,
   val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
)
{
   private var activeRangeStartState by mutableFloatStateOf(activeRangeStart)
   private var activeRangeEndState by mutableFloatStateOf(activeRangeEnd)
   
   /** [Float] that indicates the start of the current active range for the [RangeSlider]. */
   var activeRangeStart: Float
      set(newVal) {
         val coercedValue = newVal.coerceIn(valueRange.start, activeRangeEnd)
         val snappedValue =
            snapValueToTick(
               coercedValue,
               tickFractions,
               valueRange.start,
               valueRange.endInclusive,
            )
         activeRangeStartState = snappedValue
      }
      get() = activeRangeStartState
   
   /** [Float] that indicates the end of the current active range for the [RangeSlider]. */
   var activeRangeEnd: Float
      set(newVal) {
         val coercedValue = newVal.coerceIn(activeRangeStart, valueRange.endInclusive)
         val snappedValue =
            snapValueToTick(
               coercedValue,
               tickFractions,
               valueRange.start,
               valueRange.endInclusive,
            )
         activeRangeEndState = snappedValue
      }
      get() = activeRangeEndState
   
   internal var onValueChange: ((SliderRange) -> Unit)? = null
   
   internal val tickFractions = stepsToTickFractions(steps)
   
   internal var startThumbWidth by mutableFloatStateOf(0f)
   internal var startThumbHeight by mutableFloatStateOf(0f)
   internal var endThumbWidth by mutableFloatStateOf(0f)
   internal var endThumbHeight by mutableFloatStateOf(0f)
   internal var totalWidth by mutableIntStateOf(0)
   internal var rawOffsetStart by mutableFloatStateOf(0f)
   internal var rawOffsetEnd by mutableFloatStateOf(0f)
   internal var isDragging by mutableStateOf(false)
   
   internal var isRtl by mutableStateOf(false)
   
   internal val gestureEndAction: (Boolean) -> Unit = { onValueChangeFinished?.invoke() }
   
   private var maxPx by mutableFloatStateOf(0f)
   private var minPx by mutableFloatStateOf(0f)
   
   internal fun onDrag(isStart: Boolean, offset: Float) {
      val offsetRange =
         if (isStart) {
            rawOffsetStart = (rawOffsetStart + offset)
            rawOffsetEnd = scaleToOffset(minPx, maxPx, activeRangeEnd)
            val offsetEnd = rawOffsetEnd
            var offsetStart = rawOffsetStart.coerceIn(minPx, offsetEnd)
            offsetStart = snapValueToTick(offsetStart, tickFractions, minPx, maxPx)
            SliderRange(offsetStart.coerceAtMost(offsetEnd), offsetEnd)
         } else {
            rawOffsetEnd = (rawOffsetEnd + offset)
            rawOffsetStart = scaleToOffset(minPx, maxPx, activeRangeStart)
            val offsetStart = rawOffsetStart
            var offsetEnd = rawOffsetEnd.coerceIn(offsetStart, maxPx)
            offsetEnd = snapValueToTick(offsetEnd, tickFractions, minPx, maxPx)
            SliderRange(offsetStart, offsetEnd.coerceAtLeast(offsetStart))
         }
      val scaledUserValue = scaleToUserValue(isStart, minPx, maxPx, offsetRange)
      if (scaledUserValue != SliderRange(activeRangeStart, activeRangeEnd)) {
         if (onValueChange != null) {
            onValueChange?.let { it(scaledUserValue) }
         } else {
            this.activeRangeStart = scaledUserValue.start
            this.activeRangeEnd = scaledUserValue.endInclusive
         }
      }
   }
   
   internal val coercedActiveRangeStartAsFraction
      get() = calcFraction(valueRange.start, valueRange.endInclusive, activeRangeStart)
   
   internal val coercedActiveRangeEndAsFraction
      get() = calcFraction(valueRange.start, valueRange.endInclusive, activeRangeEnd)
   
   internal val startSteps
      get() = floor(steps * coercedActiveRangeEndAsFraction).toInt()
   
   internal val endSteps
      get() = floor(steps * (1f - coercedActiveRangeStartAsFraction)).toInt()
   
   // scales range offset from within minPx..maxPx to within valueRange.start..valueRange.end
   private fun scaleToUserValue(
      isStart: Boolean,
      minPx: Float,
      maxPx: Float,
      offset: SliderRange,
   ) = scale(isStart, minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)
   
   // scales float userValue within valueRange.start..valueRange.end to within minPx..maxPx
   private fun scaleToOffset(minPx: Float, maxPx: Float, userValue: Float) =
      scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)
   
   internal fun updateMinMaxPx() {
      val newMaxPx = max(totalWidth - endThumbWidth / 2, 0f)
      val newMinPx = min(startThumbWidth / 2, newMaxPx)
      if (
         !isDragging &&
         (minPx != newMinPx || maxPx != newMaxPx || activeRangeStart != activeRangeEnd)
      ) {
         minPx = newMinPx
         maxPx = newMaxPx
         rawOffsetStart = scaleToOffset(minPx, maxPx, activeRangeStart)
         rawOffsetEnd = scaleToOffset(minPx, maxPx, activeRangeEnd)
      }
   }
   
   companion object {
      /**
       * The default [Saver] implementation for [RangeSliderState].
       *
       * @param onValueChangeFinished lambda to be invoked when value change has ended. This
       *   callback shouldn't be used to update the range slider values (use [onValueChange] for
       *   that), but rather to know when the user has completed selecting a new value by ending a
       *   drag or a click.
       * @param valueRange range of values that Range Slider values can take. [activeRangeStart]
       *   and [activeRangeEnd] will be coerced to this range.
       */
      fun Saver(
         onValueChangeFinished: (() -> Unit)?,
         valueRange: ClosedFloatingPointRange<Float>,
      ): Saver<RangeSliderState, *> =
         listSaver(
            save = { listOf(it.activeRangeStart, it.activeRangeEnd, it.steps) },
            restore = {
               RangeSliderState(
                  activeRangeStart = it[0] as Float,
                  activeRangeEnd = it[1] as Float,
                  steps = it[2] as Int,
                  onValueChangeFinished = onValueChangeFinished,
                  valueRange = valueRange,
               )
            },
         )
   }
}

/**
 * Creates a [SliderState] that is remembered across compositions.
 *
 * Changes to the provided initial values will **not** result in the state being recreated or
 * changed in any way if it has already been created.
 *
 * @param activeRangeStart [Float] that indicates the initial start of the active range of the
 *   slider. If outside of [valueRange] provided, value will be coerced to this range.
 * @param activeRangeEnd [Float] that indicates the initial end of the active range of the slider.
 *   If outside of [valueRange] provided, value will be coerced to this range.
 * @param steps if positive, specifies the amount of discrete allowable values between the endpoints
 *   of [valueRange]. For example, a range from 0 to 10 with 4 [steps] allows 4 values evenly
 *   distributed between 0 and 10 (i.e., 2, 4, 6, 8). If [steps] is 0, the slider will behave
 *   continuously and allow any value from the range. Must not be negative.
 * @param onValueChangeFinished lambda to be invoked when value change has ended. This callback
 *   shouldn't be used to update the range slider values (use [RangeSliderState.onValueChange] for
 *   that), but rather to know when the user has completed selecting a new value by ending a drag or
 *   a click.
 * @param valueRange range of values that Range Slider values can take. [activeRangeStart] and
 *   [activeRangeEnd] will be coerced to this range.
 */
@ExperimentalMaterial3Api
@Composable
fun rememberRangeSliderState(
   activeRangeStart: Float = 0f,
   activeRangeEnd: Float = 1f,
   @IntRange(from = 0) steps: Int = 0,
   onValueChangeFinished: (() -> Unit)? = null,
   valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
): RangeSliderState {
   return rememberSaveable(saver = RangeSliderState.Saver(onValueChangeFinished, valueRange)) {
      RangeSliderState(
         activeRangeStart = activeRangeStart,
         activeRangeEnd = activeRangeEnd,
         steps = steps,
         onValueChangeFinished = onValueChangeFinished,
         valueRange = valueRange,
      )
   }
}

/**
 * Immutable float range for [RangeSlider]
 *
 * Used in [RangeSlider] to determine the active track range for the component. The range is as
 * follows: SliderRange.start..SliderRange.endInclusive.
 */
@Immutable
@JvmInline
internal value class SliderRange(val packedValue: Long) {
   /** start of the [SliderRange] */
   @Stable
   val start: Float
      get() {
         // Explicitly compare against packed values to avoid auto-boxing of Size.Unspecified
         check(this.packedValue != Unspecified.packedValue) { "SliderRange is unspecified" }
         return unpackFloat1(packedValue)
      }
   
   /** End (inclusive) of the [SliderRange] */
   @Stable
   val endInclusive: Float
      get() {
         // Explicitly compare against packed values to avoid auto-boxing of Size.Unspecified
         check(this.packedValue != Unspecified.packedValue) { "SliderRange is unspecified" }
         return unpackFloat2(packedValue)
      }
   
   companion object {
      /**
       * Represents an unspecified [SliderRange] value, usually a replacement for `null` when a
       * primitive value is desired.
       */
      @Stable val Unspecified = SliderRange(Float.NaN, Float.NaN)
   }
   
   /** String representation of the [SliderRange] */
   override fun toString() =
      if (isSpecified) {
         "$start..$endInclusive"
      } else {
         "FloatRange.Unspecified"
      }
}

/**
 * Creates a [SliderRange] from a given start and endInclusive float. It requires endInclusive to
 * be >= start.
 *
 * @param start float that indicates the start of the range
 * @param endInclusive float that indicates the end of the range
 */
@Stable
internal fun SliderRange(start: Float, endInclusive: Float): SliderRange {
   val isUnspecified = start.isNaN() && endInclusive.isNaN()
   
   require(isUnspecified || start <= endInclusive) {
      "start($start) must be <= endInclusive($endInclusive)"
   }
   return SliderRange(packFloats(start, endInclusive))
}

/**
 * Creates a [SliderRange] from a given [ClosedFloatingPointRange]. It requires
 * range.endInclusive >= range.start.
 *
 * @param range the ClosedFloatingPointRange<Float> for the range.
 */
@Stable
internal fun SliderRange(range: ClosedFloatingPointRange<Float>): SliderRange {
   val start = range.start
   val endInclusive = range.endInclusive
   val isUnspecified = start.isNaN() && endInclusive.isNaN()
   require(isUnspecified || start <= endInclusive) {
      "ClosedFloatingPointRange<Float>.start($start) must be <= " +
         "ClosedFloatingPoint.endInclusive($endInclusive)"
   }
   return SliderRange(packFloats(start, endInclusive))
}

/** Check for if a given [SliderRange] is not [SliderRange.Unspecified]. */
@Stable
internal val SliderRange.isSpecified: Boolean
   get() = packedValue != SliderRange.Unspecified.packedValue

internal val CornerSizeAlignmentLine = VerticalAlignmentLine(::min)


private val mouseSlop = 0.125.dp
private val defaultTouchSlop = 18.dp // The default touch slop on Android devices
private val mouseToTouchSlopRatio = mouseSlop / defaultTouchSlop
internal fun ViewConfiguration.pointerSlop(pointerType: PointerType): Float {
   return when (pointerType) {
      PointerType.Mouse -> touchSlop * mouseToTouchSlopRatio
      else -> touchSlop
   }
}


@OptIn(ExperimentalMaterial3Api::class)
class SliderGroupState // no empty constructor()
{
   constructor(
      orientation: Orientation,
      reverseDirection: Boolean,
      vararg states: SliderState
   ){
      this.orientation = orientation
      this.reverseVerticalDirection = reverseDirection
      this.states = states.toList()
   }
   
   constructor(
      orientation: Orientation,
      reverseDirection: Boolean,
      states:List<SliderState>
   ){
      this.orientation = orientation
      this.reverseVerticalDirection = reverseDirection
      this.states = states.toList()
   }
   
   
   var job:Job? = null
   val scope = CoroutineScope(Dispatchers.Default)
   
   var states:List<SliderState> by mutableStateOf(emptyList())
      private set
   
   var orientation by mutableStateOf(Orientation.Vertical)
      private set
   fun updateOrientation(orientation:Orientation){
      job?.cancel()
      job = scope.launch {
         // here i use apply because i need to work with a snapshot of
         // the list of states. To not produce an error in case the list has been replaced mid-iteration
         states = ArrayList(states).apply { this.forEach { it.orientation = orientation } }
         this@SliderGroupState.orientation = orientation
         
         job = null
      }
   }
   
   var reverseVerticalDirection by mutableStateOf(false)
      private set
   fun updateVerticalDirection(reverseDirection:Boolean){
      job?.cancel()
      job = scope.launch {
         states = ArrayList(states).apply { this.forEach { it.reverseVerticalDirection = reverseDirection } }
         this@SliderGroupState.reverseVerticalDirection = reverseDirection
         
         job = null
      }
   }
   
   /**
    * Manually override current values
    *
    * @throws IndexOutOfBoundsException if the provided calculation list's size is not equal to the size of the list of previous values
    * */
   fun setValues(calculation:(previousValues:List<Float>)->List<Float>){
      val valuesList = calculation.invoke(states.map { it.value })
      states.fastForEachIndexed { index,it->
         it.value = valuesList[index]
      }
   }
   
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSliderGroupState(
   @IntRange(from = 0) count:Int,
   orientation:Orientation = Vertical,
   reverseVerticalDirection:Boolean = true
): SliderGroupState{
   return remember(count) {
      SliderGroupState(
         orientation,
         reverseVerticalDirection,
         *List(count){
            SliderState().apply {
               this.reverseVerticalDirection = reverseVerticalDirection
            }
         }.toTypedArray())
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSliderGroupState(
   @FloatRange(from = 0.0,to = 1.0) values:List< Float>,
   orientation:Orientation = Vertical,
   reverseVerticalDirection:Boolean = true
): SliderGroupState{
   return remember(values) {
      SliderGroupState(
         orientation,
         reverseVerticalDirection,
            values.map { value->
               SliderState(value = value).apply{
               this.reverseVerticalDirection = reverseVerticalDirection
            } }
         )
   }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderGroup(
   state: SliderGroupState,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   colors: SliderColors = SliderDefaults.colors(),
   thumb: @Composable (SliderState, Modifier) -> Unit = { sliderState, modifier ->
      SliderDefaults.Thumb(
         interactionSource = remember { MutableInteractionSource() },
         sliderState = sliderState,
         colors = colors,
         enabled = enabled,
         thumbSize = VerticalThumbSize,
         modifier = modifier
      )
   },
   track: @Composable (SliderState) -> Unit = { sliderState ->
      
      SliderDefaults.Track(
         colors = colors,
         enabled = enabled,
         sliderState = sliderState,
         trackCornerSize = Dp.Unspecified,
         orientation = state.orientation,
         reverseDirection = state.reverseVerticalDirection
      )
   },
   startEndGap: Dp = 0.dp,
   spacing:Dp = 0.dp
) {
   
   Box(
      modifier.clipToBounds()
   ) {
      val scroll = rememberScrollState()
      
      var childLayoutCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
      
      // Content
      when (state.orientation) {
         Vertical -> {
            Row(
               modifier = Modifier
                  .matchParentSize()
                  .horizontalScroll(scroll, overscrollEffect = null)
                  .onGloballyPositioned {
                     childLayoutCoords = it
                  }
            ) {
               
               
               Spacer(
                  Modifier
                     .fillMaxHeight()
                     .width(startEndGap)
               )
               
               
               state.states.forEachIndexed { index, internalState ->
                  
                  VerticalSlider(
                     state = internalState,
                     reverseDirection = state.reverseVerticalDirection,
                     enabled = enabled,
                     colors = colors,
                     thumb = {
                        thumb.invoke(
                           it,
                           Modifier
                        )
                     },
                     track = track
                  )
                  
                  // Render gap
                  if (index != state.states.lastIndex) {
                     Spacer(
                        Modifier
                           .fillMaxHeight()
                           .width(spacing)
                     )
                  }
                  
               }
               
               
               Spacer(
                  Modifier
                     .fillMaxHeight()
                     .width(startEndGap)
               )
               
            }
            
         }
         
         Horizontal -> {
            Column(
               Modifier
                  .matchParentSize()
                  .verticalScroll(scroll, overscrollEffect = null)
                  .onGloballyPositioned {
                     childLayoutCoords = it
                  }
            ) {
               
               Spacer(
                  Modifier
                     .fillMaxWidth()
                     .height(startEndGap)
               )
               
               state.states.forEachIndexed { index, itemState ->
                  // Doesn't require reverseDirection since it handles RTL by itself
                  Slider(
                     state = itemState,
                     enabled = enabled,
                     colors = colors,
                     thumb = {
                        thumb.invoke(
                           it,
                           Modifier
                        )
                     },
                     track = track
                  )
                  
                  // Render Gap
                  if (index != state.states.lastIndex) {
                     Spacer(
                        Modifier
                           .fillMaxWidth()
                           .height(spacing)
                     )
                  }
                  
               }
               
               Spacer(
                  Modifier
                     .fillMaxWidth()
                     .height(startEndGap)
               )
               
            }
            
            
         }
      }
      
   }
   
}





private fun calcStartX(scrollState: Int):Float{
   return (scrollState * -1).toFloat()
}
private fun calcEndX(childLayoutCoords: LayoutCoordinates?, scrollState: Int):Float{
   return childLayoutCoords?.let { it.size.width.toFloat()-scrollState }?:0f
}
private fun calcEndY(childLayoutCoords: LayoutCoordinates?, scrollState: Int):Float{
   return childLayoutCoords?.let { it.size.height.toFloat()-scrollState }?:0f
}
@OptIn(ExperimentalMaterial3Api::class)
private fun calculateStartPoint(
   startX: Float,
   endX: Float,
   childLayoutCoords: LayoutCoordinates?,
   lineStartValue: Float?,
   isRtl: Boolean,
   sliderGroupState: SliderGroupState,
   enforcedTrackHeightPx: Int?,
):Offset?{
   return if(childLayoutCoords!=null) {
      
      if (lineStartValue != null) {
         when (sliderGroupState.orientation) {
            Vertical -> {
               Offset(
                  x = if (!isRtl) startX else endX,
                  y = childLayoutCoords.let {
                     when (sliderGroupState.reverseVerticalDirection) {
                        // Cases for vertical orientation
                        true -> {
                           if(enforcedTrackHeightPx!=null){
                              enforcedTrackHeightPx * (1f - lineStartValue)
                           }else{
                              it.size.height * (1f - lineStartValue)
                           }
                           
                        }
                        
                        false -> {
                           if(enforcedTrackHeightPx!=null){
                              enforcedTrackHeightPx * lineStartValue
                           }else{
                              it.size.height * lineStartValue
                           }
                        }
                     }
                  }
               )
            }
            
            Horizontal -> {
               Offset(
                  y = startX,
                  x = childLayoutCoords.let {
                     when (isRtl) {
                        false -> {
                           if(enforcedTrackHeightPx!=null){
                              enforcedTrackHeightPx * lineStartValue
                           }else{
                              it.size.width * lineStartValue
                           }
                           
                        }
                        
                        true -> {
                           if(enforcedTrackHeightPx!=null){
                              enforcedTrackHeightPx * (1f - lineStartValue)
                           }else{
                              it.size.width * (1f - lineStartValue)
                           }
                           
                        }
                     }
                  }
               )
               
            }
         }
      }else {null}
   } else {null}
}
@OptIn(ExperimentalMaterial3Api::class)
private fun calculateEndPoint(
   startX: Float,
   endX: Float,
   endY:Float,
   childLayoutCoords: LayoutCoordinates?,
   lineEndValue: Float?,
   isRtl: Boolean,
   sliderGroupState: SliderGroupState,
   enforcedTrackHeightPx: Int?,
):Offset?{
   return if(childLayoutCoords!=null){
      
      
      if (lineEndValue != null) {
         when (sliderGroupState.orientation) {
            Vertical -> {
               Offset(
                  x = if(!isRtl) endX else startX,
                  y = childLayoutCoords?.let {
                     when (sliderGroupState.reverseVerticalDirection) {
                        // Cases for vertical orientation
                        true -> {
                           if(enforcedTrackHeightPx!=null){
                              enforcedTrackHeightPx * (1f - lineEndValue)
                           }else{
                              it.size.height * (1f - lineEndValue)
                           }
                           
                        }
                        
                        false -> {
                           if(enforcedTrackHeightPx!=null){
                              enforcedTrackHeightPx * lineEndValue
                           }else{
                              it.size.height * lineEndValue
                           }
                        }
                     }
                  } ?: 0f
               )
            }
            
            Horizontal -> {
               Offset(
                  y = endY,
                  x = childLayoutCoords?.let {
                     when (isRtl) {
                        false -> {
                           if(enforcedTrackHeightPx!=null){
                              enforcedTrackHeightPx * lineEndValue
                           }
                           else{
                              it.size.width * lineEndValue
                           }
                           
                        }
                        
                        true -> {
                           if(enforcedTrackHeightPx!=null){
                              enforcedTrackHeightPx * (1f-lineEndValue)
                           }else {
                              it.size.width * (1f - lineEndValue)
                           }
                        }
                     }
                  } ?: 0f
               )
            }
            
         }
      }else {null}
   }else {null}
}


/**
 *  Displays a group of sliders in a row or column depending on the
 *  desired orientation. A line will be drawn across the component, passsing
 *  through all track thumbs and optionally continue to the edges of the component.
 *
 *  @param state [SliderGroupState] which contains the values for all sliders peresent in this group
 *  @param modifier Modifier for the encompassing box containing the actual content. This can be used for constraining the size on screen
 *  @param enabled wether the component is enabled or not
 *  @param colors colors used by the sliders by default
 *  @param thumb thumb composable drawn at the state values of sliders
 *  @param track track drawn behind sliders
 *  @param lineStartValue what position at the START edge of the component to start the connecting line. Set to NULL to disable this functionality
 *  @param lineEndValue what position at the END edge of the component to end the connecting line. Set to NULL to disable this functionality
 *  @param lineColor color of the connecting line in [Dp]
 *  @param lineWidth width of the connecting linein [Dp]
 *  @param startEndGap gap at the start and end of the composable in [Dp]
 *  @param spacing spacing between sliders except for the start and end in [Dp]
 *  @param cubicControlFactor1X control factor for first the X coordinate of the first point relative between any thumb1 and thumb2
 *  @param cubicControlFactor1Y control factor for first the Y coordinate of the first point relative between any thumb1 and thumb2
 *  @param cubicControlFactor2X control factor for first the X coordinate of the second point relative between any thumb1 and thumb2
 *  @param cubicControlFactor2Y control factor for first the Y coordinate of the second point relative between any thumb1 and thumb2
 *
 *  Visual example for deciding control factors:
 *
 *       A--(x1,y1)
 *                \
 *                 \
 *                  |
 *                  |
 *                  \
 *                   \
 *                   (x2,y2)-- B
 *
 * @sample ConnectedSliderGroupPreview
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedSliderGroup(
   state: SliderGroupState,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   colors: SliderColors = SliderDefaults.colors(),
   thumb: @Composable (SliderState, Modifier) -> Unit = { sliderState, modifier ->
      SliderDefaults.Thumb(
         interactionSource = remember { MutableInteractionSource() },
         sliderState = sliderState,
         colors = colors,
         enabled = enabled,
         thumbSize = if(state.orientation == Vertical) VerticalThumbSize else ThumbSize,
         modifier = modifier
      )
   },
   track: @Composable (SliderState) -> Unit = { sliderState ->
      SliderDefaults.Track(
         colors = colors,
         enabled = enabled,
         sliderState = sliderState,
         trackCornerSize = Dp.Unspecified,
         orientation = state.orientation,
         reverseDirection = state.reverseVerticalDirection
      )
   },
   trackEnforcedHeight:Dp? = null,
   decoration: (@Composable (Int,@Composable ()->Unit)->Unit)? = null,
   @FloatRange(from = 0.0,to = 1.0) lineStartValue: Float? = 0.5f,
   @FloatRange(from = 0.0,to = 1.0) lineEndValue: Float? = 0.5f,
   lineColor: Color = Color.Blue,
   lineWidth: Dp = Dp.Hairline,
   startEndGap: Dp = 0.dp,
   spacing:Dp = 0.dp,
   spaceEventy:Boolean = false,
   cubicControl: CubicControl = remember { CubicControl(0.3f,0.1f,0.7f,0.9f) }
) {
   val density = LocalDensity.current
   
   val internalValidDecoration = remember(decoration) {
      if (decoration == null) {
         @Composable { index:Int, deco: @Composable () -> Unit ->
            deco.invoke()
         }
      } else {
         decoration
      }
   }
   
   var parentCoords: LayoutCoordinates? by remember { mutableStateOf(null) }
   
   val points = remember {
      SnapshotStateList<Offset>(
         state.states.size
      ) { Offset.Zero }
   }
   
   val layoutDirection = LocalLayoutDirection.current
   val isRtl = layoutDirection == LayoutDirection.Rtl
   
   
   val scroll = rememberScrollState()
   var childLayoutCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
   
   val startX by remember {
      derivedStateOf {
         calcStartX(scroll.value)
      }
   }
   
   val endX by remember {
      derivedStateOf {
         calcEndX(childLayoutCoords,scroll.value)
      }
   }
   val endY by remember {
      derivedStateOf {
         calcEndY(childLayoutCoords,scroll.value)
      }
   }
   
   
   val startPointOffset by remember(startX,endX) {
      derivedStateOf {
         if (childLayoutCoords != null && lineStartValue != null) {
            
            calculateStartPoint(
               startX = startX,
               endX = endX,
               childLayoutCoords = childLayoutCoords,
               lineStartValue = lineStartValue,
               isRtl = isRtl,
               sliderGroupState = state,
               enforcedTrackHeightPx = trackEnforcedHeight?.toPx(density)?.roundToInt()
            )
         } else null
      }
   }
   
   val endPointOffset by remember(startX,endX) {
      derivedStateOf {
         if (childLayoutCoords != null && lineEndValue != null) {
            
            calculateEndPoint(
               startX = startX,
               endX = endX,
               endY,
               childLayoutCoords = childLayoutCoords,
               lineEndValue = lineEndValue,
               isRtl = isRtl,
               sliderGroupState = state,
               enforcedTrackHeightPx = trackEnforcedHeight?.toPx(density)?.roundToInt()
            )
         } else null
      }.apply {
         Log(this.value?.y.toString())
      }
   }
   
   
   Box(
      modifier
         .onGloballyPositioned {
            parentCoords = it
         }
         .clipToBounds()
         .height(IntrinsicSize.Min) // this basically sets the intrinsic size of children to minimum, making the measurement system behave as i would expect it to
   ) {
      // Sliders group
      when (state.orientation) {
         Vertical -> {
            Row(
               modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(scroll, overscrollEffect = null)
                  .onGloballyPositioned {
                     childLayoutCoords = it
                  },
               horizontalArrangement = if (spaceEventy) Arrangement.SpaceEvenly else Arrangement.Start
            ) {
               
               if (!spaceEventy)
                  Spacer(
                     Modifier
                        .fillMaxHeight()
                        .width(startEndGap)
                  )
               
               
               state.states.forEachIndexed { index, internalState ->
                  internalValidDecoration.invoke(index) {
                     VerticalSlider(
                        modifier = Modifier.let {
                           if (trackEnforcedHeight != null) {
                              it.height(trackEnforcedHeight)
                           } else {
                              it
                           }
                        },
                        state = internalState,
                        reverseDirection = state.reverseVerticalDirection,
                        enabled = enabled,
                        colors = colors,
                        thumb = {
                           thumb.invoke(
                              it,
                              Modifier.onGloballyPositioned {
                                 points[index] =
                                    parentCoords?.localPositionOf(it)
                                       .let { off ->
                                          if (off != null) {
                                             Offset(
                                                off.x + it.size.width / 2,
                                                off.y + it.size.height / 2
                                             )
                                          } else {
                                             off
                                          }
                                          
                                       }
                                       ?: Offset.Zero
                              }
                           )
                        },
                        track = track
                     )
                  }
                  
                  // Render gap
                  if (!spaceEventy)
                     if (index != state.states.lastIndex) {
                        Spacer(
                           Modifier
                              .fillMaxHeight()
                              .width(spacing)
                        )
                     }
               }
               
               if (!spaceEventy)
                  Spacer(
                     Modifier
                        .fillMaxHeight()
                        .width(startEndGap)
                  )
               
            }
            
         }
         
         Horizontal -> {
            Column(
               Modifier
                  .verticalScroll(scroll, overscrollEffect = null)
                  .onGloballyPositioned {
                     childLayoutCoords = it
                  },
               verticalArrangement = if (spaceEventy) Arrangement.SpaceEvenly else Arrangement.Top
            ) {
               
               if (!spaceEventy)
                  Spacer(
                     Modifier
                        .fillMaxWidth()
                        .height(startEndGap)
                  )
               
               
               state.states.forEachIndexed { index, itemState ->
                  // Doesn't require reverseDirection since it handles RTL by itself
                  internalValidDecoration.invoke(index) {
                     Slider(
                        modifier = Modifier.let {
                           if (trackEnforcedHeight != null) {
                              it.width(trackEnforcedHeight)
                           } else {
                              it
                           }
                        },
                        state = itemState,
                        enabled = enabled,
                        colors = colors,
                        thumb = {
                           thumb.invoke(
                              it,
                              Modifier.onGloballyPositioned {
                                 points[index] =
                                    parentCoords?.localPositionOf(it)
                                       .let { off ->
                                          if (off != null) {
                                             Offset(
                                                off.x + it.size.width / 2,
                                                off.y + it.size.height / 2
                                             )
                                          } else {
                                             off
                                          }
                                          
                                       }
                                       ?: Offset.Zero
                              }
                           )
                        },
                        track = track
                     )
                  }
                  
                  
                  // Render Gap
                  if (!spaceEventy)
                     if (index != state.states.lastIndex) {
                        Spacer(
                           Modifier
                              .fillMaxWidth()
                              .height(spacing)
                        )
                     }
                  
               }
               
               if (!spaceEventy)
                  Spacer(
                     Modifier
                        .fillMaxWidth()
                        .height(startEndGap)
                  )
               
               
            }
            
            
         }
      }
      
      // Canvas to draw the line on
      Canvas(
         modifier = Modifier
            //This canvas composable will not itself be shown but its drawings will be rendered regardless. This isn't bad honestly
            .let {
               if (trackEnforcedHeight != null) {
                  if (state.orientation == Orientation.Vertical) {
                     it.height(trackEnforcedHeight)
                  } else {
                     it.width(trackEnforcedHeight)
                  }
               } else it
            }
      ) {
         if (points.isEmpty() || points.size < 1 + countIf(
               lineStartValue != null,
               lineEndValue != null
            )
         ) return@Canvas
         
         val path = Path()
         val _cubicControl = if (state.orientation == Orientation.Vertical) {
            cubicControl
         } else {
            cubicControl.withReversedOrientation()
         }
         
         // Start at first point
         if (startPointOffset != null) {
            startPointOffset?.apply {
               path.moveTo(this.x, this.y)
               path.cubicTo(
                  x1 = lerp(this.x, points[0].x, _cubicControl.x1),
                  y1 = lerp(this.y, points[0].y, _cubicControl.y1),
                  x2 = lerp(this.x, points[0].x, _cubicControl.x2),
                  y2 = lerp(this.y, points[0].y, _cubicControl.y2),
                  x3 = points[0].x,
                  y3 = points[0].y
               )
            }
         } else {
            path.moveTo(points[0].x, points[0].y)
         }
         
         
         // Generate smooth curve through all points
         for(i in 0..<points.lastIndex) {
            path.cubicTo(
               x1 = lerp(points[i].x, points[i + 1].x, _cubicControl.x1),
               y1 = lerp(points[i].y, points[i + 1].y, _cubicControl.y1),
               x2 = lerp(points[i].x, points[i + 1].x, _cubicControl.x2),
               y2 = lerp(points[i].y, points[i + 1].y, _cubicControl.y2),
               x3 = points[i + 1].x,
               y3 = points[i + 1].y
            )
         }
         
         endPointOffset?.apply {
            path.cubicTo(
               x1 = lerp(points[points.lastIndex].x, this.x, _cubicControl.x1),
               y1 = lerp(points[points.lastIndex].y, this.y, _cubicControl.y1),
               x2 = lerp(points[points.lastIndex].x, this.x, _cubicControl.x2),
               y2 = lerp(points[points.lastIndex].y, this.y, _cubicControl.y2),
               x3 = this.x,
               y3 = this.y
            )
         }
         
         
         drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = lineWidth.toPx(), cap = StrokeCap.Round)
         
         )
         path.rewind()
      }
      
   }
   
}



// Preview the workings of after-frame computation. This COULD be made zero-frame but i'd have to
// use more math and by this point i think a few frames of delay won't do any bad.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Preview(){
   var parentCoords: LayoutCoordinates? by remember { mutableStateOf(null) }
   
   Box(modifier = Modifier
      .fillMaxSize()
      .onGloballyPositioned { parentCoords = it }){
      var auxOffset by remember { mutableFloatStateOf(0f) }
      
      val state = rememberSliderState(value = 0f).apply {
         onDispatchRawDelta = onDispatchRawDelta.append {
            auxOffset = it
         }
      }
      
      val interactionSource = remember { MutableInteractionSource() }
      
      
      
      var childCoords: Offset by remember { mutableStateOf(Offset.Zero) }
      
      VerticalSlider(
         state = state,
         reverseDirection = true,
         modifier = Modifier
            .fillMaxSize(0.5f)
            .align(Alignment.Center),
         track = {
            SliderDefaults.Track(
               sliderState = state,
               modifier = Modifier
            
            )
         },
         thumb = {
            SliderDefaults.Thumb(
               interactionSource = interactionSource,
               sliderState = state,
               colors = SliderDefaults.colors(),
               thumbSize = VerticalThumbSize,
               modifier = Modifier.onGloballyPositioned {
                  childCoords = parentCoords?.localPositionOf(it) ?: Offset.Zero
               }
            )
         }
      )
      
      Box(
         modifier = Modifier
            .offset {
               childCoords.roundToIntOffset()
            }
            .fillMaxWidth()
            .height(5.dp)
            .background(MyColor.Yellow)
      )
      
      
      
      Text(state.value.toString(), color = MyColor.Green, modifier = Modifier.align(Alignment.TopStart))
      
      Text(auxOffset.toString(), color = MyColor.Green, modifier = Modifier.align(Alignment.BottomCenter))
      
      
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Preview1(){
   
   val state = rememberSliderGroupState(3)
   
   Box(Modifier
      .fillMaxSize()
      .background(MyColor.White)){
      SliderGroup(state, modifier = Modifier)
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConnectedSliderGroupPreview(){
   
   val state = rememberSliderGroupState(12, orientation = Horizontal, reverseVerticalDirection = true)
   
   var cubicControl by remember { mutableStateOf(CubicControl(0.3f,0.1f,0.7f,0.9f)) }
   
   val scrollstate = rememberScrollState()
   Column(Modifier
      .fillMaxSize()
      .safeDrawingPadding()
      .background(MyColor.White)
      .verticalScroll(scrollstate),
      horizontalAlignment = Alignment.CenterHorizontally
   ) {
      
      ConnectedSliderGroup(state,
         lineWidth = 5.dp,
         lineColor = Color.Green,
         thumb = { state, modifier ->
            Spacer(
               modifier
                  .size(15.dp)
                  .background(Color.Green, CircleShape)
            )
         },
         modifier = Modifier
            .height(300.dp)
            .fillMaxWidth(),
         lineStartValue = 0.5f,
         lineEndValue = 0.5f,
         cubicControl = cubicControl
      )
      
      // Controlls for testing
         OutlinedTextFieldExpanded(cubicControl.x1.toString(),{cubicControl = cubicControl.copy(x1 = it.toFloatOrNull()?:0f)}, label = {Text("x1")})
         OutlinedTextFieldExpanded(cubicControl.y1.toString(),{cubicControl = cubicControl.copy(y1 = it.toFloatOrNull()?:0f)}, label = {Text("y1")})
         OutlinedTextFieldExpanded(cubicControl.x2.toString(),{cubicControl = cubicControl.copy(x2 = it.toFloatOrNull()?:0f)}, label = {Text("x2")})
         OutlinedTextFieldExpanded(cubicControl.y2.toString(),{cubicControl = cubicControl.copy(y2 = it.toFloatOrNull()?:0f)}, label = {Text("y2")})
         
         
         Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ReverseVertical")
            Spacer(Modifier.width(10.dp))
            Switch(
               state.reverseVerticalDirection,
               onCheckedChange = {
                  state.updateVerticalDirection(it)
               }
            )
         }
         Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Horizontal Orientation")
            Spacer(Modifier.width(10.dp))
            Switch(
               state.orientation == Horizontal,
               onCheckedChange = {
                  state.updateOrientation(if(it) Horizontal else Vertical)
               }
            )
         }
         
      }
      
      
      
      LaunchedEffect(Unit) {
         state.states[0].value = 0.3f
         state.states[1].value = 0.5f
      }
   
}

data class CubicControl(
   @FloatRange(from = 0.0,to = 1.0) val x1: Float,
   @FloatRange(from = 0.0,to = 1.0) val y1: Float,
   @FloatRange(from = 0.0,to = 1.0) val x2: Float,
   @FloatRange(from = 0.0,to = 1.0) val y2: Float,
){
   fun withReversedOrientation(): CubicControl{
      return CubicControl(y1,x1,y2,x2)
   }
}
