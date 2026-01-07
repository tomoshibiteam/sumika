package com.sumika.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sumika.ui.theme.*

/**
 * Pet Hero Card - ホーム画面の主役カード
 * ペット画像＋様子＋出来事＋インタラクション
 */
@Composable
fun PetHeroCard(
    petImageResId: Int,
    petName: String,
    currentMood: String, // "ねむそう", "ごきげん"
    todayEvent: String?, // "昼に窓辺でうとうとしてた"
    onPat: () -> Unit,
    onCall: () -> Unit,
    onTreat: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 呼吸アニメーション
    val infiniteTransition = rememberInfiniteTransition(label = "breathAnimation")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    
    SurfaceCard(
        modifier = modifier,
        elevation = Elevation.lg,
        cornerRadius = CornerRadius.lg
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // ペット画像（呼吸アニメーション付き）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(CornerRadius.md))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = petImageResId),
                    contentDescription = petName,
                    modifier = Modifier
                        .size(160.dp)
                        .scale(breathScale),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }
            
            // ペット名＋様子
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
            ) {
                Text(
                    text = petName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentMood,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 今日の出来事
            if (todayEvent != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerRadius.sm))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(Spacing.md)
                ) {
                    Text(
                        text = "💭 $todayEvent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // インタラクションボタン
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InteractionButton(
                    icon = Icons.Default.Info,
                    label = "なでる",
                    onClick = onPat
                )
                InteractionButton(
                    icon = Icons.Default.CheckCircle,
                    label = "呼ぶ",
                    onClick = onCall
                )
                InteractionButton(
                    icon = Icons.Default.Star,
                    label = "おやつ",
                    onClick = onTreat
                )
            }
        }
    }
}

@Composable
private fun InteractionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        modifier = Modifier
            .clip(RoundedCornerShape(CornerRadius.sm))
            .clickable(onClick = onClick)
            .padding(Spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Primary,
                modifier = Modifier.size(IconSize.md)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PetHeroCardPreview() {
    SumikaTheme {
        PetHeroCard(
            petImageResId = android.R.drawable.ic_menu_camera, // プレースホルダー
            petName = "シロ",
            currentMood = "ごきげん",
            todayEvent = "昼に窓辺でうとうとしてた",
            onPat = {},
            onCall = {},
            onTreat = {},
            modifier = Modifier.padding(Spacing.md)
        )
    }
}
