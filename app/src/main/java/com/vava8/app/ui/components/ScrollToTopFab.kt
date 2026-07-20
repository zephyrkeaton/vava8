package com.vava8.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.vava8.app.Vava8App
import com.vava8.app.ui.theme.BrandBlue
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 列表右侧浮动「返回顶部」按钮，可上下拖动；
 * 位置写入 SharedPreferences，跨页面 / 杀进程后仍保留。
 */
@Composable
fun BoxScope.ScrollToTopFab(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val prefs = Vava8App.instance.preferences
    val touchSlop = LocalViewConfiguration.current.touchSlop
    val visible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 96
        }
    }
    var offsetY by remember {
        mutableFloatStateOf(prefs.loadScrollToTopOffset())
    }
    var maxOffset by remember { mutableFloatStateOf(0f) }
    var dragDistance by remember { mutableFloatStateOf(0f) }

    fun persistOffset(value: Float) {
        offsetY = value
        prefs.saveScrollToTopOffset(value)
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier
            .align(Alignment.CenterEnd)
            .padding(end = 12.dp)
            .onGloballyPositioned { coords ->
                val parent = coords.parentLayoutCoordinates ?: return@onGloballyPositioned
                val parentH = parent.size.height.toFloat()
                val fabH = coords.size.height.toFloat()
                val half = ((parentH - fabH) / 2f).coerceAtLeast(0f)
                maxOffset = half
                val clamped = offsetY.coerceIn(-half, half)
                if (clamped != offsetY) {
                    persistOffset(clamped)
                }
            }
            .offset { IntOffset(0, offsetY.roundToInt()) },
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        FloatingActionButton(
            onClick = {
                if (abs(dragDistance) < touchSlop) {
                    scope.launch { listState.animateScrollToItem(0) }
                }
                dragDistance = 0f
            },
            modifier = Modifier
                .size(44.dp)
                .pointerInput(maxOffset) {
                    detectVerticalDragGestures(
                        onDragStart = { dragDistance = 0f },
                        onDragEnd = {
                            persistOffset(offsetY)
                            // 保留 dragDistance 供 onClick 判断是否为轻点
                        },
                        onDragCancel = {
                            persistOffset(offsetY)
                            dragDistance = 0f
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            dragDistance += dragAmount
                            val limit = maxOffset.coerceAtLeast(0f)
                            offsetY = (offsetY + dragAmount).coerceIn(-limit, limit)
                        }
                    )
                },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BrandBlue,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "返回顶部")
        }
    }
}
