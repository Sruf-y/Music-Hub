package Presentation.Navigation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.aacomposetest.ui.theme.MyColor

@Composable
fun DrawerStandardLayout(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .padding(5.dp),
    header: @Composable ()-> Unit = {},
    items: @Composable ColumnScope.()->Unit
) {


    Scaffold(modifier = modifier
        .pointerInput(Unit) {
            detectTapGestures()
        },
        containerColor = MyColor.Transparent
    ){innerPadding->

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            header.invoke()

            items.invoke(this)

        }
    }
}