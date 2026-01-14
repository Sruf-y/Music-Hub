package Navigation.Interior.Destinations

import Presentation.FullscreenPlayer.FullscreenPlayerPage
import Presentation.FullscreenPlayer.FullscreenSong
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable

@Serializable
object FullscreenPlayer {

    @Composable
    fun invoke(page:Int = 0){
        FullscreenPlayerPage(modifier = Modifier.fillMaxSize(), initialPage = page)
    }
}