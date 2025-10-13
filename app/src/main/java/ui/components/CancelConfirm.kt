package ui.components

import SrufComposeComponents.ElevatedButton
import SrufComposeComponents.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aacomposetest.ui.theme.Theme

@Preview
@Composable
fun CancelConfirm(
    modifier: Modifier = CancelConfirm.Defaults.modifier,
    buttonsModifier: Modifier = CancelConfirm.Defaults.buttonsModifier,
    onCancel: () -> Unit = CancelConfirm.Defaults.onCancel,
    onConfirm: () -> Unit = CancelConfirm.Defaults.onConfirm
) {
    Box(modifier) {
        Row {

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center){
                ElevatedButton(
                    modifier = buttonsModifier,
                    colors =ButtonDefaults.elevatedButtonColors(
                        containerColor = Theme.colorScheme().inversePrimary,
                        contentColor = Theme.colorScheme().inverseSurface
                    ),
                    onClick = {
                        onCancel.invoke()
                    }) {
                    Text(text = "Cancel")
                }
            }


            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                ElevatedButton(
                    modifier = buttonsModifier,
                    onClick = {
                        onConfirm.invoke()
                    }) {
                    Text(text = "Confirm")
                }
            }


        }
    }
}

object CancelConfirm{
    object Defaults{
        val modifier = Modifier.fillMaxWidth().wrapContentHeight()
        val buttonsModifier = Modifier
        val onCancel = {}
        val onConfirm = {}
    }
}