package Navigation.Destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.plus
import androidx.compose.runtime.getValue
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory


data class NavigationUiState(
   val currentDestination: Destination,
   val backStack: List<Destination>
)

sealed interface NavigationEffect {
   data object NavigateUp : NavigationEffect
}

class NavigationViewModel(
   private val savedStateHandle: SavedStateHandle,
   private val startDestination: Destination = Destination.PlayerContainer.MainPager
) : ViewModel(

) {
   
   companion object{
      val KEY_CURRENT_DEST = "currentDest"
      val KEY_BACK_STACK = "backStack"
      
      fun factory(start: Destination) = viewModelFactory {
         initializer {
            NavigationViewModel(
               savedStateHandle = createSavedStateHandle(),
               startDestination = start,
            )
         }
      }
   }
   
   private val _uiState = MutableStateFlow(
      NavigationUiState(
         currentDestination = savedStateHandle.get<Destination>(KEY_CURRENT_DEST)
            ?: startDestination,
         backStack = savedStateHandle.get<ArrayList<Destination>>(KEY_BACK_STACK)
            ?.takeIf { it.isNotEmpty() }
            ?: listOf(startDestination)
      )
   )
   val uiState: StateFlow<NavigationUiState> = _uiState.asStateFlow()
   
   private val _effects = Channel<NavigationEffect>(Channel.BUFFERED)
   val effects: Flow<NavigationEffect> = _effects.receiveAsFlow()
   
   private fun persist() {
      val state = _uiState.value
      savedStateHandle["currentDest"] = state.currentDestination
      savedStateHandle["backStack"] = ArrayList(state.backStack)
   }
   
   fun navigateTo(destination: Destination) {
      val current = _uiState.value
      if (current.currentDestination == destination) return
      _uiState.update {
         it.copy(
            currentDestination = destination,
            backStack = it.backStack + destination
         )
      }
      persist()
   }
   
   fun onNavigateUp(): Boolean {
      val state = _uiState.value
      if (state.backStack.size <= 1) return false
      val newStack = state.backStack.dropLast(1)
      _uiState.update { it.copy(currentDestination = newStack.last(), backStack = newStack) }
      persist()
      _effects.trySend(NavigationEffect.NavigateUp)
      return true
   }
   
   fun onDestinationChanged(destination: Destination) {
      val state = _uiState.value
      if (state.currentDestination == destination) return
      _uiState.update {
         it.copy(
            currentDestination = destination,
            // Don't duplicate if this is a restore or a "back" navigation
            backStack = if (it.backStack.lastOrNull() == destination) it.backStack
            else it.backStack + destination
         )
      }
      persist()
   }
   
   @Composable
   fun PreContent(navController: NavHostController){
      val uiState by uiState.collectAsStateWithLifecycle()
      // One-shot effects
      LaunchedEffect(Unit) {
         effects.collectLatest { effect ->
            when (effect) {
               NavigationEffect.NavigateUp -> navController.navigateUp()
            }
         }
      }
      
      // React to state changes: navigate when currentDestination changes
      LaunchedEffect(uiState.currentDestination) {
         val target = uiState.currentDestination
         if (!Destination.Companion.isAlreadyOn(navController, target)) {
            target.onNavigateTo(navController)
         }
      }
   }
   
   @Composable
   fun AfterContent(navController: NavHostController){
      // Sync NavController -> ViewModel (deep links, system back, NavController-internal pops)
      DisposableEffect(navController) {
         val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
            val mapped = controller.toCurrentDestination() ?: return@OnDestinationChangedListener
            onDestinationChanged(mapped)
         }
         navController.addOnDestinationChangedListener(listener)
         onDispose { navController.removeOnDestinationChangedListener(listener) }
      }
   }
}