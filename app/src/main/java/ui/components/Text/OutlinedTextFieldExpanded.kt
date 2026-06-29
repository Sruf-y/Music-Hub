package ui.components.Text

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun OutlinedTextFieldExpanded(
   value: String,
   onValueChange: (String) -> Unit,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   readOnly: Boolean = false,
   textStyle: TextStyle = LocalTextStyle.current,
   label: @Composable (() -> Unit)? = null,
   placeholder: @Composable (() -> Unit)? = null,
   leadingIcon: @Composable (() -> Unit)? = null,
   trailingIcon: @Composable (() -> Unit)? = null,
   prefix: @Composable (() -> Unit)? = null,
   suffix: @Composable (() -> Unit)? = null,
   supportingText: @Composable (() -> Unit)? = null,
   isError: Boolean = false,
   visualTransformation: VisualTransformation = VisualTransformation.None,
   keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
   keyboardActions: KeyboardActions = KeyboardActions.Default,
   singleLine: Boolean = false,
   maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
   minLines: Int = 1,
   interactionSource: MutableInteractionSource? = null,
   shape: Shape = OutlinedTextFieldDefaults.shape,
   colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
   @Suppress("NAME_SHADOWING")
   val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
   // If color is not provided via the text style, use content color as a default
   val textColor =
      textStyle.color.takeOrElse {
         val focused = interactionSource.collectIsFocusedAsState().value
         colors.textColor(enabled, isError, focused)
      }
   val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))
   
   CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
      BasicTextField(
         value = value,
         modifier =
            modifier
               .then(
                  if (label != null) {
                     Modifier
                        // Merge semantics at the beginning of the modifier chain to ensure
                        // padding is considered part of the text field.
                        .semantics(mergeDescendants = true) {}
                  } else {
                     Modifier
                  }
               )
               .defaultMinSize(
                  minWidth = OutlinedTextFieldDefaults.MinWidth,
                  minHeight = OutlinedTextFieldDefaults.MinHeight,
               ),
         onValueChange = onValueChange,
         enabled = enabled,
         readOnly = readOnly,
         textStyle = mergedTextStyle,
         cursorBrush = SolidColor(colors.cursorColor(isError)),
         visualTransformation = visualTransformation,
         keyboardOptions = keyboardOptions,
         keyboardActions = keyboardActions,
         interactionSource = interactionSource,
         singleLine = singleLine,
         maxLines = maxLines,
         minLines = minLines,
         decorationBox =
            @Composable { innerTextField ->
               OutlinedTextFieldDefaults.DecorationBox(
                  value = value,
                  visualTransformation = visualTransformation,
                  innerTextField = innerTextField,
                  placeholder = placeholder,
                  label = label,
                  leadingIcon = leadingIcon,
                  trailingIcon = trailingIcon,
                  prefix = prefix,
                  suffix = suffix,
                  supportingText = supportingText,
                  singleLine = singleLine,
                  enabled = enabled,
                  isError = isError,
                  interactionSource = interactionSource,
                  colors = colors,
                  container = {
                     OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = isError,
                        interactionSource = interactionSource,
                        colors = colors,
                        shape = shape,
                     )
                  },
               )
            },
      )
   }
}

@Composable
fun OutlinedTextFieldExpanded(
   state: TextFieldState,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   readOnly: Boolean = false,
   textStyle: TextStyle = LocalTextStyle.current,
   label: @Composable (() -> Unit)? = null,
   placeholder: @Composable (() -> Unit)? = null,
   leadingIcon: @Composable (() -> Unit)? = null,
   trailingIcon: @Composable (() -> Unit)? = null,
   prefix: @Composable (() -> Unit)? = null,
   suffix: @Composable (() -> Unit)? = null,
   supportingText: @Composable (() -> Unit)? = null,
   isError: Boolean = false,
   visualTransformation: VisualTransformation = VisualTransformation.None,
   keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
   onKeyboardAction: KeyboardActionHandler? = null,
   lines: TextFieldLineLimits = TextFieldLineLimits.Default,
   interactionSource: MutableInteractionSource? = null,
   shape: Shape = OutlinedTextFieldDefaults.shape,
   colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
   @Suppress("NAME_SHADOWING")
   val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
   // If color is not provided via the text style, use content color as a default
   val textColor =
      textStyle.color.takeOrElse {
         val focused = interactionSource.collectIsFocusedAsState().value
         colors.textColor(enabled, isError, focused)
      }
   val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))
   
   CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
      BasicTextField(
         state = state,
         modifier =
            modifier
               .then(
                  if (label != null) {
                     Modifier
                        // Merge semantics at the beginning of the modifier chain to ensure
                        // padding is considered part of the text field.
                        .semantics(mergeDescendants = true) {}
                  } else {
                     Modifier
                  }
               )
               .defaultMinSize(
                  minWidth = OutlinedTextFieldDefaults.MinWidth,
                  minHeight = OutlinedTextFieldDefaults.MinHeight,
               ),
         enabled = enabled,
         readOnly = readOnly,
         textStyle = mergedTextStyle,
         cursorBrush = SolidColor(colors.cursorColor(isError)),
         keyboardOptions = keyboardOptions,
         lineLimits = lines,
         onKeyboardAction = onKeyboardAction,
         interactionSource = interactionSource,
         decorator = TextFieldDecorator{innerTextField ->
                  OutlinedTextFieldDefaults.DecorationBox(
                     value = state.text.toString(),
                     visualTransformation = visualTransformation,
                     innerTextField = innerTextField,
                     placeholder = placeholder,
                     label = label,
                     leadingIcon = leadingIcon,
                     trailingIcon = trailingIcon,
                     prefix = prefix,
                     suffix = suffix,
                     supportingText = supportingText,
                     singleLine = lines == TextFieldLineLimits.SingleLine,
                     enabled = enabled,
                     isError = isError,
                     interactionSource = interactionSource,
                     colors = colors,
                     container = {
                        OutlinedTextFieldDefaults.Container(
                           enabled = enabled,
                           isError = isError,
                           interactionSource = interactionSource,
                           colors = colors,
                           shape = shape,
                        )
                     },
                  )
               
         }
      )
   }
}



