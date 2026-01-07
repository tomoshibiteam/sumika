package com.sumika.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.sumika.ui.theme.Spacing
import com.sumika.ui.theme.SumikaTheme

/**
 * App Top Bar - 大見出し＋サブテキスト＋右アクション
 * 全画面で統一されたヘッダー
 */
@Composable
fun AppTopBar(
    greeting: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    rightAction: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Spacing.xxs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        if (rightAction != null) {
            Spacer(modifier = Modifier.width(Spacing.md))
            rightAction()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    SumikaTheme {
        AppTopBar(
            greeting = "おかえり！",
            subtitle = "シロが待っているよ",
            modifier = Modifier.padding(Spacing.md)
        )
    }
}
