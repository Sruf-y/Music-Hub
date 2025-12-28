package ui.components.BottomSheets.Content

import AppDatabase.PlaylistRelated.Playlist
import SrufComposeComponents.MyModalBottomSheet
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import ui.components.CancelConfirm

@Composable
fun PlaylistDelete_Content(playlist: Playlist, bottomSheetVM: MyModalBottomSheet.ModalVM?){

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val myTitle = playlist.title

    val annotatedString = buildAnnotatedString {
        append("Are you sure you want to ")

        withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
            append("DELETE")
        }

    }

    val annotatedString2 = buildAnnotatedString {
        withStyle(style = SpanStyle(color = MyColor.Orange)) {
            append(myTitle)
        }

        append(" ?")
    }

    val onDismiss = {
        bottomSheetVM?.dismissSheet()
    }

    Column {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp), contentAlignment = Alignment.Center){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = annotatedString, color = Theme.colorScheme().inverseSurface)
                Text(text = annotatedString2, color = Theme.colorScheme().inverseSurface)

            }


        }
        CancelConfirm(onCancel = {
            onDismiss.invoke()
        }, onConfirm = {

            playlist.delete(context)
            onDismiss.invoke()
        })
    }

}