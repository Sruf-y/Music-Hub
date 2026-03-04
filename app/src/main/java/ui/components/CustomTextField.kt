package ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.forEach
import androidx.core.view.updatePadding
import com.aacomposetest.ui.theme.MyColor


public class CustomEditText(context: Context): AppCompatEditText(context){
   
   var currentActionMode: ActionMode? = null
   
   /**
    * DO NOT USE
    * */
   override fun isCursorVisible():Boolean{
      return super.isCursorVisible
   }
   
   /**
    * Computationally cheap: Can be used without worry and only does basic comparations (aside from the allocations required)
    * */
   @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
   fun isCursorVisibleInScroll(scrollY: Int, height: Int): Boolean {
      val layout = layout ?: return true
      val line = layout.getLineForOffset(selectionStart)
      
      val lineRect = Rect()
      getLineBounds(line,lineRect)
      
      // scrollY = scroll container vertical scroll
      // cursorBottom > scrollY && cursorTop < scrollY + height
      return lineRect.bottom > scrollY && lineRect.top < scrollY + height
   }
   
}

val emptyTextToolbar:(CustomEditText,ScrollView)-> ActionMode.Callback ={editText,parent->
   
    with(editText) {
      object : ActionMode.Callback {
         override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.clear()
            return true
         }
         
         override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.clear()
            return true
         }
         
         override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            // NO-OP
            return false
         }
         
         override fun onDestroyActionMode(mode: ActionMode?) {
            // Cleanup
         }
      }
   }
   
}


val textToolbar:(CustomEditText,ScrollView)-> ActionMode.Callback ={editText,parent->
    with(editText) {
      object : ActionMode.Callback {
         override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            currentActionMode = mode
            // System automatically adds default items (Copy, Cut, Paste, Select All)
            // We can remove speciffic items or even clear the menu before adding my own items.
            menu?.removeItem(android.R.id.autofill)
//                        menu?.removeItem(android.R.id.copy)
//                        menu?.removeItem(android.R.id.cut)
//                        menu?.removeItem(android.R.id.paste)
//                        menu?.removeItem(android.R.id.selectAll)
            
            // We can add our custom items to the SAME toolbar
//                  menu?.add(Menu.NONE, 1001, 1, "Bold")
//                  menu?.add(Menu.NONE, 1002, 2, "Italic")
//                  menu?.add(Menu.NONE, 1003, 3, "Share")
//                  menu?.add(Menu.NONE, 1004, 4, "Translate")
            
            return true
         }
         
         /**
          * Called whenever the FAB is invalidated and needs to be re-rendered.
          *
          * Ex: Scrolling the text down.
          * */
         @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
         override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val cursorVisible = isCursorVisibleInScroll(parent.scrollY,parent.height)
            
            
            menu?.forEach {
               it.isVisible = cursorVisible
            }

//                     menu?.findItem(1001)?.isEnabled = cursorVisible // Bold
//                     menu?.findItem(1002)?.isEnabled = cursorVisible // Italic
//                     menu?.findItem(1003)?.isEnabled = cursorVisible // Share
//                     menu?.findItem(1004)?.isEnabled = cursorVisible // Translate
            
            return true
         }
         
         override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
               1001 -> {
                  // Handle Bold action
                  Toast.makeText(context, "Bold clicked", Toast.LENGTH_SHORT).show()
                  mode?.finish()
                  return true
               }
               
               1002 -> {
                  // Handle Italic action
                  Toast.makeText(context, "Italic clicked", Toast.LENGTH_SHORT).show()
                  mode?.finish()
                  return true
               }
               
               1003 -> {
                  // Handle Share action
                  val selectedText =
                     text?.substring(selectionStart, selectionEnd)
                  val intent = Intent(Intent.ACTION_SEND).apply {
                     type = "text/plain"
                     putExtra(Intent.EXTRA_TEXT, selectedText)
                  }
                  context.startActivity(Intent.createChooser(intent, "Share text"))
                  mode?.finish()
                  return true
               }
               
               1004 -> {
                  // Handle Translate action
                  Toast.makeText(context, "Translate clicked", Toast.LENGTH_SHORT)
                     .show()
                  mode?.finish()
                  return true
               }
            }
            return false
         }
         
         override fun onDestroyActionMode(mode: ActionMode?) {
            currentActionMode = null
            // Cleanup
         }
      }
   }
}


