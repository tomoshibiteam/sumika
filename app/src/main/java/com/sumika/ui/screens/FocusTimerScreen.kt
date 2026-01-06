package com.sumika.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumika.ui.theme.Primary
import com.sumika.ui.theme.Success
import kotlinx.coroutines.delay

@Composable
fun FocusTimerScreen() {
    var isRunning by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }  // 25分
    
    LaunchedEffect(isRunning) {
        while (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        if (remainingSeconds == 0) {
            isRunning = false
        }
    }
    
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "集中タイマー",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "ペットと一緒に集中しよう",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // タイマー表示
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // コントロールボタン
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isRunning) {
                Button(
                    onClick = { isRunning = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("一時停止")
                }
            } else {
                Button(
                    onClick = { isRunning = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("開始")
                }
            }
            
            OutlinedButton(
                onClick = {
                    isRunning = false
                    remainingSeconds = 25 * 60
                },
                modifier = Modifier.width(120.dp)
            ) {
                Text("リセット")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 報酬説明
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Success.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🎁", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "タイマー完了報酬",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ペットが喜び、成長XPを獲得！",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
