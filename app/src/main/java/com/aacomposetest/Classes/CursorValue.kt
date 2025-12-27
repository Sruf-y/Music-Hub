package com.aacomposetest.Classes

import android.content.SharedPreferences
import com.aacomposetest.Classes.MyPrefs.getCursorValue
import kotlin.reflect.KClass

data class CursorValue<T : Any>(
    /**
     * Name of the column to pull/put data.
     * */
    var name: String,
    /**
     * The class to try casting the data to/from
     * */
    var dataType: KClass<T>
){
    fun getFromPrefs(sharedPreferences: SharedPreferences,errorValue:T):T{
        return sharedPreferences.getCursorValue(this,errorValue)
    }
}