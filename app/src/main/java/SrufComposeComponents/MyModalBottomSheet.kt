package SrufComposeComponents


import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.aacomposetest.Logs
import com.aacomposetest.ui.theme.MyColor
import com.composables.core.BottomSheet
import com.composables.core.BottomSheetState
import com.composables.core.SheetDetent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object MyModalBottomSheet {

    class ModalVM(): ViewModel() {
        init {
            Log.i(Logs.RUNNING_INFO.name,"ModalVM has been recreated")
        }

        private val _isSheetOpen = MutableStateFlow(false)
        val isSheetOpen: StateFlow<Boolean> = _isSheetOpen


        var content: MutableState<@Composable (ColumnScope.(isFocusHeld:MutableState<Boolean>) -> Unit)?> = mutableStateOf({})

        var onDismiss:(()-> Unit)? = null
        fun dismissSheet(onDismiss:(()-> Unit)? = null) {
            this.onDismiss = onDismiss?:{content.value = null}

            _isSheetOpen.value = false
        }

        fun show(withContent:@Composable (ColumnScope.(isFocusHeld:MutableState<Boolean>)-> Unit)? = null){
            if(withContent!=null){
                content.value = withContent
            }
            _isSheetOpen.value = true
        }
    }

    object Defaults{
        val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        



    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyModalBottomSheet(
    viewModel: MyModalBottomSheet.ModalVM,
    sheetState: BottomSheetState,
    backgroundTintColor: Color = MyColor.SemiTransparent(),
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    shape: Shape = MyModalBottomSheet.Defaults.shape,
    handle: @Composable (ColumnScope.() -> Unit)? = null,
    isDragEnabled: Boolean = true,
    bottomSheetContent: @Composable (ColumnScope.(isFocusHeld: MutableState<Boolean>) -> Unit)? = {}
) {

    val contextRef = LocalContext.current

    val scaffoldInteractionSource = remember { MutableInteractionSource() }



    val focusManager = LocalFocusManager.current
    val isFocusHeld = remember { mutableStateOf(false) }


    val isSheetOpen by viewModel.isSheetOpen.collectAsState()

    val mutex = remember { Mutex() }



    LaunchedEffect(isSheetOpen) {
        mutex.withLock {
            try {
                if (isSheetOpen) {
                    sheetState.animateTo(SheetDetent.FullyExpanded)
                } else {
                    sheetState.animateTo(SheetDetent.Hidden)
                    viewModel.onDismiss?.invoke()
                }
            }finally {

            }

        }
    }

    fun onDismiss(){

        if(isFocusHeld.value){
            isFocusHeld.value = false
            focusManager.clearFocus()
        }else{
            viewModel.dismissSheet()


        }

    }



    val alpha by animateFloatAsState(
        targetValue = when {
            sheetState.targetDetent == SheetDetent.FullyExpanded -> sheetState.progress   // Peek fades in
            else -> 1f - sheetState.progress                           // Expanded fades in
        },
        animationSpec = tween(durationMillis = 0)
    )


    val background = @Composable {
        Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(color = backgroundTintColor)
            .clickable(
                interactionSource = scaffoldInteractionSource,
                indication = null,
                onClick = {
                    if (sheetState.currentDetent == SheetDetent.FullyExpanded)
                        onDismiss()
                })
    )}




    BackHandler(isSheetOpen) {
        onDismiss()
    }



        // Apply a dim background while the bottomsheet is active
        if (isSheetOpen) background.invoke()

        Box(modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .statusBarsPadding()
            .displayCutoutPadding()
            .navigationBarsPadding()
            .imePadding()
        ){
            BottomSheet(
                modifier = Modifier
                    .padding(PaddingValues())
                    .alpha(
                        if (alpha == 0f) {
                            0f
                        } else 1f
                    ),
                state = sheetState,
                content = {
                    Column {
                        handle?.invoke(this)
                        bottomSheetContent?.invoke(this,isFocusHeld)
                    }
                },
                shape = shape,
                enabled = isDragEnabled,
                backgroundColor = containerColor,
                contentColor = contentColor
            )
        }








}


