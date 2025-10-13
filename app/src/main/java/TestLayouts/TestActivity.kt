package TestLayouts


import AppDatabase.SongRelated.Song
import ExoPlayer.myExoPlayer
import SrufComposeComponents.Text
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.util.Log as External_Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.aacomposetest.Classes.HeaderAwareList
import com.aacomposetest.Classes.rememberHeaderAwareListState
import com.aacomposetest.Logs
import com.aacomposetest.ui.theme.ComposeTestTheme
import com.aacomposetest.ui.theme.MyColor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import ui.components.SongItem
import kotlin.math.roundToInt



class TestActivity : ComponentActivity() {
    val httpVM by viewModels<HttpViewmodel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val a = List(100) { index -> Song.emptySong(index.toLong()) }
        setContent {
            ComposeTestTheme {

                mainfunction()

            }

        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun mainfunction(){

        var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {

            posts = httpVM.fetchPosts(tags = "glory_hole m/m",limit = 10)
            isLoading = false
        }

        Surface {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){

                if(isLoading){
                    Text("Loading...")
                }else{
                    LazyVerticalGrid(columns = GridCells.Fixed(3)) {

                        itemsIndexed(posts,key = {index,it->it.id}) {index,post->
                            Card(modifier = Modifier.padding(8.dp)) {
                                AsyncImage(model = post.preview.url?:post.file.url,
                                    contentDescription = "e621 image"
                                )
                                Text("ID: ${post.id}")
                                Text("Artist: ${post.tags.artist.joinToString()}")
                            }
                        }
                    }
                }
            }
        }
    }






}

class HttpViewmodel: ViewModel(){

    private val timeout:Long = 5000

    val client = HttpClient(Android) {
        install(HttpTimeout){
            requestTimeoutMillis= timeout
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true  // ignore data not required in the Post class
            })
        }
    }

    suspend fun fetchPosts(tags: String = "", limit: Int = 10): List<Post> {
        val url = "https://e621.net/posts.json?tags=$tags&limit=$limit"

        return try{
            client.get(url) {
                header("User-Agent", "MyTestMobileApp/1.0 (by PetSruf on e621)")
            }.body<PostsResponse>().posts

        } catch (e: ClientRequestException) {
            Log.e(Logs.HTTP.name, "4xx: ${e.response.status}")
            emptyList()
        } catch (e: ServerResponseException) {
            Log.e(Logs.HTTP.name, "5xx: ${e.response.status}")
            emptyList()
        } catch (e: HttpRequestTimeoutException) {
            Log.e(Logs.HTTP.name, "Timeout: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            Log.e(Logs.HTTP.name, "Unknown error: ${e.message}")
            emptyList()
        }

    }
}
@Serializable
data class PostsResponse(
    val posts: List<Post>
)

@Serializable
data class Post(
    val id: Int,
    val file: FileInfo,
    val preview: PreviewInfo,
    val tags: Tags,
    val rating: String
)

@Serializable
data class FileInfo(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class PreviewInfo(
    val url: String? = null
)

@Serializable
data class Tags(
    val artist: List<String> = emptyList(),
    val character: List<String> = emptyList(),
    val general: List<String> = emptyList()
)