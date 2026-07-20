package com.vava8.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * 详情页任意位置向右滑动返回（类似微信 / 常见阅读 App）。
 *
 * 挂在内容容器上，用 [PointerEventPass.Initial] 做方向锁定：
 * - 确认是「向右横滑」后才消费事件并带动画退出
 * - 纵滑 / 点击不消费，交给 LazyColumn、WebView、按钮处理
 */
@Composable
fun SwipeBackContainer(
    enabled: Boolean = true,
    onBack: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val triggerPx = with(density) { 72.dp.toPx() }
    val touchSlop = LocalViewConfiguration.current.touchSlop
    val maxDragPx = screenWidthPx
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        val progress = (offsetX / (screenWidthPx * 0.45f)).coerceIn(0f, 1f)
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f * (1f - progress))
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt().coerceAtLeast(0), 0) }
                .graphicsLayer {
                    shadowElevation = if (progress > 0.02f) 12f else 0f
                }
                .then(
                    if (enabled) {
                        Modifier.pointerInput(triggerPx, maxDragPx, touchSlop) {
                            awaitPointerEventScope {
                                while (true) {
                                    val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                                    val down = downEvent.changes.firstOrNull() ?: continue
                                    if (!down.changedToDown()) continue

                                    val tracker = VelocityTracker()
                                    tracker.addPosition(down.uptimeMillis, down.position)
                                    var totalDx = 0f
                                    var totalDy = 0f
                                    // null=未判定, true=右滑返回, false=交给子组件
                                    var lockedHorizontal: Boolean? = null
                                    var tracking = true

                                    while (tracking) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        val change = event.changes.firstOrNull() ?: break

                                        if (change.changedToUp() || !change.pressed) {
                                            tracker.addPosition(change.uptimeMillis, change.position)
                                            if (lockedHorizontal == true) {
                                                change.consume()
                                                val velocityX = tracker.calculateVelocity().x
                                                val shouldBack =
                                                    offsetX >= triggerPx || velocityX > 900f
                                                val start = offsetX
                                                val target = if (shouldBack) maxDragPx else 0f
                                                scope.launch {
                                                    val anim = Animatable(start)
                                                    anim.animateTo(
                                                        target,
                                                        animationSpec = tween(180)
                                                    ) { offsetX = value }
                                                    if (shouldBack) onBack() else offsetX = 0f
                                                }
                                            }
                                            tracking = false
                                            continue
                                        }

                                        val delta = change.positionChange()
                                        tracker.addPosition(change.uptimeMillis, change.position)

                                        when (lockedHorizontal) {
                                            null -> {
                                                totalDx += delta.x
                                                totalDy += delta.y
                                                if (hypot(totalDx, totalDy) >= touchSlop) {
                                                    val isRightSwipe =
                                                        abs(totalDx) > abs(totalDy) && totalDx > 0f
                                                    lockedHorizontal = isRightSwipe
                                                    if (isRightSwipe) {
                                                        change.consume()
                                                        offsetX = totalDx.coerceIn(0f, maxDragPx)
                                                    } else {
                                                        // 纵滑或左滑：退出监听，本轮不再拦截
                                                        tracking = false
                                                    }
                                                }
                                            }
                                            true -> {
                                                change.consume()
                                                offsetX = (offsetX + delta.x).coerceIn(0f, maxDragPx)
                                            }
                                            false -> tracking = false
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            content()
        }
    }
}
