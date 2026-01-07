package com.sumika.ui.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sumika.wallpaper.SumikaWallpaperService

@Composable
fun SettingsScreen(onNavigateToCalibration: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Text(
            text = "設定",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 壁紙設定セクション
        SettingsSection(title = "壁紙") {
            SettingsItem(
                title = "ライブ壁紙を設定",
                description = "ホーム画面でペットを表示",
                onClick = {
                    openWallpaperSettings(context)
                }
            )
            SettingsItem(
                title = "おうちの位置を設定",
                description = "アプリアイコンの場所を指定",
                onClick = onNavigateToCalibration
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // ペット設定セクション
        SettingsSection(title = "ペット") {
            SettingsItem(
                title = "ペットを選ぶ",
                description = "猫・犬・鳥から選択",
                onClick = { /* TODO */ }
            )
            SettingsItem(
                title = "名前を変更",
                description = "現在: モモ",
                onClick = { /* TODO */ }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 背景設定セクション
        SettingsSection(title = "背景") {
            SettingsItem(
                title = "背景画像を選ぶ",
                description = "ギャラリーから選択",
                onClick = { /* TODO */ }
            )
            SettingsItem(
                title = "ぼかし・明るさ",
                description = "アイコンを見やすく調整",
                onClick = { /* TODO */ }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // タイマー設定セクション
        SettingsSection(title = "集中タイマー") {
            SettingsItem(
                title = "タイマー時間",
                description = "25分",
                onClick = { /* TODO */ }
            )
            SettingsItem(
                title = "完了通知",
                description = "オン",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

private fun openWallpaperSettings(context: Context) {
    try {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, SumikaWallpaperService::class.java)
            )
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // フォールバック: 壁紙設定画面を開く
        val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
        context.startActivity(intent)
    }
}
