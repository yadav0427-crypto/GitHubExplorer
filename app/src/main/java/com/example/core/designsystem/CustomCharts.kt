package com.example.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * A highly polished, custom-engineered Bezier curve line chart with glowing gradients.
 * Engineered using native Compose Canvas to represent Stars growth or activity.
 */
@Composable
fun GlowLineChart(
    dataPoints: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    glowColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
) {
    if (dataPoints.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(dataPoints) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = selectedIndex?.let {
                "Selected Point: ${dataPoints[it].toInt()} stars (${labels.getOrNull(it) ?: ""})"
            } ?: "Interactive View (Tap points to inspect)",
            style = MaterialTheme.typography.bodySmall,
            color = if (selectedIndex != null) MaterialTheme.colorScheme.primary else labelColor,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(dataPoints) {
                    detectTapGestures { offset ->
                        // Detect closest node
                        val width = size.width
                        val paddingLeft = 40.dp.toPx()
                        val paddingRight = 16.dp.toPx()
                        val chartWidth = width - paddingLeft - paddingRight
                        val xStep = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

                        var minDistance = Float.MAX_VALUE
                        var closestIdx = 0
                        for (i in dataPoints.indices) {
                            val nodeX = paddingLeft + i * xStep
                            val distance = kotlin.math.abs(offset.x - nodeX)
                            if (distance < minDistance) {
                                minDistance = distance
                                closestIdx = i
                            }
                        }
                        if (minDistance < xStep / 1.5f) {
                            selectedIndex = closestIdx
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            val paddingLeft = 40.dp.toPx()
            val paddingBottom = 24.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingRight = 16.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            val maxVal = (dataPoints.maxOrNull() ?: 1f).coerceAtLeast(1f)
            val minVal = (dataPoints.minOrNull() ?: 0f)
            val range = (maxVal - minVal).coerceAtLeast(1f)

            // Draw horizontal grid lines
            val gridCount = 4
            for (i in 0..gridCount) {
                val ratio = i.toFloat() / gridCount
                val y = paddingTop + chartHeight * (1f - ratio)
                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Grid label
                val gridLabel = (minVal + range * ratio).toInt().toString()
                drawText(
                    textMeasurer = textMeasurer,
                    text = gridLabel,
                    style = TextStyle(color = labelColor, fontSize = 9.sp),
                    topLeft = Offset(4.dp.toPx(), y - 6.dp.toPx())
                )
            }

            // Draw line and gradient path
            val xStep = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)
            val points = dataPoints.indices.map { i ->
                val x = paddingLeft + i * xStep
                val ratio = (dataPoints[i] - minVal) / range
                val y = paddingTop + chartHeight * (1f - ratio)
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                val strokePath = Path()
                val fillPath = Path()

                strokePath.moveTo(points[0].x, points[0].y + (height - points[0].y) * (1f - animatedProgress.value))
                fillPath.moveTo(points[0].x, height - paddingBottom)

                for (i in 1 until points.size) {
                    val pPrev = points[i - 1]
                    val pCurr = points[i]
                    
                    // Control points for Bezier Curve
                    val cp1X = pPrev.x + xStep / 2f
                    val cp1Y = pPrev.y + (height - pPrev.y) * (1f - animatedProgress.value)
                    val cp2X = pCurr.x - xStep / 2f
                    val cp2Y = pCurr.y + (height - pCurr.y) * (1f - animatedProgress.value)

                    val targetY = pCurr.y + (height - pCurr.y) * (1f - animatedProgress.value)

                    strokePath.cubicTo(
                        cp1X, cp1Y,
                        cp2X, cp2Y,
                        pCurr.x, targetY
                    )
                }

                // Create closed shape for fill
                for (i in points.indices) {
                    val p = points[i]
                    val targetY = p.y + (height - p.y) * (1f - animatedProgress.value)
                    if (i == 0) {
                        fillPath.lineTo(p.x, targetY)
                    } else {
                        val pPrev = points[i - 1]
                        val cp1X = pPrev.x + xStep / 2f
                        val cp1Y = pPrev.y + (height - pPrev.y) * (1f - animatedProgress.value)
                        val cp2X = p.x - xStep / 2f
                        val cp2Y = targetY // approximate control point

                        fillPath.cubicTo(cp1X, cp1Y, cp2X, cp2Y, p.x, targetY)
                    }
                }
                fillPath.lineTo(points.last().x, height - paddingBottom)
                fillPath.close()

                // Draw filled gradient area
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        startY = paddingTop,
                        endY = height - paddingBottom
                    )
                )

                // Draw curve outline
                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw nodes
                points.forEachIndexed { idx, point ->
                    val y = point.y + (height - point.y) * (1f - animatedProgress.value)
                    val isSelected = selectedIndex == idx
                    
                    // Ripple glowing node
                    drawCircle(
                        color = lineColor,
                        radius = if (isSelected) 8.dp.toPx() else 4.dp.toPx(),
                        center = Offset(point.x, y)
                    )
                    if (isSelected) {
                        drawCircle(
                            color = lineColor.copy(alpha = 0.4f),
                            radius = 14.dp.toPx(),
                            center = Offset(point.x, y),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            // Draw details bottom labels
            for (i in labels.indices) {
                if (i % (labels.size / 4).coerceAtLeast(1) == 0 || i == labels.lastIndex) {
                    val x = paddingLeft + i * xStep
                    val textStr = labels[i]
                    drawText(
                        textMeasurer = textMeasurer,
                        text = textStr,
                        style = TextStyle(
                            color = labelColor,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        ),
                        topLeft = Offset(x - 16.dp.toPx(), height - paddingBottom + 4.dp.toPx())
                    )
                }
            }
        }
    }
}

/**
 * Custom bar chart representing Pull Request activity or Issue breakdown.
 */
@Composable
fun CustomBarChart(
    bars: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.secondary
) {
    if (bars.isEmpty()) return

    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Activity Distribution",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val maxVal = (bars.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

            bars.forEach { bar ->
                val ratio = bar.second.toFloat() / maxVal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = bar.second.toString(),
                        style = TextStyle(fontSize = 9.sp, color = barColor, textAlign = TextAlign.Center)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight(ratio.coerceAtLeast(0.05f))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(barColor, barColor.copy(alpha = 0.5f))
                                ),
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bar.first,
                        style = TextStyle(fontSize = 10.sp, color = labelColor, textAlign = TextAlign.Center),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// Simple extension helper for Surface colors
@Composable
private fun androidx.compose.material3.ColorScheme.surfaceColorAtElevation(elevation: androidx.compose.ui.unit.Dp): Color {
    val alpha = when (elevation.value) {
        0f -> 0f
        1f -> 0.05f
        3f -> 0.08f
        6f -> 0.11f
        8f -> 0.12f
        else -> 0.14f
    }
    return primary.copy(alpha = alpha).compositeOver(surface)
}
