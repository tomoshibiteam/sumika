package com.sumika.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sumika.core.model.PetCatalogEntry
import com.sumika.ui.components.PremiumButton
import com.sumika.ui.components.SurfaceCard
import com.sumika.ui.theme.*

/**
 * Pet Detail Screen - お迎え/購入の意思決定画面
 * 大きいプレビュー＋性格説明＋確認ダイアログ
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    petEntry: PetCatalogEntry,
    isOwned: Boolean,
    isActive: Boolean,
    onBack: () -> Unit,
    onAdopt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val personalityText = buildString {
        append("この子は")
        append(petEntry.personalityLabels.firstOrNull() ?: "個性的")
        append("な性格です。ホーム画面で一緒に過ごせます。")
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Spacing.md)
                .padding(top = Spacing.lg, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Big Preview
            SurfaceCard(
                elevation = Elevation.lg,
                cornerRadius = CornerRadius.lg
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    // ペット大画像
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(CornerRadius.md))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = android.R.drawable.ic_menu_camera),
                            contentDescription = petEntry.defaultName,
                            modifier = Modifier.size(200.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                    
                    // 名前
                    Text(
                        text = petEntry.defaultName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 性格タグ
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        petEntry.personalityLabels.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(CornerRadius.sm))
                                    .background(Primary.copy(alpha = 0.1f))
                                    .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Personality Section
            SurfaceCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = "性格",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = petEntry.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }
            }
            
            // 反応デモ
            SurfaceCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = "一緒に暮らすと",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = personalityText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Top Bar
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(Spacing.md)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "戻る",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        
        // Bottom Action
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = Elevation.md
        ) {
            Box(
                modifier = Modifier.padding(Spacing.md)
            ) {
                when {
                    isActive -> {
                        Text(
                            text = "いま一緒に暮らしています",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Success,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    isOwned -> {
                        PremiumButton(
                            text = "この子と一緒に暮らす",
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        PremiumButton(
                            text = if (petEntry.isProOnly) "Proプランでお迎え" else "この子をお迎えする",
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = if (isOwned) "確認" else "お迎え",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val dialogMessage = buildString {
                    append(petEntry.defaultName)
                    append(if (isOwned) "と一緒に暮らし" else "をお迎えし")
                    append("ます。この子がホームで暮らします。ペットの変更はショップから行えます。よろしいですか？")
                }
                Text(
                    text = dialogMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onAdopt()
                }) {
                    Text("はい")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PetDetailScreenPreview() {
    SumikaTheme {
        PetDetailScreen(
            petEntry = com.sumika.core.model.PetCatalog.ALL_PETS.first(),
            isOwned = false,
            isActive = false,
            onBack = {},
            onAdopt = {}
        )
    }
}
