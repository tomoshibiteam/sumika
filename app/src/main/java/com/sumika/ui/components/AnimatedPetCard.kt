package com.sumika.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.sumika.core.model.GrowthStage
import com.sumika.core.model.PetType
import com.sumika.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * アニメーション付きペットカード
 */
@Composable
fun AnimatedPetCard(
    petType: PetType,
    petVariation: Int,
    petName: String,
    growthStage: GrowthStage,
    growthXp: Int,
    xpToNextStage: Int,
    onPetTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 行動カウンター（変化を検知するため）
    var actionTick by remember { mutableIntStateOf(0) }
    
    // 派生した行動状態
    val behaviorIndex = actionTick % 6
    val statusText = when (behaviorIndex) {
        0 -> "・・・"
        1 -> "🚶 おさんぽ中..."
        2 -> "🪑 おすわり"
        3 -> "💤 zzz..."
        4 -> "🎾 あそんでる！"
        else -> "👀 きょろきょろ"
    }
    
    // 自律行動タイマー
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(4000L) // 4秒ごと
            actionTick++
        }
    }
    
    // 現在の行動に基づくアニメーション値
    val positionX = remember(actionTick) {
        when (behaviorIndex) {
            1 -> Random.nextFloat() * 50f - 25f // おさんぽ
            4 -> Random.nextFloat() * 30f - 15f // あそぶ
            else -> 0f
        }
    }
    
    val rotation = remember(actionTick) {
        when (behaviorIndex) {
            3 -> 12f // 寝る
            5 -> Random.nextFloat() * 20f - 10f // きょろきょろ
            else -> 0f
        }
    }
    
    val scale = when (behaviorIndex) {
        2 -> 0.95f // すわる
        3 -> 0.85f // 寝る
        4 -> 1.12f // あそぶ
        else -> 1f
    }
    
    val isJumping = behaviorIndex == 4
    
    // アニメーション
    val animatedX by animateFloatAsState(
        targetValue = positionX,
        animationSpec = tween(600),
        label = "x"
    )
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(400),
        label = "rot"
    )
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "scale"
    )
    
    // 呼吸アニメーション
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breathOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathOffset"
    )
    
    // ジャンプ
    val jumpOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jump"
    )
    
    // まばたき
    var blink by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(Random.nextLong(2500, 4500))
            blink = true
            delay(120)
            blink = false
        }
    }
    
    // タップハート
    var showHeart by remember { mutableStateOf(false) }
    val heartScale by animateFloatAsState(
        targetValue = if (showHeart) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.4f),
        label = "heart"
    )
    LaunchedEffect(showHeart) {
        if (showHeart) {
            delay(500)
            showHeart = false
        }
    }
    
    // カード
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(GlassSurface, GlassSurfaceDark)
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(0.3f), Color.White.copy(0.1f))
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showHeart = true
                onPetTap()
            }
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // ペット
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .graphicsLayer {
                        translationX = animatedX
                        translationY = -breathOffset - (if (isJumping) jumpOffset else 0f)
                        rotationZ = animatedRotation
                        scaleX = animatedScale
                        scaleY = animatedScale
                    },
                contentAlignment = Alignment.Center
            ) {
                // 背景グロー
                Box(
                    Modifier
                        .size(130.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    GradientStart.copy(0.3f),
                                    GradientEnd.copy(0.15f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                
                // 絵文字
                Text(
                    text = getEmoji(petType, behaviorIndex, blink),
                    fontSize = when (growthStage) {
                        GrowthStage.BABY -> 52.sp
                        GrowthStage.TEEN -> 64.sp
                        GrowthStage.ADULT -> 76.sp
                    }
                )
                
                // ハート
                if (heartScale > 0f) {
                    Text(
                        "❤️",
                        fontSize = 28.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .graphicsLayer {
                                scaleX = heartScale
                                scaleY = heartScale
                            }
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // ステータス
            Text(
                statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
            
            Spacer(Modifier.height(4.dp))
            
            // 名前
            Text(
                petName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(4.dp))
            
            // バッジ
            GrowthBadge(growthStage)
            
            Spacer(Modifier.height(16.dp))
            
            // XP
            if (growthStage != GrowthStage.ADULT) {
                XpBar(growthXp, xpToNextStage)
            } else {
                Text(
                    "✨ 最大成長！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Success,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun GrowthBadge(stage: GrowthStage) {
    val (emoji, label, color) = when (stage) {
        GrowthStage.BABY -> Triple("🍼", "赤ちゃん", GradientAccent)
        GrowthStage.TEEN -> Triple("🌟", "こども", GradientStart)
        GrowthStage.ADULT -> Triple("👑", "おとな", Warning)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(0.15f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(emoji, fontSize = 13.sp)
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun XpBar(current: Int, max: Int) {
    val progress = (current.toFloat() / max).coerceIn(0f, 1f)
    val animProgress by animateFloatAsState(progress, tween(400), label = "xp")
    
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("成長XP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Text("$current / $max", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(0.1f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animProgress)
                    .background(
                        Brush.horizontalGradient(listOf(GradientStart, GradientEnd)),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

private fun getEmoji(type: PetType, behavior: Int, blink: Boolean): String {
    return when {
        behavior == 3 -> "😴" // 寝てる
        behavior == 4 || blink -> when (type) {
            PetType.CAT -> "😸"
            PetType.DOG -> "🐶"
            PetType.BIRD -> "🐤"
        }
        else -> when (type) {
            PetType.CAT -> "🐱"
            PetType.DOG -> "🐕"
            PetType.BIRD -> "🐦"
        }
    }
}
