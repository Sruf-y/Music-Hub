package Navigation.Destinations

import AppDatabase.SongRelated.Song
import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import MyViewModels.PlaylistWatcher
import SrufComposeComponents.IconButton
import SrufComposeComponents.Text
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aacomposetest.Classes.MyIcons
import com.aacomposetest.Functions.mapAfterOrderOf
import com.aacomposetest.MyApplication
import com.aacomposetest.R
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import ui.components.BottomSheets.renderDefaultBackground
import ui.components.SongItem

@Serializable
data class Page_ViewPlaylist(val title: String) {
   
   companion object{
      
      @Composable
      fun PlaylistViewing(
         modifier:Modifier = Modifier.fillMaxSize(),
         lazyListState: LazyListState,
         playlistTitle: String,
         onBack:(()-> Unit)? = null,
         bottomPadding: Dp = 0.dp,
         thumbNailSize:Dp = 150.dp,
         plusButtonClick:(()-> Unit)? =null,
         onOptionsClickShowMenu: @Composable ((MutableState<Boolean>) -> Unit)? = null
      ) {
         renderDefaultBackground()
         
         /**
          * Unused for now because Navigation is taking care of it.
          * */
         BackHandler(onBack!=null) {
            onBack?.invoke()
         }
         
         // Use this value for searching/filtering in the playlist. TODO
         val isSearching = remember { false }
         
         
         val contextRef = LocalContext.current
         
         val allSongs by ListFeedingVM.allSongs.collectAsState()
         
         
         /** Holds the playlist that is shown on this screen. */
         val onScreenPlaylistVM = remember {
            PlaylistWatcher(
               fileWatcher = MyApplication.playlistFolderWatcher
            )
         }
         
         val playlist by onScreenPlaylistVM.playlist.collectAsState()
         
         val currentlyPlayingSong by myExoPlayer.currentlyPlayingSong.stateFlow.collectAsState()
         
         
         val const_PlaylistSongs by remember {
            derivedStateOf {
               
               allSongs.mapAfterOrderOf(playlist?.songsIdList ?: emptyList()) { it.id }
               
            }
         }
         
         var playlistSongs by remember {
            mutableStateOf(
               allSongs.filter {
                  playlist?.songsIdList?.contains(it.id) == true
               }
            )
         }
         
         
         // Get the playlist
         DisposableEffect(Unit) {
            
            /**
             * Set current playlist. Listener is added and removed automatically on ViewModel creation and destruction.
             * */
            onScreenPlaylistVM.changePlaylist(playlistTitle,contextRef)
            
            onDispose {
               /**
                * Saves on decomposition.
                * */
               playlist?.save(contextRef)
            }
         }
         
         
         
         
         
         
         val menuIsExpanded = remember { mutableStateOf(false) }
         
         
         
         /**
          * Update the list of songs like this if the real list updates because [playlistSongs] can be reordered
          * */
         LaunchedEffect(const_PlaylistSongs) {
            
            playlistSongs = const_PlaylistSongs
         }
         
         /**
          * State for the reorderable list.
          * */
         val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
            playlistSongs = playlistSongs.toMutableList().let {
               it.add(to.index-2, it.removeAt(from.index-2))
               it
            }
         }
         
         Box(modifier = modifier) {
            
            
            val sHeaderTitle by remember {
               derivedStateOf {
                  playlist?.title?.let { it + " (${playlist!!.songsIdList.size})" } ?: "Error"
               }
            }
            
            val removeItemOnClickListener: (Song) -> Unit = remember {
               { song ->
                  playlistSongs =
                     playlistSongs.toMutableList().let {
                        it.removeIf { it.id == song.id }
                        
                        it
                     }
                  if (myExoPlayer.currentlyPlayingSong.getValue?.id == song.id) {
                     myExoPlayer.setCurrentPlaylist(null)
                  }
                  playlist?.songsIdList?.clear()
                  playlist?.songsIdList?.addAll(playlistSongs.map { it.id })
                  
                  playlist?.save(contextRef)
               }
            }
            
            
            LazyColumn(
               modifier = Modifier
                  .fillMaxSize(),
               state = if (playlistSongs.isEmpty()) rememberLazyListState() else lazyListState,
               contentPadding = PaddingValues(bottom = bottomPadding)
            ) {
               
               
               item(key = "PlaylistIcon") {
                  Box(
                     modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                  ) {
                     Icon(
                        painter = painterResource(
                           playlist?.thumbNail ?: R.drawable.exclamation_sign
                        ),
                        contentDescription = null,
                        tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        modifier = Modifier
                           .size(thumbNailSize)
                           .align(Alignment.Center)
                           .padding(20.dp)
                     )
                  }
                  
               }
               
               
               stickyHeader(key = "PlaylistStickyBar") {
                  Box(
                     modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(Theme.Sourcesets.ToolbarBackground()),
                  ) {
                     
                     Text(
                        text = sHeaderTitle,
                        modifier = Modifier
                           .align(Alignment.CenterStart)
                           .padding(horizontal = 30.dp)
                     )
                     
                     Row(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        verticalAlignment = Alignment.CenterVertically
                     ) {
                        
                        
                        // PLUS ICON
                        IconButton(
                           resId = R.drawable.ic_add,
                           iconSize = Size(0.75f, 0.75f),
                           modifier = Modifier.size(MyIcons.sizeMedium, MyIcons.sizeMedium)
                        ) {
                           plusButtonClick?.invoke()
                        }
                        
                        // More options button
                        Box(
                           contentAlignment = Alignment.Center
                        ) {
                           
                           IconButton(
                              imageVector = Icons.Default.MoreVert,
                              modifier = Modifier.size(MyIcons.sizeMedium, MyIcons.sizeMedium),
                              iconSize = Size(0.75f, 0.75f)
                           ) {
                              menuIsExpanded.value = true
                           }
                           
                           // Show the menu, hence a Composable
                           onOptionsClickShowMenu?.invoke(menuIsExpanded)
                        }
                     }
                     
                     
                  }
               }
               
               
               items(
                  items = playlistSongs,
                  key = { it.id }
               ) { song ->
                  
                  val textColor = if (currentlyPlayingSong?.id == song.id) {
                     MyColor.SelectedGold
                  } else {
                     MyColor.TextColor()
                  }
                  
                  
                  ReorderableItem(reorderableLazyListState, key = song.id) { isDragging ->
                     val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                     
                     Surface(shadowElevation = elevation, color = MyColor.Transparent) {
                        SongItem(
                           song = song,
                           textColor = textColor,
                           modifier = Modifier
                              .background(if (isDragging) MyColor.SemiTransparentDark else MyColor.Transparent)
                              .clickable(
                                 onClick = {
                                    
                                    ExoPlayerViewModel.setSong(
                                       song = song,
                                       startOnPrepared = true,
                                       playlistTitle = playlist?.title,
                                    )
                                 }
                              )
                              .animateItem(),
                           optionsMenu = { showMenu ->
                              DropdownMenu(
                                 expanded = showMenu.value,
                                 onDismissRequest = { showMenu.value = false }) {
                                 DropdownMenuItem(
                                    text = { Text("Remove") },
                                    onClick = {
                                       removeItemOnClickListener.invoke(song)
                                       
                                       showMenu.value = false
                                    }
                                 )
                              }
                              
                           }
                        ) {
                           if (!isSearching)
                              androidx.compose.material3.IconButton(
                                 modifier = Modifier.draggableHandle(
                                    onDragStopped = {
                                       
                                       val auxList = playlistSongs.map { it -> it.id }
                                       
                                       playlist?.songsIdList?.clear()
                                       
                                       playlist?.songsIdList?.addAll(auxList)
                                       
                                       playlist?.save(contextRef) {
                                          Toast.makeText(
                                             contextRef,
                                             "Playlist saved!",
                                             Toast.LENGTH_SHORT
                                          ).show()
                                       }
                                    }
                                 ),
                                 onClick = {
                                 
                                 },
                              ) {
                                 Icon(
                                    painter = painterResource(R.drawable.ic_drag_handle),
                                    contentDescription = "Reorder",
                                    modifier = Modifier.fillMaxHeight(0.9f).aspectRatio(1f),
                                    tint = MyColor.TextColor()
                                 )
                              }
                        }
                     }
                  }
                  
               }
               
            }
         }
         
         
      }
   }
}

