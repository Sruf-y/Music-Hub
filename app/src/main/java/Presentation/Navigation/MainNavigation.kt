package Presentation.Navigation

import Presentation.Navigation.Destinations.MainPage
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import io.ktor.http.content.OutgoingContent

@Composable
fun MainNavigation(navController: NavHostController,content: NavGraphBuilder.()-> Unit){
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = MainPage,
        enterTransition = {

            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },// page is navigated TO normally
        popExitTransition = {

            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },// page is navigated FROM by popBackStack() (returning FROM the page to the previous one)
        popEnterTransition = {

            EnterTransition.None
        },// page is being returned to by the above page animating away
        exitTransition = {

            ExitTransition.None
        },// page stays back while another page is animated above

    ) {
        content.invoke(this)
    }
}