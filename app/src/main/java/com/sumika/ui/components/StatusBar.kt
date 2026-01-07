package com.sumika.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sumika.ui.theme.CornerRadius
import com.sumika.ui.theme.IconSize
import com.sumika.ui.theme.Spacing
import com.sumika.ui.theme.SumikaTheme

/**
 * Status Bar - 状態メーター（空腹/元気/きげん等）
 * アイコン＋ラベル＋ゲージで美しく表示
 */
@Composable
fun StatusBar(
    icon: ImageVector,
    label: String,
    value: Float, // 0.0 - 1.0
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        // アイコン
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(IconSize.md)
        )
        
        // ラベル
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.width(56.dp)
        )
        
        // プログレスバー
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(CornerRadius.xs))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value.coerceIn(0f, 1f))
                    .background(color)
            )
        }
        
        // パーセント表示
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.width(40.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusBarPreview() {
    SumikaTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            StatusBar(
                icon = Icons.Default.CheckCircle,
                label = "元気",
                value = 0.8f,
                color = Color(0xFFEF4444)
            )
            StatusBar(
                icon = Icons.Default.Star,
                label = "空腹",
                value = 0.4f,
                color = Color(0xFFF59E0B)
            )
            StatusBar(
                icon = Icons.Default.Info,
                label = "きげん",
                value = 0.95f,
                color = Color(0xFF10B981)
            )
        }
    }
}
