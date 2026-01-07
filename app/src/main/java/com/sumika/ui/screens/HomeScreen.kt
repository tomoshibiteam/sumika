package com.sumika.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.sumika.ui.components.AnimatedPetCard
import com.sumika.ui.components.EnhancedStatsCard
import com.sumika.ui.theme.*
import com.sumika.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientStart.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "おかえり！",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${state.petName}が待っているよ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                
                // プロフィールアイコン
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            GradientStart.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 24.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ペットカード（アニメーション付き）
            AnimatedPetCard(
                petType = state.petType,
                petVariation = state.petVariation,
                petName = state.petName,
                growthStage = state.growthStage,
                growthXp = state.growthXp,
                xpToNextStage = state.xpToNextStage,
                onPetTap = { /* TODO: ペットとのインタラクション */ }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 統計カード
            EnhancedStatsCard(
                totalFocusMinutes = state.totalFocusMinutes,
                focusSessionsCount = state.focusSessionsCount,
                totalXp = state.focusSessionsCount * 100 // 仮の計算
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ペット選択セクション
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ペットを選ぶ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                PetTypeSelector(
                    selectedType = state.petType,
                    selectedVariation = state.petVariation,
                    onTypeSelected = { viewModel.setPetType(it) },
                    onVariationSelected = { viewModel.setPetVariation(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
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
        // ペットタイプ選択
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PetType.entries.forEach { type ->
                val isSelected = type == selectedType
                PetTypeChip(
                    petType = type,
                    isSelected = isSelected,
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // カラーバリエーション
        Text(
            text = "カラー",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            val colors = when (selectedType) {
                PetType.CAT -> listOf(0xFF2D2D2D, 0xFFE8A87C, 0xFFF5F5F5)
                PetType.DOG -> listOf(0xFFC4956A, 0xFF3D3D3D, 0xFFF0F0F0)
                PetType.BIRD -> listOf(0xFFFFD93D, 0xFF6EC6FF, 0xFFFAFAFA)
            }
            
            colors.forEachIndexed { index, color ->
                val isSelected = index == selectedVariation
                ColorChip(
                    color = Color(color),
                    isSelected = isSelected,
                    onClick = { onVariationSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun PetTypeChip(
    petType: PetType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emoji = when (petType) {
        PetType.CAT -> "🐱"
        PetType.DOG -> "🐕"
        PetType.BIRD -> "🐦"
    }
    val label = when (petType) {
        PetType.CAT -> "ねこ"
        PetType.DOG -> "いぬ"
        PetType.BIRD -> "とり"
    }
    
    Box(
        modifier = modifier
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
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) GradientStart 
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ColorChip(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) GradientStart else Color.Gray.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() }
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(16.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, GradientStart, CircleShape)
            )
        }
    }
}
