package com.sumika.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumika.core.model.PetCatalog
import com.sumika.core.model.PetCatalogEntry
import com.sumika.core.model.PetType
import com.sumika.ui.components.AppTopBar
import com.sumika.ui.components.PetGridCard
import com.sumika.ui.theme.*
import com.sumika.ui.viewmodel.PetShopViewModel

/**
 * Pet Shop Screen - ペットショップ体験に特化
 * グリッド表示＋フィルタ＋詳細画面への導線
 */
@Composable
fun PetShopScreen(
    viewModel: PetShopViewModel = hiltViewModel(),
    onNavigateToDetail: (PetCatalogEntry) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var selectedFilter by remember { mutableStateOf<PetType?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.md)
            .padding(top = Spacing.lg, bottom = Spacing.md)
    ) {
        // Header
        AppTopBar(
            greeting = "ペット一覧",
            subtitle = "${state.ownedPets.size}/${PetCatalog.ALL_PETS.size} お迎え済",
            rightAction = {
                // ProバッジまたはUpgradeボタン
                if (state.isProSubscriber) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(CornerRadius.sm))
                            .background(Primary)
                            .padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                    ) {
                        Text(
                            text = "PRO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(Spacing.md))
        
        // フィルタチップ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("すべて") }
            )
            FilterChip(
                selected = selectedFilter == PetType.CAT,
                onClick = { selectedFilter = PetType.CAT },
                label = { Text("猫") }
            )
            FilterChip(
                selected = selectedFilter == PetType.DOG,
                onClick = { selectedFilter = PetType.DOG },
                label = { Text("犬") }
            )
            FilterChip(
                selected = selectedFilter == PetType.BIRD,
                onClick = { selectedFilter = PetType.BIRD },
                label = { Text("鳥") }
            )
            FilterChip(
                selected = selectedFilter == PetType.RABBIT,
                onClick = { selectedFilter = PetType.RABBIT },
                label = { Text("ウサギ") }
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.md))
        
        // Grid
        val filteredPets = remember(selectedFilter) {
            if (selectedFilter == null) {
                PetCatalog.ALL_PETS
            } else {
                PetCatalog.ALL_PETS.filter { it.type == selectedFilter }
            }
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(vertical = Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(filteredPets) { pet ->
                val isOwned = state.ownedPets.contains(pet.id)
                val isActive = state.activePetId == pet.id
                
                // 画像リソースIDを取得（仮のロジック）
                val thumbnailResId = remember(pet.imageResName) {
                    // TODO: 実際のリソースIDを返すロジック
                    android.R.drawable.ic_menu_camera
                }
                
                PetGridCard(
                    thumbnail = thumbnailResId,
                    name = pet.defaultName,
                    personalityTags = pet.personalityLabels.take(2),
                    price = if (!isOwned && pet.isProOnly) "Pro限定" else if (!isOwned) "未所有" else null,
                    isOwned = isOwned,
                    isActive = isActive,
                    onClick = { onNavigateToDetail(pet) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PetShopScreenPreview() {
    SumikaTheme {
        PetShopScreen()
    }
}
