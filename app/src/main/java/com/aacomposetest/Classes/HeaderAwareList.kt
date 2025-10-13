package com.aacomposetest.Classes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned

@Stable
class HeaderAwareListState(
    val lazyListState: LazyListState,
    val headerIndex: Int? = null
) {
    internal val _containerRect: MutableState<Rect?> = mutableStateOf(null)
    internal val _headerRect: MutableState<Rect?> = mutableStateOf(null)

    val containerRect: Rect? get() = _containerRect.value
    val headerRect: Rect? get() = _headerRect.value

    val containerTop: Float get() = _containerRect.value?.top ?: 0f
    val containerBottom: Float get() = _containerRect.value?.bottom ?: Float.MAX_VALUE

    val headerTop: Float get() = _headerRect.value?.top ?: 0f
    val headerBottom: Float get() = _headerRect.value?.bottom ?: 0f

    fun updateContainer(rect: Rect) { _containerRect.value = rect }
    fun updateHeader(rect: Rect) { _headerRect.value = rect }

    val indexOffsetFromHeader: Int?
        get(){
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return null

            if (headerIndex == null) return 0

            val beforeHeader = (headerIndex - 1).coerceIn(0, headerIndex)
            val itemEnteredInHeader = if (
                (visibleItems.getOrNull(beforeHeader + 1)
                    ?.let { it.offset + it.size } ?: 0) <= headerBottom
            ) 1 else 0

            val index = if (headerTop <= containerTop)
                beforeHeader + 1 + itemEnteredInHeader
            else
                headerIndex + 1

            return index
        }

    /** Info for the first content item below the header (if any) */
    val firstContentItemInfo: LazyListItemInfo?
        get() {
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return null

            if (headerIndex == null) return visibleItems.first()



            return visibleItems.getOrNull(indexOffsetFromHeader?:-1)
        }



    val firstContentItemIndex: Int
        get() = firstContentItemInfo?.index ?: 0

    val firstContentItemTop: Int
        get() = firstContentItemInfo?.offset ?: 0

    val firstContentItemBottom: Int
        get() = (firstContentItemInfo?.offset ?: 0) + (firstContentItemInfo?.size ?: 0)

    fun getVisibleIndexClicked(indexInDataList: Int):Int{
        return indexInDataList-(firstContentItemIndex)+(indexOffsetFromHeader?:0)+(if(headerIndex!=null)headerIndex+1 else 0)
    }

    fun getItemsAroundIndexClicked(indexInDataList:Int):Pair<LazyListItemInfo?,LazyListItemInfo?>{
        val a = getVisibleIndexClicked(indexInDataList-1).takeUnless { it<0 || it>lazyListState.layoutInfo.visibleItemsInfo.lastIndex }
        val b = getVisibleIndexClicked(indexInDataList+1).takeUnless { it<0 || it>lazyListState.layoutInfo.visibleItemsInfo.lastIndex }

        val c = lazyListState.layoutInfo.visibleItemsInfo.getOrNull(a?:-1)
        val d = lazyListState.layoutInfo.visibleItemsInfo.getOrNull(b?:-1)

        return (c to d)
    }

    fun getItemIndexAtOffset( clickY: Float): Int? {
        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
        return visibleItems
            .firstOrNull { itemInfo ->
                val top = itemInfo.offset.toFloat()
                val bottom = top + itemInfo.size.toFloat()
                clickY in top..bottom
            }?.index
    }
}

/** Factory function to remember the ReorderableListState in Compose */
@Composable
fun rememberHeaderAwareListState(
    lazyListState: LazyListState = rememberLazyListState(),
    headerIndex: Int? = null
): HeaderAwareListState {
    return remember(lazyListState, headerIndex) {
        HeaderAwareListState(lazyListState, headerIndex)
    }
}

@Composable
fun HeaderAwareList(
    modifier: Modifier = Modifier,
    headerAwareListState: HeaderAwareListState,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: LazyListScope.()-> Unit = {}
){

    Box(modifier = modifier
        .onGloballyPositioned{
            headerAwareListState.updateContainer(it.boundsInParent())
        }
    ){
        LazyColumn(
            state = headerAwareListState.lazyListState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }

}
