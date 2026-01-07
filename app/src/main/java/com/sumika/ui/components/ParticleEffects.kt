package com.sumika.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sumika.ui.theme.*
import kotlin.random.Random

/**
 * 紙吹雪アニメーション
 * タイマー完了時のお祝い演出
 */
@Composable
fun ConfettiAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isPlaying) return
    
    val confettiColors = listOf(
        GradientStart,
        GradientEnd,
        GradientAccent,
        Warning,
        Success,
        Heart
    )
    
    // 紙吹雪パーティクル
    val particles = remember {
        List(50) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                size = Random.nextFloat() * 8f + 4f,
                color = confettiColors.random(),
                speedX = Random.nextFloat() * 0.4f - 0.2f,
                speedY = Random.nextFloat() * 0.3f + 0.2f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 10f - 5f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiTime"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val x = (particle.x + particle.speedX * time) % 1f * size.width
            val y = (particle.y + particle.speedY * time * 2f) % 1.5f * size.height
            
            if (y > 0 && y < size.height) {
                drawCircle(
                    color = particle.color,
                    radius = particle.size,
                    center = Offset(x, y)
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val speedX: Float,
    val speedY: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

/**
 * 浮遊パーティクル背景
 * 集中中のアンビエント効果
 */
@Composable
fun FloatingParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 20,
    baseColor: Color = GradientStart
) {
    val particles = remember {
        List(particleCount) {
            FloatingParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 2f,
                alpha = Random.nextFloat() * 0.3f + 0.1f,
                speedY = Random.nextFloat() * 0.05f + 0.02f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floatingTime"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val y = (particle.y - particle.speedY * time) % 1f
            val adjustedY = if (y < 0) y + 1f else y
            
            drawCircle(
                color = baseColor.copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(particle.x * size.width, adjustedY * size.height)
            )
        }
    }
}

private data class FloatingParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val speedY: Float
)
