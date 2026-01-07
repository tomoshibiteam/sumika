package com.sumika.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.sumika.ui.theme.Shimmer

/**
 * シマーエフェクト
 * プレミアムペットなどに使用する光沢アニメーション
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    durationMillis: Int = 1500,
    shimmerColor: Color = Shimmer
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    
    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    shimmerColor.copy(alpha = 0.6f),
                    Color.Transparent
                ),
                start = Offset(
                    x = offset * 1000f,
                    y = offset * 1000f
                ),
                end = Offset(
                    x = (offset + 1f) * 1000f,
                    y = (offset + 1f) * 1000f
                )
            )
        )
    )
}
