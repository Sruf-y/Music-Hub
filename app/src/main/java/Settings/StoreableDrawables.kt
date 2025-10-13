package Settings

import com.aacomposetest.R
object StoreableDrawables {
    enum class Name{
        PLAYLIST_DEF_IC,
        FUNKY_BACKGROUND
    }

    fun getDrawableRes(name: Name): Int{
        return when(name){
            Name.FUNKY_BACKGROUND ->R.drawable.yellow_spiral

            Name.PLAYLIST_DEF_IC -> R.drawable.ic_launcher_foreground
        }
    }
}