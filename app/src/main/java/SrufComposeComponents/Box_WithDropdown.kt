package SrufComposeComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.runtime.State

@Composable
fun Box_WithDropdown(
    modifier: Modifier = Modifier,
    state: MutableState<Boolean> = mutableStateOf(false),
    actions: List<Pair<String,()-> Unit>>,
    content: @Composable () -> Unit,
) {

    Box(modifier){


        // The actual content
        content.invoke()

        // The dropdown menu render
        DropdownMenu(expanded = state.value,
            onDismissRequest = {state.value = false}
        ) {

            actions.forEach {
                DropdownMenuItem(
                    text = {
                        Text(it.first)
                    }, onClick = {
                        it.second.invoke()
                    }
                )
            }
        }


    }
}