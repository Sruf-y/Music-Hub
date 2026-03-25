package ui.components.Text

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun BasicTextFieldExpanded(
   value: TextFieldValue,
   onValueChange: (TextFieldValue) -> Unit,
   enabled: Boolean = true,
   readOnly: Boolean = false,
   textStyle: TextStyle = TextStyle.Default,
   keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
   keyboardActions: KeyboardActions = KeyboardActions.Default,
   singleLine: Boolean = false,
   maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
   minLines: Int = 1,
   modifier: Modifier = Modifier,
   onSelectionChanged: ((TextFieldValue) -> Unit)? = null,
   allowAutoScroll: Boolean = false,
   onAutoScroll: ((Int) -> Unit)? = null,
   visualTransformation: VisualTransformation = VisualTransformation.None,
   onTextLayout: (TextLayoutResult) -> Unit = {},
   interactionSource: MutableInteractionSource? = null,
   cursorBrush: Brush = SolidColor(Color.Black),
   decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
      @Composable { innerTextField -> innerTextField() }
) {
   val density = LocalDensity.current
   val view = LocalView.current
   val ime = WindowInsets.ime.asPaddingValues()
   
   val imeTop by remember {
      derivedStateOf {
         with(density) { ((view.bottom.toDp())-ime.calculateBottomPadding()).roundToPx()}
      }
   }
   
   var layoutCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
   
   BasicTextField(
      modifier = modifier then
         Modifier.onGloballyPositioned{
            layoutCoords = it
         },
      value = value,
      onValueChange = onValueChange,
      textStyle = textStyle,
      onTextLayout = {
         
         if (allowAutoScroll && layoutCoords != null) {
            
            // get cursor and pass
            val cursor = it.getCursorRect(value.selection.end)
            
            val cursorY = cursor.bottom
            
            val calculation = layoutCoords?.localToWindow(Offset(0f, cursorY))?.y ?: 0f
            if (calculation > imeTop) {
               val scrollY = calculation - imeTop
               
               onAutoScroll?.invoke(scrollY.roundToInt())
            }
         }
         // called pass
         onTextLayout.invoke(it)
      },
      keyboardActions = keyboardActions,
      keyboardOptions = keyboardOptions,
      cursorBrush = cursorBrush,
      visualTransformation = visualTransformation,
      decorationBox = decorationBox,
      interactionSource = interactionSource,
      readOnly = readOnly,
      minLines = minLines,
      maxLines = maxLines,
      enabled = enabled
   )
   
   LaunchedEffect(value.selection) {
      onSelectionChanged?.invoke(value)
   }
}

@Composable
fun BasicTextFieldExpanded(
   value: String,
   onValueChange: (String) -> Unit,
   enabled: Boolean = true,
   readOnly: Boolean = false,
   textStyle: TextStyle = TextStyle.Default,
   keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
   keyboardActions: KeyboardActions = KeyboardActions.Default,
   singleLine: Boolean = false,
   maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
   minLines: Int = 1,
   modifier: Modifier = Modifier,
   onSelectionChanged: ((String) -> Unit)? = null,
   allowAutoScroll: Boolean = false,
   onAutoScroll: ((Int) -> Unit)? = null,
   visualTransformation: VisualTransformation = VisualTransformation.None,
   onTextLayout: (TextLayoutResult) -> Unit = {},
   interactionSource: MutableInteractionSource? = null,
   cursorBrush: Brush = SolidColor(Color.Black),
   decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
      @Composable { innerTextField -> innerTextField() }
) {
   val density = LocalDensity.current
   val view = LocalView.current
   val ime = WindowInsets.ime.asPaddingValues()
   
   val imeTop by remember {
      derivedStateOf {
         with(density) { ((view.bottom.toDp())-ime.calculateBottomPadding()).roundToPx()}
      }
   }
   
   var layoutCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
   
   val textValue = remember(value) { TextFieldValue(value) }
   
   BasicTextField(
      modifier = modifier then
         Modifier.onGloballyPositioned{
            layoutCoords = it
         },
      value = value,
      onValueChange = onValueChange,
      textStyle = textStyle,
      onTextLayout = {
         
         if (allowAutoScroll && layoutCoords != null) {
            
            // get cursor and pass
            val cursor = it.getCursorRect(textValue.selection.end)
            
            val cursorY = cursor.bottom
            
            val calculation = layoutCoords?.localToWindow(Offset(0f, cursorY))?.y ?: 0f
            if (calculation > imeTop) {
               val scrollY = calculation - imeTop
               
               onAutoScroll?.invoke(scrollY.roundToInt())
            }
         }
         // called pass
         onTextLayout.invoke(it)
      },
      keyboardActions = keyboardActions,
      keyboardOptions = keyboardOptions,
      cursorBrush = cursorBrush,
      visualTransformation = visualTransformation,
      decorationBox = decorationBox,
      interactionSource = interactionSource,
      readOnly = readOnly,
      minLines = minLines,
      maxLines = maxLines,
      enabled = enabled
   )
   
   LaunchedEffect(textValue.selection) {
      onSelectionChanged?.invoke(value)
   }
}

@Composable
private fun BasicTextFieldExpanded_Sample() {
   
   val scope = rememberCoroutineScope()
   
   val lazyState = rememberLazyListState()
   
   LazyColumn(
      state = lazyState,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
         .fillMaxSize()
   ) {
      
      item(key = "58395803984"){
         var textValue by remember { mutableStateOf(TextFieldValue("""
            hello
            everyone
            i
            am
            here
         """.trimIndent())) }
         
         
         
         
         BasicTextFieldExpanded(
            value = textValue,
            onValueChange = {
               textValue = it
            },
            allowAutoScroll = true,
            onAutoScroll = { y->
               scope.launch {
                  lazyState.scrollBy(y.toFloat())
               }
            }
         )
      }
      
      
      
      
      items(10,{it}) {
         Box(Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(10.dp)
            .background(Color.Yellow))
      }
   }
}