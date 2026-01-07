package com.sumika.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sumika.ui.theme.*

/**
 * Pet Grid Card - ショップのグリッドカード
 * サムネ/名前/性格タグ/価格/バッジ
 */
@Composable
fun PetGridCard(
    thumbnail: Int,
    name: String,
    personalityTags: List<String>,
    price: String?,
    isOwned: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SurfaceCard(
        modifier = modifier,
        onClick = onClick,
        elevation = if (isActive) Elevation.lg else Elevation.md,
        cornerRadius = CornerRadius.md
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // サムネイル＋バッジ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                // ペット画像
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(CornerRadius.sm))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = thumbnail),
                        contentDescription = name,
                        modifier = Modifier
                            .size(80.dp)
                            .then(
                                if (!isOwned) {
                                    Modifier
                                } else {
                                    Modifier
                                }
                            ),
                        colorFilter = if (!isOwned) {
                            ColorFilter.colorMatrix(ColorMatrix().apply {
                                setToSaturation(0f)
                            })
                        } else null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
                
                // バッジ
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(Spacing.xs)
                            .clip(RoundedCornerShape(CornerRadius.xs))
                            .background(Primary)
                            .padding(horizontal = Spacing.xs, vertical = Spacing.xxs)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "いま一緒",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "いま一緒",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                } else if (isOwned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(Spacing.xs)
                            .clip(RoundedCornerShape(CornerRadius.xs))
                            .background(Success)
                            .padding(horizontal = Spacing.xs, vertical = Spacing.xxs)
                    ) {
                        Text(
                            text = "お迎え済",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                } else if (price != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(Spacing.xs)
                            .clip(RoundedCornerShape(CornerRadius.xs))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = Spacing.xs, vertical = Spacing.xxs)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "未解放",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = price,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // 名前
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 性格タグ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
            ) {
                personalityTags.take(2).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(CornerRadius.xs))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = Spacing.xs, vertical = Spacing.xxs)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PetGridCardPreview() {
    SumikaTheme {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            PetGridCard(
                thumbnail = android.R.drawable.ic_menu_camera,
                name = "シロ",
                personalityTags = listOf("おっとり", "人懐っこい"),
                price = null,
                isOwned = true,
                isActive = true,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            PetGridCard(
                thumbnail = android.R.drawable.ic_menu_camera,
                name = "クロ",
                personalityTags = listOf("活発", "遊び好き"),
                price = "¥480",
                isOwned = false,
                isActive = false,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}
