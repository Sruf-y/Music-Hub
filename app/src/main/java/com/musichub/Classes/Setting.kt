package com.musichub.Classes

data class Setting<T: Any>(val cursorValue: CursorValue<T>, var value:T)  {}