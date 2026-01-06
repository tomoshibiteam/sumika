package com.sumika.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumika.core.model.GrowthStage
import com.sumika.core.model.PetType
import com.sumika.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ヘッダー
        Text(
            text = "🏠 おうち",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // ペットプレビュー
        PetPreviewCard(
            petType = state.petType,
            petVariation = state.petVariation,
            petName = state.petName,
            growthStage = state.growthStage,
            growthXp = state.growthXp,
            xpToNextStage = state.xpToNextStage
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 統計カード
        StatsCard(
            totalFocusMinutes = state.totalFocusMinutes,
            focusSessionsCount = state.focusSessionsCount
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // ペット選択
        PetTypeSelector(
            selectedType = state.petType,
            selectedVariation = state.petVariation,
            onTypeSelected = { viewModel.setPetType(it) },
            onVariationSelected = { viewModel.setPetVariation(it) }
        )
    }
}

@Composable
private fun PetPreviewCard(
    petType: PetType,
    petVariation: Int,
    petName: String,
    growthStage: GrowthStage,
    growthXp: Int,
    xpToNextStage: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ペットアイコン
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (petType) {
                        PetType.CAT -> "🐱"
                        PetType.DOG -> "🐕"
                        PetType.BIRD -> "🐦"
                    },
                    fontSize = 64.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 名前と成長段階
            Text(
                text = petName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = when (growthStage) {
                    GrowthStage.BABY -> "👶 赤ちゃん"
                    GrowthStage.TEEN -> "🧒 こども"
                    GrowthStage.ADULT -> "🧑 おとな"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // XPプログレス
            if (growthStage != GrowthStage.ADULT) {
                val progress = growthXp.toFloat() / xpToNextStage.toFloat()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "成長XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            "$growthXp / $xpToNextStage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                }
            } else {
                Text(
                    "✨ 最大成長に到達！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    totalFocusMinutes: Int,
    focusSessionsCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn(
                emoji = "⏱️",
                value = "${totalFocusMinutes}分",
                label = "累計集中"
            )
            StatColumn(
                emoji = "🎯",
                value = "${focusSessionsCount}回",
                label = "セッション"
            )
            StatColumn(
                emoji = "🔥",
                value = "${focusSessionsCount * 4}",
                label = "獲得XP"
            )
        }
    }
}

@Composable
private fun StatColumn(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PetTypeSelector(
    selectedType: PetType,
    selectedVariation: Int,
    onTypeSelected: (PetType) -> Unit,
    onVariationSelected: (Int) -> Unit
) {
    Column {
        Text(
            text = "ペットを選ぶ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PetType.entries.forEach { type ->
                val isSelected = type == selectedType
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onTypeSelected(type) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (type) {
                            PetType.CAT -> "🐱"
                            PetType.DOG -> "🐕"
                            PetType.BIRD -> "🐦"
                        },
                        fontSize = 36.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // バリエーション選択
        Text(
            text = "カラー",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            val colors = when (selectedType) {
                PetType.CAT -> listOf(0xFF2D2D2D, 0xFFE8A87C, 0xFFF5F5F5)  // 黒, 三毛, 白
                PetType.DOG -> listOf(0xFFC4956A, 0xFF3D3D3D, 0xFFF0F0F0)  // 茶, 黒, 白
                PetType.BIRD -> listOf(0xFFFFD93D, 0xFF6EC6FF, 0xFFFAFAFA) // 黄, 青, 白
            }
            
            colors.forEachIndexed { index, color ->
                val isSelected = index == selectedVariation
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(color.toInt()))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary 
                                   else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onVariationSelected(index) }
                )
            }
        }
    }
}