/**
 * Textfield that tries to scroll to avoid having the cursor under the IME.
 * @param lineSpacingMultiplier the size of a line relative to the size of a line. Basically above 1f it adds space, under it removes (not reccomanded)
 * @param onEditTextSetUp No real need for this but it allows the styling of the editText ONLY on initial creation and not on any recompositions.
 * */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomTextField(
   text: MutableState<String?>,
   hint: String = "",
   hintColor: Color = Color.DarkGray,
   onValueChange: (Editable?) -> Unit,
   textSize: Float = 16.sp.value,
   color: Color = Color.Black,
   modifier: Modifier = Modifier,
   textGravity: Int = Gravity.START or Gravity.CENTER,
   onEditTextSetUp: (EditText) -> Unit = {},
   lineSpacingMultiplier: Float = 1f,
   contentPadding: PaddingValues = PaddingValues(0.dp),
   selectionActionModeCallback:(CustomEditText,ScrollView)->ActionMode.Callback = textToolbar,
   insertionActionModeCallback:(CustomEditText,ScrollView)->ActionMode.Callback = emptyTextToolbar,
   enabled: Boolean = true,
) {
   val view = LocalView.current
   val context = LocalContext.current
   val density = LocalDensity.current
   
   val imeTop = with(density) { ((view.bottom.toDp())-WindowInsets.ime.asPaddingValues().calculateBottomPadding()).roundToPx()}
   
   
   
   val scrollViewID = remember { View.generateViewId() }
   val editTextID = remember { View.generateViewId() }
   
   val scrollView = remember {
      ScrollView(context).apply {
         id = scrollViewID
         //setBackgroundColor(Color.Green.toArgb())
         layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
         )
         
         
         isVerticalScrollBarEnabled = true
         
         isNestedScrollingEnabled = true
         overScrollMode = View.OVER_SCROLL_ALWAYS
      }
   }
   
   val editText = remember {
      CustomEditText(context).apply {
         
         id = editTextID
         
         // Unimportant to autofill
         importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
         
         // Set background to TRANSPARENT
         setBackgroundColor(Color.Transparent.toArgb())
         
         layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
         )
         
         //minWidth = with(density){200.dp.roundToPx()}
         //minHeight = with(density){48.dp.roundToPx()}
         
         gravity = textGravity
         
         setText(text.value)
         setTextColor(color.toArgb())
         this.textSize = textSize
         
         // Set padding to 0
         setPadding(0, 0, 0, 0)
         
         // Customize the system selection toolbar.
         customSelectionActionModeCallback = selectionActionModeCallback(this,scrollView)
         // No action bar on double tapping for insertion.
         customInsertionActionModeCallback = insertionActionModeCallback(this,scrollView)
         
         this.isEnabled = enabled
      }
   }
   
   val keyboardController = LocalSoftwareKeyboardController.current
   
   DisposableEffect(Unit) {
      onDispose {
         keyboardController?.hide()
      }
   }
   
   val layoutDirection = LocalLayoutDirection.current
   
   // minHeight == its bounds height to be able to start writting from anywhere
   var minTextViewHeight by remember { mutableIntStateOf(0) }
   
   Layout(modifier = modifier
      .onSizeChanged{
         minTextViewHeight = it.height
      }.imePadding(),
      content = {
      AndroidView(
         modifier = modifier
            .clipToBounds()
            // Safe Autoscroll logic
            .onSizeChanged {
//            scrollView.updateLayoutParams {
//               this.height = it.height
//            }
               val selectionBottom = editText.getSelectionScreenRect()?.bottom ?: 0
               
               val selectionEnd = selectionBottom % (view.bottom.takeIf { it != 0 } ?: 1)
               
               if (selectionEnd > imeTop) {
                  scrollView.scrollBy(0, selectionEnd - imeTop)
               }
               
               
            },
         factory = { context ->
            
            // Minimum height for better experience
            editText.apply {
               minHeight = minTextViewHeight
            }
            
            // Setup lineSpacing
            editText.setLineSpacing(0f, lineSpacingMultiplier)
            
            // Text change listener
            editText.addTextChangedListener(object : TextWatcher {
               override fun afterTextChanged(s: Editable?) {
                  onValueChange.invoke(s)
               }
               
               override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
               
               }
               
               override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
               
               }
            })
            
            // Scroll listener. This verifies if the cursor is still in view of the user.
            var pastCursorVisibility = false
            
            scrollView.setOnScrollChangeListener(
               object : View.OnScrollChangeListener {
                  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                  override fun onScrollChange(
                     v: View,
                     scrollX: Int,
                     scrollY: Int,
                     oldScrollX: Int,
                     oldScrollY: Int,
                  ) {
                     
                     editText.isCursorVisibleInScroll(scrollY, v.height).apply {
                        
                        if (this != pastCursorVisibility) {
                           editText.currentActionMode?.invalidate()
                           pastCursorVisibility = this
                        }
                     }
                     
                  }
               }
            )
            
            scrollView.addView(editText)
            //editText.fillMaxSize()
            
            // Callback to allow outside modifications
            onEditTextSetUp.invoke(editText)
            
            editText.hint = hint
            editText.setHintTextColor(hintColor.toArgb())
            
            scrollView
            //editText
         },
         update = { nestedScrollView ->
            //val editText = nestedScrollView.getChildAt(0) as EditText
            
            editText.apply {
               
               // Apply minHeight in case of change
               minHeight = minTextViewHeight
               
               // Hint
               editText.hint = hint
               editText.setHintTextColor(hintColor.toArgb())
               
               // Text listening
               if (editText.text.toString() != text.value) {
                  
                  setText(text.value)
               }
               // Text size
               this.textSize = textSize
               
               // Text color
               setTextColor(color.toArgb())
               
               gravity = textGravity
               
               this.isEnabled = enabled
               
               setLineSpacing(0f, lineSpacingMultiplier)
               
               
               updatePadding(
                  left = with(density) {
                     contentPadding.calculateLeftPadding(layoutDirection).roundToPx()
                  },
                  bottom = with(density) { contentPadding.calculateBottomPadding().roundToPx() },
                  right = with(density) {
                     contentPadding.calculateRightPadding(layoutDirection).roundToPx()
                  },
                  top = with(density) { contentPadding.calculateTopPadding().roundToPx() }
               )
               
               
            }
            
         }
      )
   }) {measurables,constraints->
      val placeable = measurables[0].measure(constraints)
      layout(constraints.maxWidth,placeable.height) {
         placeable.placeRelative(0,0)
      }
   }
   
   
   // React to changes made via compose states.
   LaunchedEffect(selectionActionModeCallback) {
      editText.customSelectionActionModeCallback = selectionActionModeCallback(editText,scrollView)
   }
   
   LaunchedEffect(insertionActionModeCallback) {
      editText.customInsertionActionModeCallback = insertionActionModeCallback(editText,scrollView)
   }
   
   
}
   
   


fun EditText.getSelectionScreenRect(): Rect? {
   val layout = layout ?: return null
   if (selectionStart < 0) return null
   
   val startLine = layout.getLineForOffset(selectionStart)
   val endLine = layout.getLineForOffset(selectionEnd)
   
   val left = layout.getPrimaryHorizontal(selectionStart).toInt()
   val top = layout.getLineTop(startLine)
   val right = layout.getPrimaryHorizontal(selectionEnd).toInt()
   val bottom = layout.getLineBottom(endLine)
   
   val location = IntArray(2)
   
   getLocationOnScreen(location)
   
   return Rect(
      location[0] + left,
      location[1] + top,
      location[0] + right,
      location[1] + bottom
   )
}


