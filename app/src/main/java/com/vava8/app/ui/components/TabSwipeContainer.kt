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
 * 底栏 Tab 横向滑动：跟手位移 + 松手滑出动画（类似详情页右滑返回），
 * 再切换到相邻 Tab。纵滑 / 点击不拦截。
 */
@Composable
fun TabSwipeContainer(
    enabled: Boolean = true,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val touchSlop = LocalViewConfiguration.current.touchSlop
    val triggerPx = with(density) { 72.dp.toPx() }
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableFloatStateOf(0f) }

    val progress = (abs(offsetX) / (screenWidthPx * 0.45f)).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize()) {
        // 跟手滑出时用页面底色垫底，避免露出空白/遮罩闪一下
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .graphicsLayer {
                    shadowElevation = if (progress > 0.02f) 10f else 0f
                }
                .then(
                    if (enabled && (onSwipeLeft != null || onSwipeRight != null)) {
                        Modifier.pointerInput(
                            onSwipeLeft,
                            onSwipeRight,
                            triggerPx,
                            touchSlop,
                            screenWidthPx
                        ) {
                            awaitPointerEventScope {
                                while (true) {
                                    val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                                    val down = downEvent.changes.firstOrNull() ?: continue
                                    if (!down.changedToDown()) continue

                                    val tracker = VelocityTracker()
                                    tracker.addPosition(down.uptimeMillis, down.position)
                                    var totalDx = 0f
                                    var totalDy = 0f
                                    // null=未判定, true=左滑(下一页), false=右滑(上一页)
                                    var lockedLeft: Boolean? = null
                                    var tracking = true

                                    while (tracking) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        val change = event.changes.firstOrNull() ?: break

                                        if (change.changedToUp() || !change.pressed) {
                                            tracker.addPosition(change.uptimeMillis, change.position)
                                            if (lockedLeft != null) {
                                                change.consume()
                                                val velocityX = tracker.calculateVelocity().x
                                                val goNext = lockedLeft == true &&
                                                    (offsetX <= -triggerPx || velocityX < -900f) &&
                                                    onSwipeLeft != null
                                                val goPrev = lockedLeft == false &&
                                                    (offsetX >= triggerPx || velocityX > 900f) &&
                                                    onSwipeRight != null
                                                val start = offsetX
                                                when {
                                                    goNext -> {
                                                        scope.launch {
                                                            val anim = Animatable(start)
                                                            anim.animateTo(
                                                                -screenWidthPx,
                                                                animationSpec = tween(200)
                                                            ) { offsetX = value }
                                                            com.vava8.app.navigation.TabTransitionState
                                                                .suppressNextExit = true
                                                            com.vava8.app.navigation.TabTransitionState
                                                                .suppressNextEnter = true
                                                            onSwipeLeft?.invoke()
                                                        }
                                                    }
                                                    goPrev -> {
                                                        scope.launch {
                                                            val anim = Animatable(start)
                                                            anim.animateTo(
                                                                screenWidthPx,
                                                                animationSpec = tween(200)
                                                            ) { offsetX = value }
                                                            com.vava8.app.navigation.TabTransitionState
                                                                .suppressNextExit = true
                                                            com.vava8.app.navigation.TabTransitionState
                                                                .suppressNextEnter = true
                                                            onSwipeRight?.invoke()
                                                        }
                                                    }
                                                    else -> {
                                                        scope.launch {
                                                            val anim = Animatable(start)
                                                            anim.animateTo(
                                                                0f,
                                                                animationSpec = tween(180)
                                                            ) { offsetX = value }
                                                        }
                                                    }
                                                }
                                            }
                                            tracking = false
                                            continue
                                        }

                                        val delta = change.positionChange()
                                        tracker.addPosition(change.uptimeMillis, change.position)

                                        when (lockedLeft) {
                                            null -> {
                                                totalDx += delta.x
                                                totalDy += delta.y
                                                if (hypot(totalDx, totalDy) >= touchSlop) {
                                                    lockedLeft = when {
                                                        totalDx < 0f &&
                                                            abs(totalDx) > abs(totalDy) &&
                                                            onSwipeLeft != null -> true
                                                        totalDx > 0f &&
                                                            abs(totalDx) > abs(totalDy) &&
                                                            onSwipeRight != null -> false
                                                        else -> {
                                                            tracking = false
                                                            null
                                                        }
                                                    }
                                                    if (lockedLeft != null) {
                                                        change.consume()
                                                        offsetX = when (lockedLeft) {
                                                            true -> totalDx.coerceIn(-screenWidthPx, 0f)
                                                            false -> totalDx.coerceIn(0f, screenWidthPx)
                                                            null -> 0f
                                                        }
                                                    }
                                                }
                                            }
                                            true -> {
                                                change.consume()
                                                offsetX = (offsetX + delta.x)
                                                    .coerceIn(-screenWidthPx, 0f)
                                            }
                                            false -> {
                                                change.consume()
                                                offsetX = (offsetX + delta.x)
                                                    .coerceIn(0f, screenWidthPx)
                                            }
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