/**
 * Represents the color used for the leading icon of this text field.
 *
 * @param enabled whether the text field is enabled
 * @param isError whether the text field's current value is in error
 * @param focused whether the text field is in focus
 */
@Stable
internal fun TextFieldColors.leadingIconColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
   when {
      !enabled -> disabledLeadingIconColor
      isError -> errorLeadingIconColor
      focused -> focusedLeadingIconColor
      else -> unfocusedLeadingIconColor
   }

/**
 * Represents the color used for the trailing icon of this text field.
 *
 * @param enabled whether the text field is enabled
 * @param isError whether the text field's current value is in error
 * @param focused whether the text field is in focus
 */
@Stable
internal fun TextFieldColors.trailingIconColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
   when {
      !enabled -> disabledTrailingIconColor
      isError -> errorTrailingIconColor
      focused -> focusedTrailingIconColor
      else -> unfocusedTrailingIconColor
   }

/**
 * Represents the color used for the border indicator of this text field.
 *
 * @param enabled whether the text field is enabled
 * @param isError whether the text field's current value is in error
 * @param focused whether the text field is in focus
 */
@Stable
internal fun TextFieldColors.indicatorColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
   when {
      !enabled -> disabledIndicatorColor
      isError -> errorIndicatorColor
      focused -> focusedIndicatorColor
      else -> unfocusedIndicatorColor
   }

/**
 * Represents the container color for this text field.
 *
 * @param enabled whether the text field is enabled
 * @param isError whether the text field's current value is in error
 * @param focused whether the text field is in focus
 */
@Stable
internal fun TextFieldColors.containerColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
   when {
      !enabled -> disabledContainerColor
      isError -> errorContainerColor
      focused -> focusedContainerColor
      else -> unfocusedContainerColor
   }

/**
 * Represents the color used for the placeholder of this text field.
 *
 * @param enabled whether the text field is enabled
 * @param isError whether the text field's current value is in error
 * @param focused whether the text field is in focus
 */
@Stable
internal fun TextFieldColors.placeholderColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
   when {
      !enabled -> disabledPlaceholderColor
      isError -> errorPlaceholderColor
      focused -> focusedPlaceholderColor
      else -> unfocusedPlaceholderColor
   }

/**
 * Represents the color used for the label of this text field.
 *
 * @param enabled whether the text field is enabled
 * @param isError whether the text field's current value is in error
 * @param focused whether the text field is in focus
 */
@Stable
internal fun TextFieldColors.labelColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
   when {
      !enabled -> disabledLabelColor
      isError -> errorLabelColor
      focused -> focusedLabelColor
      else -> unfocusedLabelColor
   }

/**
 * Represents the color used for the input field of this text field.
 *
 * @param enabled whether the text field is enabled
 * @param isError whether the text field's current value is in error
 * @param focused whether the text field is in focus
 */
@Stable
internal fun TextFieldColors.textColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
   when {
      !enabled -> disabledTextColor
      isError -> errorTextColor
      focused -> focusedTextColor
      else -> unfocusedTextColor
   }

/**
 * Represents the colors used for the supporting text of this text field.
 *
 * @param enabled whether the text field is enabled
 * @param isError whether the text field's current value is in error
 * @param focused whether the text field is in focus
 */
@Stable
internal fun TextFieldColors.supportingTextColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
   when {
      !enabled -> disabledSupportingTextColor
      isError -> errorSupportingTextColor
      focused -> focusedSupportingTextColor
      else -> unfocusedSupportingTextColor
   }

/**
 * Represents the color used for the prefix of this text field.
 *
 * @param enabled whether the text field is enabled
 * @param isError whether the text field's current value is in error
 * @param focused whether the text field is in focus
 */
@Stable
internal fun TextFieldColors.prefixColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
   when {
      !enabled -> disabledPrefixColor
      isError -> errorPrefixColor
      focused -> focusedPrefixColor
      else -> unfocusedPrefixColor
   }

/**
 * Represents the color used for the suffix of this text field.
 *
 * @param enabled whether the text field is enabled
 * @param isError whether the text field's current value is in error
 * @param focused whether the text field is in focus
 */
@Stable
internal fun TextFieldColors.suffixColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
   when {
      !enabled -> disabledSuffixColor
      isError -> errorSuffixColor
      focused -> focusedSuffixColor
      else -> unfocusedSuffixColor
   }

/**
 * Represents the color used for the cursor of this text field.
 *
 * @param isError whether the text field's current value is in error
 */
@Stable
internal fun TextFieldColors.cursorColor(isError: Boolean): Color =
   if (isError) errorCursorColor else cursorColor