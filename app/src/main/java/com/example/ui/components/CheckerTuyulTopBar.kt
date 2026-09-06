package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.SentinelCardBorder
import com.example.ui.theme.SentinelCyan
import com.example.ui.theme.SentinelSurface
import com.example.ui.theme.StatusPass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun DeviceParameterTopBar(
    anomalyAlertsEnabled: Boolean,
    onToggleAnomalyAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF0A101D))
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(listOf(SentinelCardBorder, Color(0x3000E5FF))),
                RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Device Parameter Logo & Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF131F33))
                        .border(1.5.dp, SentinelCyan, CircleShape)
                        .padding(2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_checker_tuyul_logo),
                        contentDescription = "Logo Device Parameter",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DEVICE PARAMETER",
                            color = SentinelCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(StatusPass)
                        )
                    }

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = "Realtime Device Parameter & Integrity Auditor",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right: Instant Anomaly Notification Toggle
            IconButton(
                onClick = onToggleAnomalyAlerts,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (anomalyAlertsEnabled) Color(0x2500E5FF) else SentinelSurface)
                    .border(1.dp, if (anomalyAlertsEnabled) SentinelCyan else SentinelCardBorder, CircleShape)
                    .size(38.dp)
                    .testTag("anomaly_alert_button")
            ) {
                Icon(
                    imageVector = if (anomalyAlertsEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                    contentDescription = "Notifikasi Anomali",
                    tint = if (anomalyAlertsEnabled) SentinelCyan else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Backward-compatible alias
@Composable
fun CheckerTuyulTopBar(
    anomalyAlertsEnabled: Boolean,
    onToggleAnomalyAlerts: () -> Unit,
    modifier: Modifier = Modifier
) = DeviceParameterTopBar(anomalyAlertsEnabled, onToggleAnomalyAlerts, modifier)
