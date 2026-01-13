package TestLayouts


import android.content.ContentResolver
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aacomposetest.ui.theme.SrufTheme
import SrufComposeComponents.Text as Text
import SrufComposeComponents.ElevatedButton as ElevatedButton

object RememberTest {

    //@Preview()
    @Composable
    fun incrementalNumberButton(contentResolver: ContentResolver,context: Context) {
        SrufTheme {







            var myNumber by rememberSaveable { mutableIntStateOf(0) }
            var text by rememberSaveable { mutableStateOf(myNumber.toString()) }

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    BasicTextField(
                        value = text,
                        onValueChange = { newVal ->
                            text = newVal
                            newVal.toIntOrNull()?.let { myNumber = it }
                        },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    ElevatedButton(
                        onClick = {
                            myNumber++
                            text = myNumber.toString()
                        }
                    ) {
                        SrufComposeComponents.Text("Increase") // now uses onPrimary
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }

                Text(
                    "Whatever man",
                    fontSize = 20.sp
                )
            }
        }
    }
}