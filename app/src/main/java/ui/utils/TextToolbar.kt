package ui.utils

import android.os.Build
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.unit.dp
import android.graphics.Rect as ViewsRect


object TextToolbarDefaults{
   // This is the default implementation taken straight out of the AOSP documentation. All rights for this implementation go to AOSP
   val defaultImplementation = object: TextActionModeCallback()
   {
      override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
         
         if(menu!=null){
            onCopyRequested?.let {
               addMenuItem(menu, 1,android.R.string.copy)
            }
            onPasteRequested?.let {
               addMenuItem(menu, 2,android.R.string.paste)
            }
            onCutRequested?.let {
               addMenuItem(menu, 3,android.R.string.cut)
            }
            onSelectAllRequested?.let {
               addMenuItem(menu, 4,android.R.string.selectAll)
            }
         }
         
         return true
      }
      
      // this method is called to populate new menu items when the actionMode was invalidated
      override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
         if (mode == null || menu == null) return false
         updateMenuItems(menu)
         // should return true so that new menu items are populated
         return true
      }
      
      override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
         when (item!!.itemId) {
            1 -> onCopyRequested?.invoke()
            2 -> onPasteRequested?.invoke()
            3 -> onCutRequested?.invoke()
            4 -> onSelectAllRequested?.invoke()
            else -> return false
         }
         mode?.finish()
         return true
      }
      
      override fun onDestroyActionMode() {
         onActionModeDestroy?.invoke()
      }
      
      
      internal fun updateMenuItems(menu: Menu) {
         addOrRemoveMenuItem(menu, 1, android.R.string.copy,onCopyRequested)
         addOrRemoveMenuItem(menu, 2, android.R.string.paste,onPasteRequested)
         addOrRemoveMenuItem(menu, 3, android.R.string.cut,onCutRequested)
         addOrRemoveMenuItem(menu, 4, android.R.string.selectAll,onSelectAllRequested)
      }
      
      internal fun addMenuItem(menu: Menu, id:Int,titleRes:Int) {
         menu.add(0, id, id, titleRes)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
      }
      
      private fun addOrRemoveMenuItem(
         menu: Menu,
         id:Int,
         titleRes:Int,
         callback: (() -> Unit)?
      ) {
         when {
            callback != null && menu.findItem(id) == null -> addMenuItem(menu, id,titleRes)
            callback == null && menu.findItem(id) != null -> menu.removeItem(id)
         }
      }
   }
   
}




/**
 *
 * */
abstract class TextActionModeCallback(
   var onActionModeDestroy: (() -> Unit)? = null,
   var rect: Rect = Rect.Zero,
   var onCopyRequested: (() -> Unit)? = null,
   var onPasteRequested: (() -> Unit)? = null,
   var onCutRequested: (() -> Unit)? = null,
   var onSelectAllRequested: (() -> Unit)? = null
)
{
   /**
    * @return true when action was handled, false otherwise
    * */
   abstract fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean
   
   /**
    * @return true when action was handled, false otherwise
    * */
   abstract fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean
   
   /**
    * @return true when action was handled, false otherwise
    * */
   abstract fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean
   
   /**
    * should call [onActionModeDestroy]
    * */
   abstract fun onDestroyActionMode()
}


// This is the default implementation taken straight out of the AOSP documentation. All rights for this implementation go to AOSP
internal class PrimaryTextActionModeCallback(
   private val callback: TextActionModeCallback
)
   : ActionMode.Callback {
   override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
      return callback.onActionItemClicked(mode, item)
   }
   
   override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
      return callback.onCreateActionMode(mode, menu)
   }
   
   override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
      return callback.onPrepareActionMode(mode, menu)
   }
   
   override fun onDestroyActionMode(mode: ActionMode?) {
      callback.onDestroyActionMode()
   }
}

// This is the default implementation taken straight out of the AOSP documentation. All rights for this implementation go to AOSP
internal class FloatingTextActionModeCallback(
   private val callback: TextActionModeCallback
)
   : ActionMode.Callback2() {
   
   override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
      return callback.onActionItemClicked(mode, item)
   }
   
   override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
      return callback.onCreateActionMode(mode, menu)
   }
   
   override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
      return callback.onPrepareActionMode(mode, menu)
   }
   
   override fun onDestroyActionMode(mode: ActionMode?) {
      callback.onDestroyActionMode()
   }
   
   // This is where the floating Rect is positioned on the screen at the cursor coordonates.
   override fun onGetContentRect(
      mode: ActionMode,
      view: View,
      outRect: ViewsRect,
   ) {
      var rect = callback.rect
      outRect.set(
         rect.left.toInt(),
         rect.top.toInt(),
         rect.right.toInt(),
         rect.bottom.toInt()
      )
   }
}




