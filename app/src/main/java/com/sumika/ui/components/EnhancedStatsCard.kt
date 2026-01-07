package com.sumika.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumika.ui.theme.*

/**
 * 改善された統計カード
 * - アイコン付きの統計表示
 * - グラスモーフィズム効果
 * - 微妙なアニメーション
 */
@Composable
fun EnhancedStatsCard(
    totalFocusMinutes: Int,
    focusSessionsCount: Int,
    totalXp: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            emoji = "⏱️",
            value = formatTime(totalFocusMinutes),
            label = "累計集中",
            accentColor = GradientAccent
        )
        
        StatDivider()
        
        StatItem(
            emoji = "🎯",
            value = "${focusSessionsCount}回",
            label = "セッション",
            accentColor = GradientStart
        )
        
        StatDivider()
        
        StatItem(
            emoji = "⭐",
            value = "$totalXp",
            label = "獲得XP",
            accentColor = Warning
        )
    }
}

@Composable
private fun StatItem(
    emoji: String,
    value: String,
    label: String,
    accentColor: Color
) {
    // 軽い浮遊アニメーション
    val infiniteTransition = rememberInfiniteTransition(label = "statFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer { translationY = -floatOffset }
    ) {
        // 絵文字と背景
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    accentColor.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(60.dp)
            .background(
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )
    )
}

private fun formatTime(minutes: Int): String {
    return when {
        minutes < 60 -> "${minutes}分"
        minutes < 1440 -> "${minutes / 60}時間"
        else -> "${minutes / 1440}日"
    }
}
