package Navigation

import com.aacomposetest.Classes.Storeables

enum class NavigationItem(
    val title:String,
    val icon: Storeables.Drawable
) {
    Home(
        title = "Home",
        icon = Storeables.Drawable.Home
    ),
    Settings(
        title = "Settings",
        icon = Storeables.Drawable.Settings
    )
}