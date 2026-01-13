package ui.components.BottomSheets.Content

import AppDatabase.PlaylistRelated.Playlist
import SrufComposeComponents.MyModalBottomSheet
import SrufComposeComponents.Text
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aacomposetest.Functions.rename
import com.aacomposetest.ui.theme.SrufTheme
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import ui.components.CancelConfirm


@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
private fun Preview(){
    SrufTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MyColor.Black),
            contentAlignment = Alignment.BottomCenter
        ) {
            PlaylistAddOrEdit_Content()
        }


    }
}

@Composable
fun PlaylistAddOrEdit_Content(
    existingPlaylists: List<Playlist>? = null,
    bottomSheetVM: MyModalBottomSheet.ModalVM? = null,
    playlist: Playlist? = null,
    isFocusHeld: MutableState<Boolean> = mutableStateOf(false)
) {

    val contextRef = LocalContext.current


    var myText by remember { mutableStateOf(playlist?.title?:"") }

    val onDismiss = {
        bottomSheetVM?.content?.value = null
        bottomSheetVM?.dismissSheet()
    }


    fun saveLogic(){

        if(myText.isNotEmpty()){
            if( !(existingPlaylists?.map { it->it.title }?.contains(myText)?:false)){
                val invalidChars = "[\\\\/:*?\"<>]".toRegex()
                myText = myText.replace(invalidChars, "_")
                myText = myText.replace("|", "\\u2503")


                val file = playlist?.getFileIfExists(contextRef)

                if (file != null) {
                    file.rename(myText)
                } else {
                    Playlist(myText).save(contextRef)
                }

                onDismiss.invoke()

            }else{
                Toast.makeText(contextRef,"Playlist already exists!", Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Toast.makeText(contextRef,"Title must not be blank!", Toast.LENGTH_SHORT).show()
        }


    }


    val focusManager = LocalFocusManager.current

    Column(Modifier
        .wrapContentSize()
    ) {



        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 30.dp)
                ,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(Modifier.weight(0.5f))

            OutlinedTextField(modifier = Modifier
                .weight(3f)
                .fillMaxWidth()
                .onFocusChanged({
                    isFocusHeld.value = it.isFocused
                }),
                value = myText,
                label = {
                    Text(text = "Playlist name", color = MyColor.Inactive)
                },
                onValueChange = { myText = it },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Theme.colorScheme().inverseSurface,
                    focusedTextColor = Theme.colorScheme().inverseSurface,
                    focusedContainerColor = Theme.colorScheme().secondary)
            )

            Spacer(Modifier.weight(0.5f))

        }

        CancelConfirm(
            onCancel = {
                onDismiss.invoke()
            },
            onConfirm = {

                saveLogic()
            }
        )
    }
}