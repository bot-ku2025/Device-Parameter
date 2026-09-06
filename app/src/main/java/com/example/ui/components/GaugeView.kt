package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SentinelBlue
import com.example.ui.theme.SentinelCyan
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusPass
import com.example.ui.theme.StatusWarn
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun CircularGauge(
    value: Float, // 0 to 100
    maxValue: Float = 100f,
    label: String,
    size: Dp = 130.dp,
    strokeWidth: Dp = 10.dp,
    isScoreMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val progress = (value / maxValue).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "GaugeAnimation"
    )

    val primaryColor = if (isScoreMode) {
        when {
            value >= 85 -> StatusPass
            value >= 60 -> StatusWarn
            else -> StatusFail
        }
    } else {
        when {
            value < 65 -> SentinelCyan
            value < 85 -> StatusWarn
            else -> StatusFail
        }
    }

    val secondaryColor = if (isScoreMode) {
        when {
            value >= 85 -> Color(0xFF059669)
            value >= 60 -> Color(0xFFD97706)
            else -> Color(0xFFDC2626)
        }
    } else {
        SentinelBlue
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()

            // Background track
            drawArc(
                color = Color(0xFF162338),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(primaryColor, secondaryColor)
                    ),
                    startAngle = 135f,
                    sweepAngle = 270f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${value.toInt()}${if (!isScoreMode) "%" else ""}",
                color = TextPrimary,
                fontSize = if (isScoreMode) 28.sp else 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
