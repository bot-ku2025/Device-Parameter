package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CheckStatus
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusFailBg
import com.example.ui.theme.StatusPass
import com.example.ui.theme.StatusPassBg
import com.example.ui.theme.StatusWarn
import com.example.ui.theme.StatusWarnBg

@Composable
fun StatusPill(
    status: CheckStatus,
    modifier: Modifier = Modifier,
    customLabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    val (bgColor, textColor, label) = when (status) {
        CheckStatus.PASS -> Triple(StatusPassBg, StatusPass, customLabel ?: "LOLOS")
        CheckStatus.WARN -> Triple(StatusWarnBg, StatusWarn, customLabel ?: "PERINGATAN")
        CheckStatus.FAIL -> Triple(StatusFailBg, StatusFail, customLabel ?: "TERDETEKSI")
        CheckStatus.PENDING -> Triple(Color(0x2064748B), com.example.ui.theme.TextMuted, customLabel ?: "MENUNGGU AUDIT")
    }

    val baseModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .background(bgColor)
            .border(0.8.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    } else {
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(0.8.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = baseModifier
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
