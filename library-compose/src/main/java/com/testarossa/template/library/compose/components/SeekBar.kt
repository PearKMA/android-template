package com.testarossa.template.library.compose.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun SeekBar(
    progress: Long,
    max: Long,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onSeek: (progress: Long) -> Unit = {},
    onSeekStarted: (startedProgress: Long) -> Unit = {},
    onSeekStopped: (stoppedProgress: Long) -> Unit = {},
    activeTrackColor: Color = MaterialTheme.colorScheme.primary,
    inActiveTrackColor: Color = Color.White.copy(alpha = 0.6f),
    @DrawableRes thumb: Int? = null,
    colorThumb: Color = MaterialTheme.colorScheme.primary
) {
    // if there is an ongoing drag, only dragging progress is evaluated.
    // when dragging finishes, given [progress] continues to be used.
    var onGoingDrag by remember { mutableStateOf(false) }
    val indicatorSize = 12.dp

    BoxWithConstraints(
        modifier = modifier.padding(start = 10.dp, end = 10.dp)
    ) {
        if (progress >= max) return@BoxWithConstraints

        val boxWidth = constraints.maxWidth.toFloat()

        val percentage = remember(progress, max) {
            progress.coerceAtMost(max).toFloat() / max.toFloat()
        }

        val indicatorOffsetByPercentage = remember(percentage) {
            Offset(percentage * boxWidth, 0f)
        }

        // Indicator should be at "percentage" but dragging can change that.
        // This state keeps track of current dragging position.
        var indicatorOffsetByDragState by remember { mutableStateOf(Offset.Zero) }

        val finalIndicatorOffset = remember(
            indicatorOffsetByDragState,
            indicatorOffsetByPercentage,
            onGoingDrag
        ) {
            val finalIndicatorPosition = if (onGoingDrag) {
                indicatorOffsetByDragState
            } else {
                indicatorOffsetByPercentage
            }
            finalIndicatorPosition.copy(x = finalIndicatorPosition.x.coerceIn(0f, boxWidth))
        }

        Column {
            Box(
                modifier = Modifier
                    .height(indicatorSize)
            ) {
                // MAIN PROGRESS
                LinearProgressIndicator(
                    progress = percentage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.Center),
                    color = activeTrackColor,
                    trackColor = inActiveTrackColor
                )


                // SEEK INDICATOR
                if (enabled) {
                    val (offsetDpX, offsetDpY) = with(LocalDensity.current) {
                        (finalIndicatorOffset.x).toDp() - indicatorSize / 2 to (finalIndicatorOffset.y).toDp()
                    }

                    val draggableState = rememberDraggableState(onDelta = { dx ->
                        indicatorOffsetByDragState = Offset(
                            x = (indicatorOffsetByDragState.x + dx),
                            y = indicatorOffsetByDragState.y
                        )

                        val currentProgress =
                            (indicatorOffsetByDragState.x / boxWidth) * max
                        onSeek(currentProgress.toLong())
                    })

                    Row(modifier = Modifier
                        .matchParentSize()
                        .draggable(
                            state = draggableState,
                            orientation = Orientation.Horizontal,
                            startDragImmediately = true,
                            onDragStarted = { downPosition ->
                                onGoingDrag = true
                                indicatorOffsetByDragState =
                                    indicatorOffsetByDragState.copy(x = downPosition.x)
                                val newProgress =
                                    (indicatorOffsetByDragState.x / boxWidth) * max
                                onSeekStarted(newProgress.toLong())
                            },
                            onDragStopped = {
                                val newProgress =
                                    (indicatorOffsetByDragState.x / boxWidth) * max
                                onSeekStopped(newProgress.toLong())
                                indicatorOffsetByDragState = Offset.Zero
                                onGoingDrag = false
                            }
                        )
                    ) {

                        Indicator(
                            modifier = Modifier
                                .offset(x = offsetDpX, y = offsetDpY)
                                .size(indicatorSize),
                            thumb = thumb,
                            color = colorThumb
                        )
                    }
                }


            }
        }
    }
}

@Composable
fun Indicator(
    modifier: Modifier = Modifier,
    @DrawableRes thumb: Int? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    if (thumb != null) {
        Image(
            painter = painterResource(id = thumb),
            contentDescription = null,
            modifier = modifier
        )
    } else {
        Canvas(modifier = modifier) {
            val radius = size.height / 2
            drawCircle(color, radius)
        }
    }
}
