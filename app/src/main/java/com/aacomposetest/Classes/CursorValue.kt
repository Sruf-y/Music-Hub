package com.aacomposetest.Classes

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
)