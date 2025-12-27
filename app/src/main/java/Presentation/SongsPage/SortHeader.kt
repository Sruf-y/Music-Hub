package Presentation.SongsPage

import AppDatabase.SongRelated.Song
import SrufComposeComponents.IconButton
import SrufComposeComponents.IconButtonDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aacomposetest.R

/**
 * Header for sorting items
 * IF onFilterClick == null then doesn't show the filter button at all.
 * */
@Composable
internal fun SortHeader(
    songs: List<Song>,
    onFilterClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(), contentAlignment = Alignment.CenterStart
    ) {

        Text(text = "Songs(${songs.size})")

        if (onFilterClick != null) {
            IconButton(
                resId = R.drawable.ic_sort,
                modifier = IconButtonDefaults.modifier.align(Alignment.CenterEnd)
            ) {
                onFilterClick.invoke()
            }

        }
    }
}