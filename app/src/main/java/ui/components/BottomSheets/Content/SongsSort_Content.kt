package ui.components.BottomSheets.Content

import AppDatabase.SongRelated.Song
import SrufComposeComponents.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aacomposetest.ui.theme.ComposeTestTheme
import com.aacomposetest.ui.theme.MyColor
import ui.components.CancelConfirm

@Composable
fun SongsSort_Content(
    currentType:String,
    currentDirection:String,
    sortTypeOptions:List<String> = emptyList<String>(),
    sortDirectionOptions:List<String> = emptyList<String>(),
    onConfirm:(sortType: String,sortDirection:String)->Unit = {it,that-> Unit },
    onCancel:()->Unit = {}
){

    var sortTypeState by remember { mutableStateOf(currentType) }

    var sortDirectionState by remember { mutableStateOf(currentDirection) } // false -> normal sort, true -> inverse sort

    val radioButtonColors = RadioButtonColors(RadioButtonDefaults.colors().selectedColor,RadioButtonDefaults.colors().unselectedColor,RadioButtonDefaults.colors().selectedColor,RadioButtonDefaults.colors().unselectedColor)


    Column(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()){

        Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()){
            Text("Filter by:",modifier = Modifier.padding(10.dp), fontSize = 22.sp)
        }

        // types of filters
        Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()){
            Column {
                sortTypeOptions.forEach {

                    Row(modifier = Modifier.clickable(onClick = {
                        sortTypeState = it
                    }), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = it, modifier = Modifier.weight(1f).padding(start = 10.dp))
                        RadioButton(selected = it == sortTypeState, onClick = {},
                            enabled = false,
                            colors = radioButtonColors
                        )
                    }
                }
            }
        }
        HorizontalDivider()

        // direction of filter
        Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()){
            Column {
                sortDirectionOptions.forEach {
                    Row(modifier = Modifier.clickable(onClick = {
                        sortDirectionState = it
                    }), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = it, modifier = Modifier.weight(1f).padding(start = 10.dp))
                        RadioButton(selected = it == sortDirectionState, onClick = {},
                            enabled = false,
                            colors = radioButtonColors
                        )
                    }
                }
            }
        }
        // on confirm -> call the onConfirm function to let the callback set the songs
        CancelConfirm(onConfirm = {onConfirm.invoke(sortTypeState,sortDirectionState)}, onCancel = onCancel)
    }
}
@Preview(showBackground = true)
@Composable
private fun Preview(){
    val sortTypeOptions = listOf<String>("Title","Author","Length")
    val sortDirectionOptions = listOf<String>("Descending","Ascending")

    ComposeTestTheme {
        Surface {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom ){
                SongsSort_Content(sortTypeOptions.first(),sortDirectionOptions.first(),sortTypeOptions,sortDirectionOptions)
            }
        }
    }
}