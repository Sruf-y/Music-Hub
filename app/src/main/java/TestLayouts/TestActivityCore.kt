package TestLayouts

import SrufComposeComponents.Text
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aacomposetest.TestSource
import com.aacomposetest.ui.theme.SrufTheme

class TestActivityCore : ComponentActivity() {
   
   @OptIn(ExperimentalMaterial3Api::class)
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      enableEdgeToEdge()
      
      setContent {
         Box(Modifier.fillMaxSize().safeDrawingPadding().background(Color.Blue.copy(alpha = 0.7f))){
            LazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
               items(count = TestSource.Sources.entries.size,key = { it }) {
                  Button({
                     val intent = TestActivity.newInstance(TestSource.Sources.entries[it],this@TestActivityCore)
                     
                     startActivity(intent)
                  }) {
                     Text(TestSource.Sources.entries[it].name,color = Color.White)
                  }
               }
            }
         }
         
      }
      
   }
   
   companion object{
   }
}