package Presentation.Navigation

import Navigation.Destinations.NavigationViewModel
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandIn
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.SavedState
import androidx.transition.Transition

@Composable
fun MyNavigation(navController: NavHostController,navigationViewModel: NavigationViewModel, content: NavGraphBuilder.()-> Unit){
    
    navigationViewModel.PreContent(navController)
    
    val initialDestination = remember {
        navigationViewModel.uiState.value.currentDestination   // read once
    }
    
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = initialDestination,
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
    
    navigationViewModel.AfterContent(navController)
}
