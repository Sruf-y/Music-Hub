package Navigation

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistPage(val title: String) {

    companion object{
        @Composable
        fun invoke(title:String,content:@Composable (title:String)-> Unit){

        }
    }
}