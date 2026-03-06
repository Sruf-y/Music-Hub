package Navigation.Destinations

import MyViewModels.ListFeedingVM
import com.aacomposetest.Classes.PersistentState
import SrufComposeComponents.CustomPagerTab
import SrufComposeComponents.IconButton
import SrufComposeComponents.KeyboardAwareBox
import SrufComposeComponents.NestedScrollingFront
import SrufComposeComponents.TabRowIndicator
import SrufComposeComponents.Text
import android.view.Gravity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.putCursorValue
import com.aacomposetest.Log
import com.aacomposetest.R
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import com.composables.core.HorizontalSeparator
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ui.components.CustomTextField


private val customIndicator =
   @Composable { tabPositions: List<TabPosition>,pagerState: PagerState ->
      TabRowIndicator(tabPositions, pagerState, 2.dp)
   }

@Serializable
object MainPager {
   
   @Composable
   fun invoke(initialPage:Int?, pageContent:@Composable (page:Int)->Unit) {
      
      val contextRef = LocalContext.current
      val prefs = remember { MyPrefs.getPrefs(contextRef, MyPrefs.PrefDirs.VisualData) }
      
      val pageState = remember { MainPagerState().apply {
         if(initialPage!=null){this.initialPage = initialPage}
      }}
      
      
      var pagerState = rememberPagerState(
         initialPage = pageState.initialPage,
         pageCount = { pageState.pages.size }
      )
      
      
      
      
      // Save current page
      LaunchedEffect(pagerState.currentPage) {
         
         pageState.initialPage = pagerState.currentPage
         
         prefs.edit() {
            putCursorValue(MyPrefs._prefs_PagerPosition,pagerState.currentPage.toString())
         }
      }
      
      
      /**
       * Main screen pager. This currently handles the Searchbar, the Tabrow and the Pager's combined scroll
       * */
      val animationScope = rememberCoroutineScope()
      
      
      NestedScrollingFront(
         appbar = {
            SearchTopBar()
         },
         tabRow = {
            TabRow(
               modifier = Modifier.wrapContentHeight(),
               indicator = {
                  customIndicator.invoke(it,pagerState)
               },
               divider = {
                  HorizontalSeparator(
                     color = MyColor.Inactive,
                     thickness = 2.dp
                  )
               },
               selectedTabIndex = pagerState.currentPage,
               containerColor = Theme.Sourcesets.TabRowBackground()
            ) {
               pageState.pages.forEachIndexed { index, title ->
                  val interactionSource =
                     remember { MutableInteractionSource() }
                  
                  CustomPagerTab(
                     modifier = Modifier
                        .height(40.dp)
                        .clickable(
                           onClick = {},
                           interactionSource = interactionSource,
                           indication = null
                        ),
                     unselectedContentColor = Theme.colorScheme().surfaceDim,
                     selectedContentColor = Theme.colorScheme().outline,
                     selected = pagerState.currentPage == index,
                     onClick = {
                        animationScope.launch {
                           pagerState.animateScrollToPage(index)
                        }
                     }, text = {
                        Text(
                           text = title,
                           fontSize = 16.sp,
                           modifier = Modifier.zIndex(2f)
                        )
                     }
                  )
               }
            }
         },
         pager = {
            HorizontalPager(
               state = pagerState,
               modifier = Modifier.fillMaxSize()
            ) { newPage ->
               Box(
                  modifier = Modifier.fillMaxSize()
               ) {
                  
                  pageContent.invoke(newPage)
                  
               }
            }
         },
         cursorValue = MyPrefs._prefs_songListState_NestedScrollOffset
      )
      
      
      // PAGER_END //
      
   }
   @Composable
   private fun SearchTopBar(){
      KeyboardAwareBox(
         modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .background(Theme.Sourcesets.AppbarBackground())
            .clipToBounds()
      ) {isImeVisible,imeInsets,navBarInset->
         
         val searchWord by ListFeedingVM.SortedSongs.searchWordFlow.collectAsState()
         
         val isSearchInProgress by remember {
            derivedStateOf {
               !searchWord.isBlank()
            }
         }
         
         val focusRequester = remember { FocusRequester() }
         var focusState by remember { mutableStateOf<FocusState?>(null) }
         
         val focusManager = LocalFocusManager.current
         
         var shouldBeFocused by remember { mutableStateOf(false) }
         
         val isFocused by ListFeedingVM.SortedSongs.isSearching.collectAsState()
         
         LaunchedEffect(isSearchInProgress,  focusState?.hasFocus, shouldBeFocused) {
            ListFeedingVM.SortedSongs.setSearching(isSearchInProgress || focusState?.hasFocus == true || shouldBeFocused)
         }
         
         
         LaunchedEffect(isImeVisible) {
            if(!isImeVisible) focusManager.clearFocus()
            shouldBeFocused = false
         }
         
         val iconModifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 10.dp)
            .aspectRatio(1f, true)
         
         if(!isFocused) {
            Text(
               text = stringResource(R.string.app_name),
               fontSize = 25.sp,
               modifier = Modifier
                  .align(Alignment.CenterStart)
                  .padding(horizontal = 5.dp)
            )
            
            IconButton(modifier = iconModifier.align(Alignment.CenterEnd),
               imageVector = Icons.Default.Search,
               onclick = {
                  shouldBeFocused = true
                  
               }
            )
         }
         else {
            
            LaunchedEffect(isFocused) {
               focusRequester.requestFocus()
            }
            CustomTextField(textGravity = Gravity.START or Gravity.CENTER,
               singleLine = true,
               contentPadding = PaddingValues(start = 10.dp),
               text = searchWord,
               onValueChange = {
                  ListFeedingVM.SortedSongs.setSearchWord(it.toString())
               },
               modifier = Modifier
                  .fillMaxSize()
                  .focusRequester(focusRequester)
                  .onFocusChanged {
                     focusState = it
                  },
               hint = "Search",
               trailingIcon = {
                  IconButton(
                     modifier = iconModifier,
                     imageVector = Icons.Default.Close,
                     onclick = {
                        focusManager.clearFocus()
                        ListFeedingVM.SortedSongs.setSearchWord("")
                        shouldBeFocused = false
                     }
                  )
               }
            )
            
         }
         
      }
   }
   
   private class MainPagerState(){
      val pages = listOf(
         "Songs",
         "Playlists"
      )
      
      var initialPage by PersistentState("mainPagerInitialPage",{ 0 })
   }
}

