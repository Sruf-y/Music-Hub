package Navigation

import AppDatabase.SongRelated.Song
import kotlinx.coroutines.flow.MutableStateFlow

object SongPinpointViewModel {
   
   /**
    *
    * */
   sealed interface Events{
      class FindSong(val songId: Long,val playlistTitle:String?) : Events
      class Done() : Events
   }
   
   val eventsFlow = MutableStateFlow<Events>(Events.Done())
   
   /**
    * First of all, this can only be actioned while inside the SongButtomSheet container. So i can
    * only have the navigator that navigates between the main pager and the playlist viewing page.
    *
    * And also i have the pager
    * */
   fun findCurrentlyPlayingSong(songs:List<Song>, songId: Long, playlistTitle:String?){
      if(songs.map { it.id }.contains(songId)) {
         eventsFlow.tryEmit(Events.FindSong(songId,playlistTitle))
      }
   }
   
   fun onFinished(){
      eventsFlow.tryEmit(Events.Done())
   }
   
}