package com.sumika.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumika.ui.components.AppTopBar
import com.sumika.ui.components.PetHeroCard
import com.sumika.ui.components.StatusBar
import com.sumika.ui.components.SurfaceCard
import com.sumika.ui.theme.*
import com.sumika.ui.viewmodel.HomeViewModel

/**
 * Home Screen - 観察体験に特化した画面設計
 * ペットを眺める価値を最大化し、スカスカ問題を解消
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    
    // ペット画像のリソースIDを取得（仮のロジック）
    val petImageResId = remember(state.petType, state.petVariation) {
        // TODO: 実際のペット画像リソースIDを返すロジックを実装
        android.R.drawable.ic_menu_camera
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = Spacing.md)
            .padding(top = Spacing.lg, bottom = Spacing.md)
    ) {
        // Header
        AppTopBar(
            greeting = "おかえり！",
            subtitle = "${state.petName}が待っているよ",
            rightAction = {
                // プロフィールアイコン（小さめ）
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 20.sp)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        // Hero: Pet Hero Card
        PetHeroCard(
            petImageResId = petImageResId,
            petName = state.petName,
            currentMood = determineMood(state.growthStage),
            todayEvent = generateTodayEvent(),
            onPat = { /* TODO: ペットなでる反応 */ },
            onCall = { /* TODO: ペット呼ぶ反応 */ },
            onTreat = { /* TODO: おやつ反応 */ }
        )
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        // Status Section
        SurfaceCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = "いまの様子",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                StatusBar(
                    icon = Icons.Default.Favorite,
                    label = "元気",
                    value = 0.8f,
                    color = Error
                )
                
                StatusBar(
                    icon = Icons.Default.Star,
                    label = "空腹",
                    value = 0.4f,
                    color = Warning
                )
                
                StatusBar(
                    icon = Icons.Default.CheckCircle,
                    label = "きげん",
                    value = 0.95f,
                    color = Success
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        // Activity Log (ミニログ)
        SurfaceCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = "最近のできごと",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                repeat(3) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text(
                            text = "12:${30 + index * 15}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(48.dp)
                        )
                        Text(
                            text = getActivityLogItem(index),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        // Footer: Growth Progress (サブ情報として控えめに)
        if (state.growthStage != com.sumika.core.model.GrowthStage.ADULT) {
            SurfaceCard(
                elevation = Elevation.sm
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = "成長記録",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getStageLabel(state.growthStage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${state.growthXp} / ${state.xpToNextStage} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.md))
    }
}

// Helper Functions
private fun determineMood(stage: com.sumika.core.model.GrowthStage): String {
    return when (stage) {
        com.sumika.core.model.GrowthStage.BABY -> "すやすや"
        com.sumika.core.model.GrowthStage.TEEN -> "ごきげん"
        com.sumika.core.model.GrowthStage.ADULT -> "おだやか"
    }
}

private fun generateTodayEvent(): String {
    val events = listOf(
        "昼に窓辺でうとうとしてた",
        "お気に入りの場所でリラックス中",
        "ちょっと遊びたそう",
        "今日はとってもおとなしい",
        "なんだか落ち着かない様子"
    )
    return events.random()
}

private fun getActivityLogItem(index: Int): String {
    val activities = listOf(
        "水を飲んだ",
        "おやつを食べた",
        "お昼寝から起きた",
        "伸びをした",
        "窓の外を眺めていた",
        "ちょっと遊んだ"
    )
    return activities.getOrNull(index) ?: activities.random()
}

private fun getStageLabel(stage: com.sumika.core.model.GrowthStage): String {
    return when (stage) {
        com.sumika.core.model.GrowthStage.BABY -> "子ども"
        com.sumika.core.model.GrowthStage.TEEN -> "若者"
        com.sumika.core.model.GrowthStage.ADULT -> "大人"
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SumikaTheme {
        HomeScreen()
    }
}
