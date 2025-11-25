package com.aacomposetest.Classes

import androidx.compose.ui.graphics.Color
import com.aacomposetest.R
import com.aacomposetest.ui.theme.MyColor

object Storeables {
    enum class Drawable(val resID: Int){
        AppIcon(R.drawable.ic_launcher_foreground),
        YellowSpiral(R.drawable.yellow_spiral),
        Home(R.drawable.ic_home),
        Settings(R.drawable.ic_settings)
    }

    enum class Colors(val color: Color){
        Pink(MyColor.Pink40),
        Blue(MyColor.Blue)
    }





}