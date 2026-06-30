package Navigation.Destinations

import Settings.MySettings
import Settings.SettingsRepository
import SrufComposeComponents.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aacomposetest.Classes.rememberSimpleDropdownMenu
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import androidx.compose.runtime.getValue
import com.aacomposetest.Classes.SimpleDropdownMenuState
import com.composables.core.HorizontalSeparator
import kotlinx.serialization.Serializable
import ui.components.Overlay

private val settings: SettingsRepository?
   get() {return MySettings.settings}

@Serializable
object SettingsDest {
   @Composable
   fun Page(){
      Box(Modifier.fillMaxSize()){
         
         Theme.RenderDefaultBackground()
         
         val lazyListState = rememberLazyListState()
         
         LazyColumn(state = lazyListState,modifier = Modifier.fillMaxSize()) {
            stickyHeader {
               Box(
                  modifier = Modifier
                     .fillMaxWidth()
                     .wrapContentHeight()
                     .background(Theme.Sourcesets.ToolbarBackground())
                     .padding(10.dp),
                  contentAlignment = Alignment.CenterStart
               ){
                  Text("Settings", fontSize = 20.sp)
               }
            }
            settings?.let {settings->
               
               item("Shuffle") {
                  val value by settings.playerAllowShuffle.stateFlow.collectAsState()
                  SettingItem(
                     title = "Player Shuffle",
                     onClick = {
                        settings.playerAllowShuffle.setValue(!value)
                     }
                  ) {
                     Switch(value, {})
                  }
               }
               item("Orientation") {
                  HorizontalSeparator(MyColor.SeparatorGray)
                  val value by settings.orientation.stateFlow.collectAsState()
                  val dropdownState = rememberSimpleDropdownMenu(
                     listOf(
                        MySettings.ScreenOrientation.Auto.name to {
                           settings.orientation.setValue(MySettings.ScreenOrientation.Auto.value)
                        },
                        MySettings.ScreenOrientation.Portrait.name to {
                           settings.orientation.setValue(MySettings.ScreenOrientation.Portrait.value)
                        },
                        MySettings.ScreenOrientation.LandScape.name to {
                           settings.orientation.setValue(MySettings.ScreenOrientation.LandScape.value)
                        }
                     )
                  )
                  
                  SettingItem(
                     title = "Orientation",
                     onClick = {
                        dropdownState.open()
                     }
                  ) {
                     ChoiceButton(
                        text = MySettings.ScreenOrientation.fromInt(value).name,
                        dropdownState = dropdownState
                     )
                  }
               }
               item("Loop songs") {
                  HorizontalSeparator(MyColor.SeparatorGray)
                  val value by settings.playerAllowLoopSong.stateFlow.collectAsState()
                  SettingItem(
                     title = "Loop Songs",
                     onClick = {
                        settings.playerAllowLoopSong.setValue(!value)
                     }
                  ) {
                     Switch(value,{})
                  }
               }
               item("Loop at end") {
                  HorizontalSeparator(MyColor.SeparatorGray)
                  val value by settings.playerAllowLoopBack.stateFlow.collectAsState()
                  SettingItem(
                     title = "Loop Back",
                     onClick = {
                        settings.playerAllowLoopBack.setValue(!value)
                     }
                  ) {
                     Switch(value,{})
                  }
               }
               item("Loop around at beginning") {
                  HorizontalSeparator(MyColor.SeparatorGray)
                  val value by settings.playerAllowLoopAround.stateFlow.collectAsState()
                  SettingItem(
                     title = "Loop Back Around",
                     onClick = {
                        settings.playerAllowLoopAround.setValue(!value)
                     }
                  ) {
                     Switch(value,{})
                  }
               }
            }
         }
            
         
      }
   }
   
   @Composable
   fun SettingItem(
      title:String,
      onClick:()-> Unit,
      component: @Composable ()-> Unit
   ){
      Overlay(
         overlay = {
            Box(
               Modifier.clickable(onClick = onClick)
            )
         }
      ) {
         Row(
         modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
         verticalAlignment = Alignment.CenterVertically,
         horizontalArrangement = Arrangement.SpaceBetween
      ) {
         Text(title)
            
            component.invoke()
         }
      }
   }
   private val shape = RoundedCornerShape(5.dp)
   @Composable
   fun ChoiceButton(
      text: String,
      dropdownState: SimpleDropdownMenuState
   ){
      
      Box(modifier = Modifier
         .wrapContentSize()
         .border(
            2.dp,
            Theme.Sourcesets.thumb,
            shape
         )
         .background(Theme.background())
         .padding(vertical = 5.dp, horizontal = 8.dp),
         contentAlignment = Alignment.Center
      ){
         Text(text = text)
         
         dropdownState.RenderMenu()
      }
      
   }
}