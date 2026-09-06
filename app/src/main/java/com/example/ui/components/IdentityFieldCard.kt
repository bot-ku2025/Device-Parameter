package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CheckStatus
import com.example.ui.theme.SentinelCardBorder
import com.example.ui.theme.SentinelSurface
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusPass
import com.example.ui.theme.StatusWarn
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.data.model.ParameterDiagnostic
import com.example.ui.components.StatusPill
import com.example.ui.theme.TextSecondary

@Composable
fun IdentityFieldCard(
    label: String,
    value: String,
    icon: ImageVector,
    status: CheckStatus = CheckStatus.PASS,
    isMonospace: Boolean = false,
    diagnostic: ParameterDiagnostic? = null,
    onInspect: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val statusColor = when (status) {
        CheckStatus.PASS -> StatusPass
        CheckStatus.WARN -> StatusWarn
        CheckStatus.FAIL -> StatusFail
        CheckStatus.PENDING -> TextMuted
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SentinelSurface)
            .border(
                width = if (status == CheckStatus.FAIL || status == CheckStatus.WARN) 1.2.dp else 1.dp,
                color = if (status == CheckStatus.FAIL) StatusFail.copy(alpha = 0.7f)
                else if (status == CheckStatus.WARN) StatusWarn.copy(alpha = 0.6f)
                else SentinelCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                if (onInspect != null) {
                    onInspect()
                } else {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(label, value)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "$label disalin ke clipboard", Toast.LENGTH_SHORT).show()
                }
            }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(
                        status = status,
                        onClick = onInspect
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(label, value)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "$label disalin ke clipboard", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy $label",
                            tint = TextMuted.copy(alpha = 0.8f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = value.ifEmpty { "<Tidak Tersedia>" },
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
