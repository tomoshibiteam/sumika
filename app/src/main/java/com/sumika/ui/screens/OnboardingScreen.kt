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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumika.core.model.PetCatalog
import com.sumika.core.model.PetCatalogEntry
import com.sumika.core.model.PetType
import com.sumika.ui.components.PremiumButton
import com.sumika.ui.theme.*
import com.sumika.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientStart.copy(alpha = 0.3f),
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
                .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.xxl))
            
            // ヒーローセクション
            Text(
                text = "✨",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = Spacing.md)
            )
            
            Text(
                text = "ようこそ！",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            Text(
                text = "ホーム画面で",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "ペットと暮らそう",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = GradientStart,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            Text(
                text = "まずは最初のペットを選んでください",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Spacing.xxl))
            
            // 無料スターターペット見出し
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🎁",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = Spacing.xs)
                )
                Text(
                    text = "無料で選べるペット",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // ペットカード
            PetCatalog.FREE_STARTER_PETS.forEach { pet ->
                PremiumStarterPetCard(
                    pet = pet,
                    isSelected = state.selectedPetId == pet.id,
                    onSelect = { viewModel.selectPet(pet.id) }
                )
                Spacer(modifier = Modifier.height(Spacing.md))
            }
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // 名前入力
            if (state.selectedPetId != null) {
                OutlinedTextField(
                    value = state.petName,
                    onValueChange = { viewModel.setPetName(it) },
                    label = { 
                        Text(
                            "ペットの名前",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    placeholder = { 
                        Text(PetCatalog.findById(state.selectedPetId!!)?.defaultName ?: "")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md),
                    singleLine = true,
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientStart,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                Spacer(modifier = Modifier.height(Spacing.xl))
                
                // 決定ボタン
                PremiumButton(
                    text = "このペットと暮らす",
                    onClick = {
                        scope.launch {
                            viewModel.completeOnboarding()
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md),
                    enabled = state.petName.isNotBlank()
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun PremiumStarterPetCard(
    pet: PetCatalogEntry,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val emoji = when (pet.type) {
        PetType.CAT -> "🐱"
        PetType.DOG -> "🐕"
        PetType.BIRD -> "🐦"
        PetType.RABBIT -> "🐰"
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(CornerRadius.xl))
            .background(
                brush = if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            GradientStart.copy(alpha = 0.15f),
                            GradientMiddle.copy(alpha = 0.1f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            )
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(GradientStart, GradientMiddle)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                },
                shape = RoundedCornerShape(CornerRadius.xl)
            )
            .clickable { onSelect() }
            .padding(Spacing.lg)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // ペット画像
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(CornerRadius.md))
                    .background(
                        if (isSelected) {
                            Brush.radialGradient(
                                colors = listOf(
                                    GradientStart.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val imageResId = remember(pet.imageResName) {
                    context.resources.getIdentifier(pet.imageResName, "drawable", context.packageName)
                }
                
                if (imageResId != 0) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = imageResId),
                        contentDescription = pet.defaultName,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.xxs),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                } else {
                    // フォールバック
                    Text(
                        text = emoji,
                        fontSize = 40.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(Spacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.defaultName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        GradientStart
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Spacer(modifier = Modifier.height(Spacing.xxs))
                
                Text(
                    text = pet.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(Spacing.sm))
                
                // 性格ラベル
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pet.personalityLabels.take(3).forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(CornerRadius.xs))
                                .background(
                                    if (isSelected) {
                                        GradientStart.copy(alpha = 0.15f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    GradientStart
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            
            // チェックマーク
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(GradientStart, GradientMiddle)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✓",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
