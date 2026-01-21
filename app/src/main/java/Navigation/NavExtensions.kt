package Navigation

import Navigation.Destinations.BS_PlayerHolder
import androidx.navigation.NavHostController
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

object NavExtensions {
   
   @OptIn(InternalSerializationApi::class)
   /***
    * Only navigates to the given destination if the destination isn't the top of the navigation stack.
    */
   inline fun <reified T: Any> NavHostController.safeNavigate(
      route: T,
   ) {
      val doNavigate = {
         this.navigate(route) {
            //launchSingleTop = allowRenavigation
            //restoreState = alloRestoreState
         }
      }
      
      // Honestly this just checks the same thing as launchSingleTop so its useless. Leaving it here as a comment tho for the future maybe
      if (this.currentDestination?.route.toString() != T::class.serializer().descriptor.serialName) {
         doNavigate.invoke()
      }
      
   }
   
}

