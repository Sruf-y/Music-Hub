package Navigation.Destinations

import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class Page_ViewPlaylist(val title: String) {
   
   companion object{
      @Composable
      fun invoke(title:String,content:@Composable (title:String)-> Unit){
      
      }
   }
}

