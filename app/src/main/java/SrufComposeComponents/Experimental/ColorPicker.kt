package SrufComposeComponents.Experimental

import android.app.Activity
import android.content.res.Configuration
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Composable
fun ColorPicker(controller: ColorPickerController= rememberColorPickerController()) {
    
    val configuration = LocalConfiguration.current
    
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if(!isLandscape)
    Column(
        // on below line we are adding a modifier to it,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
            
    ) {
        // Where the color is displayed for preview
        AlphaTile(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                
                .clip(RoundedCornerShape(6.dp)),
            
            controller = controller
        )
        
        
        HsvColorPicker(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            
            controller = controller,
           
            onColorChanged = {}
        )
        
        AlphaSlider(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp),
            
            controller = controller,
            
            tileOddColor = Color.White,
            tileEvenColor = Color.Black
        )
        
        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp),
            
            controller = controller,
        )
    }
    else{
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center
        ) {
            AlphaTile(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .weight(1f),
                
                controller = controller
            )
            
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f),
                
                controller = controller,
                
                onColorChanged = {}
            )
            
            AlphaSlider(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                
                controller = controller,
                
                tileOddColor = Color.White,
                tileEvenColor = Color.Black
            )
            
            BrightnessSlider(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                controller = controller,
            )
        }
    }
}