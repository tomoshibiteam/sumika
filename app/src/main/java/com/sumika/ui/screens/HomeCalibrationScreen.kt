package com.sumika.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sumika.core.data.PetStateRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCalibrationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { PetStateRepository(context) }
    
    var tappedPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 半透明の背景でユーザーをガイド
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        tappedPoint = offset.x / size.width to offset.y / size.height
                    }
                }
        )
        
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "おうちの位置を設定",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ホーム画面でSumikaのアイコンがある場所を思い出し、そこをタップしてください。\nペットが時々そこへ帰るようになります。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (tappedPoint != null) {
                Button(
                    onClick = {
                        scope.launch {
                            repository.setHomePosition(tappedPoint!!.first, tappedPoint!!.second)
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("ここに決定！")
                }
            }
        }
        
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .statusBarsPadding()
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "戻る", tint = Color.White)
        }
        
        // タップした位置のプレビュー
        tappedPoint?.let { (px, py) ->
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = px * size.width
                val cy = py * size.height
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = 60f,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 10f,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
            }
            
            // 家のアイコンっぽいのを出す
            Box(
                modifier = Modifier
                    .offset(
                        x = tappedPoint!!.first.toInt().dp, // Note: This is wrong, needs proper px to dp or layout logic
                        y = tappedPoint!!.second.toInt().dp
                    )
            ) {
                // Skip complex layout for now, just circle is fine
            }
        }
    }
}
