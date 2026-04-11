package TestLayouts


import AppDatabase.SongRelated.Song
import SrufComposeComponents.Experimental.ColorPicker
import SrufComposeComponents.Text
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aacomposetest.ui.theme.SrufTheme
import com.aacomposetest.ui.theme.MyColor
import kotlinx.coroutines.launch
import SrufComposeComponents.NestedScrollingFront
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import com.aacomposetest.Log
import com.aacomposetest.TestSource
import ui.components.Text.BasicTextFieldExpanded


class TestActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val intentData = intent.extras?.getString("test")
        
        val test = try {
            TestSource.Sources.valueOf(intentData!!)
        }catch (ex: Exception){
            null
        }
        
        setContent {
            if(test!=null){
                TestSource.TestSwitch(test)
            }else{
                SrufTheme {
                    Box(Modifier
                            .fillMaxSize()
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No test found", color = Color.White)
                    }
                }
            }
            
        }

    }
    
    companion object{
        fun newInstance(test: TestSource.Sources,context: Context): Intent {
            
            
            return Intent(context,TestActivity::class.java).apply {
                this.putExtra("test",test.name)
            }
        }
    }
}















