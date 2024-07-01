package com.techcravers.cropingimage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun DraggableResizableBox(
    modifier: Modifier = Modifier,
    initialStartX: Float,
    initialStartY: Float,
    initialEndX: Float,
    initialEndY: Float,
    onDragEnd: (Float, Float, Float, Float) -> Unit
) {
    var startX by remember { mutableStateOf(initialStartX) }
    var startY by remember { mutableStateOf(initialStartY) }
    var endX by remember { mutableStateOf(initialEndX) }
    var endY by remember { mutableStateOf(initialEndY) }

    val handleSize = 20.dp
    val handleColor = Color.Red

    Box(modifier = modifier) {
        // Draw the bounding box
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(startX, startY),
                size = Size(endX - startX, endY - startY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(2f)
            )
        }

        // Top-left handle
        Box(
            modifier = Modifier
                .offset { IntOffset(startX.toInt(), startY.toInt()) }
                .size(handleSize)
                .background(handleColor)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        startX = (startX + dragAmount.x).coerceIn(0f, endX - handleSize.toPx())
                        startY = (startY + dragAmount.y).coerceIn(0f, endY - handleSize.toPx())
                        onDragEnd(startX, startY, endX, endY)
                    }
                }
        )

        // Top-right handle
        Box(
            modifier = Modifier
                .offset { IntOffset(endX.toInt() - handleSize.toPx().toInt(), startY.toInt()) }
                .size(handleSize)
                .background(handleColor)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        endX = (endX + dragAmount.x).coerceIn(startX + handleSize.toPx(), Float.MAX_VALUE)
                        startY = (startY + dragAmount.y).coerceIn(0f, endY - handleSize.toPx())
                        onDragEnd(startX, startY, endX, endY)
                    }
                }
        )

        // Bottom-left handle
        Box(
            modifier = Modifier
                .offset { IntOffset(startX.toInt(), endY.toInt() - handleSize.toPx().toInt()) }
                .size(handleSize)
                .background(handleColor)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        startX = (startX + dragAmount.x).coerceIn(0f, endX - handleSize.toPx())
                        endY = (endY + dragAmount.y).coerceIn(startY + handleSize.toPx(), Float.MAX_VALUE)
                        onDragEnd(startX, startY, endX, endY)
                    }
                }
        )

        // Bottom-right handle
        Box(
            modifier = Modifier
                .offset { IntOffset(endX.toInt() - handleSize.toPx().toInt(), endY.toInt() - handleSize.toPx().toInt()) }
                .size(handleSize)
                .background(handleColor)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        endX = (endX + dragAmount.x).coerceIn(startX + handleSize.toPx(), Float.MAX_VALUE)
                        endY = (endY + dragAmount.y).coerceIn(startY + handleSize.toPx(), Float.MAX_VALUE)
                        onDragEnd(startX, startY, endX, endY)
                    }
                }
        )
    }
}