// This is ALMOST the default implementation taken straight out of the AOSP documentation. All rights for this implementation go to AOSP
final class AndroidTextToolbar(
   private val view: View,
   private val callbacks:TextActionModeCallback
)
   : TextToolbar {
   
   private var actionMode: ActionMode? = null
   
   init {
      callbacks.onActionModeDestroy = {
         actionMode = null
      }
   }
   
   override var status: TextToolbarStatus = TextToolbarStatus.Hidden
      private set
   
   override fun showMenu(
      rect: Rect,
      onCopyRequested: (() -> Unit)?,
      onPasteRequested: (() -> Unit)?,
      onCutRequested: (() -> Unit)?,
      onSelectAllRequested: (() -> Unit)?
   ) {
      // pass refferences
      callbacks.rect = rect
      
      callbacks.onCopyRequested = onCopyRequested
      callbacks.onCutRequested = onCutRequested
      callbacks.onPasteRequested = onPasteRequested
      callbacks.onSelectAllRequested = onSelectAllRequested
      
      if (actionMode == null) {
         status = TextToolbarStatus.Shown
         
         // Start action mode (floating toolbar)
         actionMode = if (Build.VERSION.SDK_INT >= 23) {
            view.startActionMode(
               FloatingTextActionModeCallback(callbacks),
               ActionMode.TYPE_FLOATING
            )
         } else {
            view.startActionMode(
               PrimaryTextActionModeCallback(callbacks)
            )
         }
         
      } else {
         actionMode?.invalidate()
      }
   }
   
   override fun hide() {
      status = TextToolbarStatus.Hidden
      actionMode?.finish()
      actionMode = null
   }
}














@Composable
fun Sample_CustomTextActions(){
   val view = LocalView.current
   val textToolbar = remember(view) {
      AndroidTextToolbar(
         view = view,
         callbacks = TextToolbarDefaults.defaultImplementation
      )
   }
   
   var text by remember { mutableStateOf("") }
   CompositionLocalProvider(
      LocalTextToolbar provides textToolbar
   ) {
      
      OutlinedTextField(
         value = text,
         onValueChange = { text = it.toString() },
         modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
      )
      
   }
   
}

@Composable
fun Sample_CustomTextActions2(){
   // create custom callbacks
   val textCallbacks = remember {
      object: TextActionModeCallback() {
         override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            
            if(menu!=null){
               onCopyRequested?.let {
                  addMenuItem(menu, 1,android.R.string.copy)
               }
               onPasteRequested?.let {
                  addMenuItem(menu, 2,android.R.string.paste)
               }
               onCutRequested?.let {
                  addMenuItem(menu, 3,android.R.string.cut)
               }
               onSelectAllRequested?.let {
                  addMenuItem(menu, 4,android.R.string.selectAll)
               }
               
               menu.add(0,5,5,"Like")
            }
            
            return true
         }
         
         // this method is called to populate new menu items when the actionMode was invalidated
         override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            if (mode == null || menu == null) return false
            menu.clear()
            updateMenuItems(menu)
            // should return true so that new menu items are populated
            return true
         }
         
         override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item!!.itemId) {
               1 -> onCopyRequested?.invoke()
               2 -> onPasteRequested?.invoke()
               3 -> onCutRequested?.invoke()
               4 -> onSelectAllRequested?.invoke()
               5->{
                  Log.i("TextToolbar","Clicked Like action on the action bar")
               }
               else -> return false
            }
            // finish the Action Mode (close the floating action bar)
            mode?.finish()
            return true
         }
         
         override fun onDestroyActionMode() {
            onActionModeDestroy?.invoke()
         }
         
         // Insertion is recognized as only having Select All as the only provided callback from the system (aside from any custom callbacks like "Like"
         fun updateMenuItems(menu: Menu) {
            onCopyRequested?.let {
               addMenuItem(menu, 1,android.R.string.copy)
            }
            onPasteRequested?.let {
               addMenuItem(menu, 2,android.R.string.paste)
            }
            onCutRequested?.let {
               addMenuItem(menu, 3,android.R.string.cut)
            }
            onSelectAllRequested?.let {
               addMenuItem(menu, 4,android.R.string.selectAll)
            }
            
            menu.add(0,5,5,"Like")
         }
         
         fun addMenuItem(menu: Menu, id:Int,titleRes:Int) {
            menu.add(0, id, id, titleRes)
               .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
         }
         
      }
   }
   
   val view = LocalView.current
   val textToolbar = remember(view,textCallbacks) {
      AndroidTextToolbar(
         view = view,
         callbacks = textCallbacks
      )
   }
   
   var text by remember { mutableStateOf("") }
   
   CompositionLocalProvider(
      LocalTextToolbar provides textToolbar
   ) {
      
      OutlinedTextField(
         value = text,
         onValueChange = { text = it.toString() },
         modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
      )
      
   }
}