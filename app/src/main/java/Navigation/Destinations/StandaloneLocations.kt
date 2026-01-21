package Navigation.Destinations

import Presentation.FullscreenPlayer.FullscreenPlayerPage
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable

@Serializable
object StandaloneLocations {
   
   @Serializable
   object FullscreenPlayer {
      
      @Composable
      fun invoke(page:Int = 0){
         FullscreenPlayerPage(modifier = Modifier.fillMaxSize(), initialPage = page)
      }
   }
   
   @Serializable
   data class PlaylistAddSongsPage(val title:String) {
      
      
      companion object{
         @Composable
         fun invoke(title:String,content:@Composable (title:String)-> Unit){
         
         }
      }
   }
   
}