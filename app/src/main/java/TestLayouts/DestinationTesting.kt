package TestLayouts

import Navigation.Destinations.MainPager
import Navigation.NavExtensions.safeNavigate
import Presentation.Navigation.MyNavigation
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aacomposetest.Log
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.SrufTheme
import kotlinx.serialization.Serializable
import ui.components.ViewDrawer
import ui.components.ViewDrawerValue
import ui.components.rememberViewDrawerState
import kotlin.random.Random


/**
 * What was learned: NavHost can 100% retain its state by simply... keeping the state above components it is nested in... So recomposition doesn't recompose those.
 * */
@Preview
@Composable
fun DestinationTesting(){
   
   
   SrufTheme {
      Surface(modifier = Modifier
         .fillMaxSize()
         .safeContentPadding()) {
         
         
         
         val drawerState = rememberViewDrawerState(ViewDrawerValue.Closed)
         
         val navController = rememberNavController()
         
         val localNavController = rememberNavController()
         
         ViewDrawer(state = drawerState,
            mainContent = {
            
            NavHost(navController = navController, startDestination = HomeDest){
               composable<HomeDest> {backstack->
                  val dest = backstack.toRoute<HomeDest>()
                  
                  dest.invoke({
                     navController.safeNavigate(SecondDest)
                  })
               }
               composable<SecondDest> {backstack->
                  val dest = backstack.toRoute<SecondDest>()
                  
                  dest.invoke({
                     navController.navigateUp()
                  },localNavController)
               }
            }
         }, drawerContent = {
            Box(modifier = Modifier
               .fillMaxSize()
               .background(MyColor.SemiTransparentDark)
            )
         })
         
         
      }
   }
}


@Serializable
object HomeDest{
   
   @Composable
   fun invoke(onClick:()->Unit = {}){
      Box(modifier = Modifier
         .fillMaxSize()
         .background(MyColor.Blue)
         .clickable(onClick = onClick))
   }
}

@Serializable
object SecondDest{
   
   @Composable
   fun invoke(onClick:()->Unit = {},localNavController: NavHostController){
      Box(modifier = Modifier
         .fillMaxSize()
         .background(MyColor.Purple)
         .clickable(onClick = onClick)
         .padding(50.dp)){
         NavHost(navController = localNavController, startDestination = ChildDest){
            composable<ChildDest> {
               it.toRoute<ChildDest>().invoke({localNavController.safeNavigate(SecondChildDest)})
            }
            composable<SecondChildDest> {
               it.toRoute<SecondChildDest>().invoke()
            }
         }
      }
   }
}


@Serializable
object ChildDest{
   @Composable
   fun invoke(onClick:()->Unit = {}){
      Box(modifier = Modifier
         .fillMaxSize()
         .background(MyColor.Red)
         .clickable(onClick = onClick))
   }
}
@Serializable
object SecondChildDest{
   @Composable
   fun invoke(onClick:()->Unit = {}){
      Box(modifier = Modifier
         .fillMaxSize()
         .background(MyColor.Yellow))
   }
}



