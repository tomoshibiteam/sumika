package com.sumika.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumika.ui.components.PremiumButton
import com.sumika.ui.components.SecondaryButton
import com.sumika.ui.theme.*
import com.sumika.ui.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    // 登録成功時の画面遷移
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onSignUpSuccess()
        }
    }
    
    // パスワード一致チェック
    val passwordsMatch = password == confirmPassword
    val canSignUp = displayName.isNotBlank() && 
                    email.isNotBlank() && 
                    password.isNotBlank() && 
                    confirmPassword.isNotBlank() && 
                    passwordsMatch
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientStart.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // タイトル
            Text(
                text = "✨",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = Spacing.md)
            )
            
            Text(
                text = "アカウント作成",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = GradientStart
            )
            
            Spacer(modifier = Modifier.height(Spacing.xs))
            
            Text(
                text = "あなただけのペットライフを始めましょう",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // エラーメッセージ
            AnimatedVisibility(visible = state.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerRadius.md))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(Spacing.md)
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.md))
            }
            
            // 名前入力
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("名前") },
                placeholder = { Text("田中 太郎") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientStart,
                    focusedLabelColor = GradientStart
                )
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // メールアドレス入力
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("メールアドレス") },
                placeholder = { Text("example@sumika.app") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientStart,
                    focusedLabelColor = GradientStart
                )
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // パスワード入力
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("パスワード") },
                placeholder = { Text("6文字以上") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientStart,
                    focusedLabelColor = GradientStart
                )
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // パスワード確認
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("パスワード（確認）") },
                placeholder = { Text("もう一度入力") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (canSignUp && !state.isLoading) {
                            viewModel.signUp(email, password, displayName)
                        }
                    }
                ),
                singleLine = true,
                isError = confirmPassword.isNotBlank() && !passwordsMatch,
                supportingText = if (confirmPassword.isNotBlank() && !passwordsMatch) {
                    { Text("パスワードが一致しません") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientStart,
                    focusedLabelColor = GradientStart
                )
            )
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // 登録ボタン
            PremiumButton(
                text = if (state.isLoading) "登録中..." else "アカウント作成",
                onClick = {
                    viewModel.clearError()
                    viewModel.signUp(email, password, displayName)
                },
                enabled = !state.isLoading && canSignUp,
                modifier = Modifier.fillMaxWidth(),
                height = 56.dp
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // キャンセルボタン
            SecondaryButton(
                text = "キャンセル",
                onClick = onNavigateToLogin,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // ログインリンク
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "既にアカウントをお持ちの方は",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "ログイン",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = GradientStart,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}
