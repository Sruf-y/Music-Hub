package MyViewModels.BottomSheet

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.musichub.ui.theme.MyColor

/**
 * Composable holding the stack of sheets.
 * */
@Composable
fun MyModalBottomSheet(
   state: SheetViewModel,
   modifier: Modifier = Modifier,
   backgroundTint: Color = MyColor.SemiTransparent(),
   specFor: (BottomSheetData) -> SheetSpec = { SheetSpec() },
) {
   BackHandler(enabled = state.stack.isNotEmpty()) {
      state.requestDismissCurrent()          // no scope.launch — mutations are synchronous
   }
   
   Box(modifier = modifier) {
      
      
      // Render the sheets
      state.renderAll(
         specFor = specFor,
         scrim = { progress ->
            
            // Render the overlay
            BlackBackground(
               alpha = progress,
               color = backgroundTint,
               onClick = { state.requestDismissCurrent() }
            )
            
         }
      )
   }
}
   