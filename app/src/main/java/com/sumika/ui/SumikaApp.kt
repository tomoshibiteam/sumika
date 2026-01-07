package com.sumika.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sumika.core.data.AuthRepository
import com.sumika.core.data.PetStateRepository
import com.sumika.ui.screens.*
import com.sumika.ui.screens.auth.LoginScreen
import com.sumika.ui.screens.auth.SignUpScreen
import com.sumika.ui.viewmodel.AuthViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Login : Screen("login", "ログイン", Icons.Default.Home)
    data object SignUp : Screen("signup", "新規登録", Icons.Default.Home)
    data object Onboarding : Screen("onboarding", "初期設定", Icons.Default.Home)
    data object Home : Screen("home", "ホーム", Icons.Default.Home)
    data object PetShop : Screen("pet_shop", "ペット一覧", Icons.Default.ShoppingCart)
    data object PetDetail : Screen("pet_detail", "ペット詳細", Icons.Default.ShoppingCart)
    data object Settings : Screen("settings", "設定", Icons.Default.Settings)
    data object HomeCalibration : Screen("home_calibration", "おうち設定", Icons.Default.Settings)
}

val bottomNavItems = listOf(Screen.Home, Screen.PetShop, Screen.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SumikaApp(
    repository: PetStateRepository = hiltViewModel<SumikaAppViewModel>().repository,
    authRepository: AuthRepository = hiltViewModel<SumikaAppViewModel>().authRepository,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val onboardingCompleted by repository.onboardingCompletedFlow.collectAsState(initial = null)
    val isAuthenticated by authRepository.isAuthenticated.collectAsState(initial = null)
    
    // 認証状態とオンボーディング状態が読み込まれるまで待つ
    if (onboardingCompleted == null || isAuthenticated == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
        }
        return
    }
    
    // スタート画面を決定
    val startDestination = when {
        isAuthenticated == false -> Screen.Login.route
        onboardingCompleted == false -> Screen.Onboarding.route
        else -> Screen.Home.route
    }
    
    // 認証されていない、またはオンボーディング未完了の場合はボトムナビなし
    if (isAuthenticated == false || onboardingCompleted == false) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onLoginSuccess = {
                        // ログイン成功後、オンボーディングまたはホームへ
                        val destination = if (onboardingCompleted == true) Screen.Home.route else Screen.Onboarding.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onSignUpSuccess = {
                        // 新規登録成功後、オンボーディングへ
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    } else {
        // メインアプリ（ボトムナビあり）
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { HomeScreen() }
                composable(Screen.PetShop.route) { 
                    PetShopScreen(
                        onNavigateToDetail = { pet ->
                            // ペットIDを保存して詳細画面へ
                            navController.currentBackStackEntry?.savedStateHandle?.set("selectedPetId", pet.id)
                            navController.navigate(Screen.PetDetail.route)
                        }
                    )
                }
                composable(Screen.PetDetail.route) {
                    // ペットIDを取得
                    val petId = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selectedPetId")
                    val petEntry = petId?.let { id -> com.sumika.core.model.PetCatalog.findById(id) }
                    
                    if (petEntry != null) {
                        val petShopViewModel: com.sumika.ui.viewmodel.PetShopViewModel = hiltViewModel(navController.previousBackStackEntry!!)
                        val state by petShopViewModel.state.collectAsState()
                        val isOwned = state.ownedPets.contains(petEntry.id)
                        val isActive = state.activePetId == petEntry.id
                        
                        PetDetailScreen(
                            petEntry = petEntry,
                            isOwned = isOwned,
                            isActive = isActive,
                            onBack = { navController.popBackStack() },
                            onAdopt = {
                                if (isOwned) {
                                    // 既に所有している場合はアクティブにするだけ
                                    petShopViewModel.selectPet(petEntry.id)
                                } else {
                                    // 未所有の場合はお迎えする
                                    petShopViewModel.adoptPet(petEntry.id, petEntry.defaultName)
                                }
                                navController.popBackStack()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                        )
                    } else {
                        // エラー: ペットが見つからない
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("ペットが見つかりません", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                        }
                    }
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateToCalibration = { navController.navigate(Screen.HomeCalibration.route) },
                        onLogout = {
                            authViewModel.signOut()
                            // ログアウト後、ログイン画面へ
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.HomeCalibration.route) {
                    HomeCalibrationScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// ViewModel to inject repository into Composable
@dagger.hilt.android.lifecycle.HiltViewModel
class SumikaAppViewModel @javax.inject.Inject constructor(
    val repository: PetStateRepository,
    val authRepository: AuthRepository
) : androidx.lifecycle.ViewModel()
