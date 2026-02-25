package com.aacomposetest.Classes

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.compose.ui.input.key.Key
import com.aacomposetest.Classes.CursorValue
import kotlin.jvm.Throws


object MyPrefs {
    enum class PrefDirs(val dirKey: String){
        PlayerData("Player Data"),
        VisualData("Visual Data")
    }
    fun PrefDirs.open(context: Context): SharedPreferences{
        return context.applicationContext.getSharedPreferences(this.dirKey,MODE_PRIVATE)
    }
    
    fun getPrefs(context: Context,prefDir: PrefDirs): SharedPreferences{
        return prefDir.open(context)
    }
    
    val _prefs_LastSongId = CursorValue("LastSongId",Long::class,PrefDirs.PlayerData)
    val _prefs_LastPlaylistTitle = CursorValue("LastPlaylistTitle",String::class,PrefDirs.PlayerData)
    val _prefs_LastSongPosition = CursorValue("LastSongPosition",Long::class,PrefDirs.PlayerData)

    val _prefs_songListstate_FirstItem = CursorValue("SongListState_FirstItem",Int::class,PrefDirs.VisualData)
    val _prefs_songListState_Offset = CursorValue("SongListState_Offset",Int::class,PrefDirs.VisualData)
    val _prefs_songListState_NestedScrollOffset = CursorValue("SongListState_NestedScrollOfster",Float::class, PrefDirs.VisualData)

    val _prefs_playlistsListState_FirstItem = CursorValue("PlaylistsListState_FirstItem",Int::class,PrefDirs.VisualData)
    val _prefs_playlistsListState_Offset = CursorValue("PlaylistsListState_Offset",Int::class,PrefDirs.VisualData)

    val _prefs_PagerPosition = CursorValue("PagerTab",String::class,PrefDirs.VisualData)

    
    val _prefs_globalSongsSortType = CursorValue("GlobalSongsSortType",String::class,PrefDirs.VisualData)
    val _prefs_globalSongsSortDirection = CursorValue("GlobalSongsSortirection",String::class,PrefDirs.VisualData)

    val _prefs_playlistSongsSortType = CursorValue("PlaylistSongsSortType",String::class,PrefDirs.VisualData)
    val _prefs_playlistSongsSortDirection = CursorValue("PlaylistSongsSortirection",String::class,PrefDirs.VisualData)

    
    

    
}