package TestLayouts


import SrufComposeComponents.Text
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aacomposetest.ui.theme.SrufTheme
import android.content.Context
import android.content.Intent
import com.aacomposetest.TestSource


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















