package TestLayouts

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.musichub.ui.theme.MyColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DragNFlingTest() {
    Surface(modifier = Modifier
        .fillMaxSize()
        .background(MyColor.Black)
        .safeContentPadding()
    ) {

        var parentRect by remember { mutableStateOf(Rect(0f,0f,0f,0f)) }

        Box(modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                parentRect = it.boundsInParent()
            }
        ) {
            var myYoffset by remember { mutableFloatStateOf(0f) }
            var myXoffset by remember { mutableFloatStateOf(0f) }

            Box(modifier = Modifier
                .height(with(LocalDensity.current) { parentRect.height.toDp() })
                .width(5.dp)
                .offset(0.dp, 0.dp)
                .background(MyColor.Red))//left
            Box(modifier = Modifier
                .height(with(LocalDensity.current) { parentRect.height.toDp() })
                .width(5.dp)
                .offset(
                    with(LocalDensity.current) { parentRect.right.toDp() - 5.dp },
                    0.dp
                )
                .background(MyColor.Red))//right
            Box(modifier = Modifier
                .height(5.dp)
                .width(with(LocalDensity.current) { parentRect.width.toDp() })
                .offset(0.dp, 0.dp)
                .background(MyColor.Red))//top
            Box(modifier = Modifier
                .height(5.dp)
                .width(with(LocalDensity.current) { parentRect.width.toDp() })
                .offset(
                    0.dp,
                    with(LocalDensity.current) { parentRect.bottom.toDp() - 5.dp })
                .background(MyColor.Red))//bottom

            val localDensity = LocalDensity.current
            fun applyBounds(x:Float,y:Float,rect:Rect):Offset{

                return Offset(x.coerceIn(0f,with(localDensity){parentRect.right-rect.width}),y.coerceIn(0f,with(localDensity){parentRect.bottom-rect.height}))
            }

            fun applyBounds(x: Float,y: Float,offset: Offset,rect:Rect): Pair<Float, Float> {

                val newx = (x+offset.x).coerceIn(0f,with(localDensity){parentRect.right-rect.width})
                val newy=(y+offset.y).coerceIn(0f,with(localDensity){parentRect.bottom-rect.height})

                return newx to newy
            }
            var myjob by remember { mutableStateOf<Job?>(null) }

            val scope = rememberCoroutineScope()
            var mainboxRect by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset {
                        IntOffset(
                            myXoffset.roundToInt(),
                            myYoffset.roundToInt()
                        )
                    }
                    .background(MyColor.SeparatorGray)
                    .pointerInput(Unit) {

                        var velocityX = 0f
                        var velocityY = 0f
                        var lastPosition = Offset.Zero
                        var lastTime = 0L


                        detectDragGestures(
                            onDragStart = { offset ->
                                myjob?.cancel()
                                lastPosition = Offset(myXoffset, myYoffset)
                                lastTime = System.currentTimeMillis()
                            },


                            onDrag = { change, offset ->
                                change.consume()

                                val currentTime = System.currentTimeMillis()
                                val timeDiff =
                                    (currentTime - lastTime).coerceAtLeast(1L)


                                val a = applyBounds(
                                    x = myXoffset,
                                    y = myYoffset,
                                    offset = offset,
                                    rect = mainboxRect
                                )
                                myXoffset = a.first
                                myYoffset = a.second

                                // calculate velocity
                                velocityX = ((myXoffset - lastPosition.x) / timeDiff) * 1000f
                                velocityY = ((myYoffset - lastPosition.y) / timeDiff) * 1000f


                                lastPosition = Offset(myXoffset, myYoffset)
                                lastTime = currentTime
                            },
                            onDragEnd = {
                                myjob?.cancel()
                                myjob = scope.launch {
                                    val boundedPos = applyBounds(myXoffset, myYoffset,mainboxRect)
                                    var currentX = boundedPos.x
                                    var currentY = boundedPos.y
                                    var velX = velocityX
                                    var velY = velocityY

                                    val friction = 0.985f

                                    // Animate until velocity is negligible
                                    while (abs(velX) > 1f || abs(velY) > 1f) {
                                        // Update position based on velocity
                                        currentX += velX * 0.016f
                                        currentY += velY * 0.016f

                                        // Apply friction (decay)
                                        velX *= friction
                                        velY *= friction

                                        // Apply bounds and handle bouncing if needed
                                        val bounded = applyBounds(currentX, currentY,mainboxRect)

                                        // Simple bounce effect when hitting boundaries
                                        if (bounded.x != currentX) velX = 0f // bounce with energy loss
                                        if (bounded.y != currentY) velY = 0f

                                        currentX = bounded.x
                                        currentY = bounded.y

                                        // Update UI
                                        myXoffset = currentX
                                        myYoffset = currentY

                                        delay(16L) // ~60fps
                                    }
                                }
                            }
                        )
                    }
                    .onGloballyPositioned {
                        mainboxRect = it.boundsInParent()
                    }
            )

            Box(modifier = Modifier
                .size(50.dp)
                .offset {
                    IntOffset(
                        (myXoffset + mainboxRect.width).roundToInt(),
                        (myYoffset + mainboxRect.height).roundToInt()
                    )
                }
                .background(MyColor.Red)
            )
        }


    }
}