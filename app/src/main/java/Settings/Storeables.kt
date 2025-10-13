package Settings

import androidx.compose.ui.graphics.Color
import com.aacomposetest.R
import com.aacomposetest.ui.theme.MyColor

object Storeables {
    enum class Drawables(val resID: Int){
        AppIcon(R.drawable.ic_launcher_foreground),
        YellowSpiral(R.drawable.yellow_spiral)
    }

    enum class Colors(val color: Color){
        Pink(MyColor.Pink40),
        Blue(MyColor.Blue)
    }





}