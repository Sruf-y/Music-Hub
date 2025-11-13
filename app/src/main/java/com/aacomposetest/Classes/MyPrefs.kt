package com.aacomposetest.Classes

import android.content.SharedPreferences
import com.aacomposetest.Classes.CursorValue
import kotlin.jvm.Throws


object MyPrefs {
    object PrefDirs{
        val GenericData = "Generic Data"
    }


    val _prefs_LastSongId = CursorValue("LastSongId",Long::class)
    val _prefs_LastPlaylistTitle = CursorValue("LastPlaylistTitle",String::class)
    val _prefs_LastSongPosition = CursorValue("LastSongPosition",Long::class)

    val _prefs_songListstate_FirstItem = CursorValue("SongListState_FirstItem",Int::class)
    val _prefs_songListState_Offset = CursorValue("SongListState_Offset",Int::class)

    val _prefs_playlistsListState_FirstItem = CursorValue("PlaylistsListState_FirstItem",Int::class)
    val _prefs_playlistsListState_Offset = CursorValue("PlaylistsListState_Offset",Int::class)

    val _prefs_PagerPosition = CursorValue("PagerTab",Int::class)

    val _prefs_globalSongsSortType = CursorValue("GlobalSongsSortType",String::class)
    val _prefs_globalSongsSortDirection = CursorValue("GlobalSongsSortirection",String::class)

    val _prefs_playlistSongsSortType = CursorValue("PlaylistSongsSortType",String::class)
    val _prefs_playlistSongsSortDirection = CursorValue("PlaylistSongsSortirection",String::class)


    @Throws(IllegalArgumentException::class)
    fun <T : Any> SharedPreferences.Editor.putCursorValue(cursorData: CursorValue<T>,value:T){

        when(cursorData.dataType){
            Int::class -> {
                putInt(cursorData.name,value as Int)
            }
            Long::class -> {
                putLong(cursorData.name,value as Long)
            }
            String::class -> {
                putString(cursorData.name,value as String)
            }
            Float::class->{
                putFloat(cursorData.name,value as Float)
            }
            Boolean::class->{
                putBoolean(cursorData.name,value as Boolean)
            }
            else -> {
                throw IllegalArgumentException("Type not recognized or missmatched.")
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    fun <T: Any> SharedPreferences.getCursorValue(cursorData: CursorValue<T>,errorValue:T):T{

        return when(cursorData.dataType){
            Int::class -> {
                getInt(cursorData.name, errorValue as Int)
            }
            Long::class -> {
                getLong(cursorData.name,errorValue as Long)
            }

            String::class -> {
                getString(cursorData.name, errorValue as String)
            }
            Float::class->{
                getFloat(cursorData.name, errorValue as Float)
            }
            Boolean::class->{
                getBoolean(cursorData.name, errorValue as Boolean)
            }
            else -> {
                throw IllegalArgumentException("Type not recognized or missmatched.")
            }
        } as T
    }
}