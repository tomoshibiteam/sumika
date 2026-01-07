package com.sumika.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumika.ui.components.ConfettiAnimation
import com.sumika.ui.components.FloatingParticles
import com.sumika.ui.theme.*
import com.sumika.ui.viewmodel.FocusTimerViewModel
import kotlinx.coroutines.delay

@Composable
fun FocusTimerScreen(
    viewModel: FocusTimerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    
    // タイマー時間選択
    var selectedDuration by remember { mutableIntStateOf(25) }
    val timerOptions = listOf(15, 25, 45, 60)
    
    // 完了祝いアニメーション
    var showCelebration by remember { mutableStateOf(false) }
    LaunchedEffect(state.isRunning, state.remainingMs) {
        if (!state.isRunning && state.remainingMs == 0L && state.totalMs > 0) {
            showCelebration = true
            delay(3000)
            showCelebration = false
        }
    }
    
    // 通知権限リクエスト
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not */ }
    
    LaunchedEffect(Unit) {
        viewModel.bindService()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.unbindService()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (state.isRunning) {
                        listOf(
                            GradientStart.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.background,
                            GradientEnd.copy(alpha = 0.1f)
                        )
                    } else {
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    }
                )
            )
    ) {
        // 背景パーティクル（集中中のみ）
        if (state.isRunning) {
            FloatingParticles(
                modifier = Modifier.fillMaxSize(),
                particleCount = 15,
                baseColor = GradientAccent
            )
        }
        
        // 紙吹雪（完了時）
        ConfettiAnimation(
            isPlaying = showCelebration,
            modifier = Modifier.fillMaxSize()
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ヘッダー
            Text(
                text = if (state.isRunning) "🧘 集中中..." else "🎯 集中モード",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (state.isRunning) "素晴らしい！その調子！" else "ペットと一緒に集中しよう",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.weight(0.15f))
            
            // タイマー表示
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(300.dp)
            ) {
                // プログレスリング
                GradientCircularProgress(
                    state = state,
                    modifier = Modifier.fillMaxSize()
                )
                
                // 中央のコンテンツ
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val minutes = (state.remainingMs / 1000 / 60).toInt()
                    val seconds = ((state.remainingMs / 1000) % 60).toInt()
                    
                    // 呼吸するペット
                    if (state.isRunning) {
                        BreathingPet()
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        fontSize = if (state.isRunning) 48.sp else 56.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    if (showCelebration) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🎉 完了！",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(0.1f))
            
            // タイマー時間選択（停止中のみ）
            if (!state.isRunning) {
                Text(
                    text = "集中時間を選ぶ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    timerOptions.forEach { minutes ->
                        DurationChip(
                            minutes = minutes,
                            isSelected = selectedDuration == minutes,
                            onClick = { selectedDuration = minutes }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // コントロールボタン
            if (state.isRunning) {
                // 停止ボタン
                FilledTonalIconButton(
                    onClick = { viewModel.stopTimer() },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "停止",
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                // 開始ボタン
                Button(
                    onClick = { viewModel.startTimer(selectedDuration) },
                    modifier = Modifier
                        .height(64.dp)
                        .widthIn(min = 220.dp)
                        .shadow(8.dp, RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    )
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "集中を始める",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(0.15f))
            
            // 統計カード
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniStatItem(
                        emoji = "📅",
                        value = "${state.sessionsToday}回",
                        label = "今日"
                    )
                    MiniStatItem(
                        emoji = "⏱️",
                        value = "${state.totalFocusMinutes}分",
                        label = "累計"
                    )
                    MiniStatItem(
                        emoji = "⭐",
                        value = "${state.sessionsToday * 100}",
                        label = "獲得XP"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ヒント
            Text(
                text = "💡 集中を完了するとペットが成長します",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DurationChip(
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) GradientStart.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) GradientStart else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${minutes}分",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) GradientStart 
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BreathingPet() {
    val infiniteTransition = rememberInfiniteTransition(label = "petBreathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "petScale"
    )
    
    Text(
        text = "🐱",
        fontSize = 40.sp,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}

@Composable
private fun GradientCircularProgress(
    state: com.sumika.ui.viewmodel.FocusTimerState,
    modifier: Modifier = Modifier
) {
    val progress = if (state.totalMs > 0) {
        state.remainingMs.toFloat() / state.totalMs.toFloat()
    } else 1f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progress"
    )
    
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(modifier = modifier) {
        val strokeWidth = 16.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        // トラック
        drawCircle(
            color = trackColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        
        // グラデーションプログレス
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(GradientStart, GradientEnd, GradientStart)
            ),
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun MiniStatItem(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
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
