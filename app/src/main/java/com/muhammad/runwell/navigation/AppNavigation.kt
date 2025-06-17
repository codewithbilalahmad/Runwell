package com.muhammad.runwell.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.muhammad.auth.presentation.intro.IntroScreen
import com.muhammad.auth.presentation.login.LoginScreen
import com.muhammad.auth.presentation.register.RegisterScreen
import com.muhammad.core.notification.ActiveRunService
import com.muhammad.run.presentation.active_run.ActiveRunScreen
import com.muhammad.run.presentation.run_overview.RunOverviewScreen
import com.muhammad.runwell.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Composable
fun AppNavigation(
    navHostController: NavHostController,
    isLoggedIn: Boolean,
    onAnalyticsClick: () -> Unit,
) {
    val context = LocalContext.current
    val startDestination = if (isLoggedIn) Destinations.Run else Destinations.Auth
    NavHost(navController = navHostController, startDestination = Destinations.Run) {
        authGraph(navHostController)
        runGraph(navHostController = navHostController, onAnalyticsClick = onAnalyticsClick)
    }
}

private fun NavGraphBuilder.authGraph(navHostController: NavHostController) {
    navigation<Destinations.Auth>(startDestination = Destinations.Intro) {
        composable<Destinations.Intro> {
            IntroScreen(onSignInClick = {
                navHostController.navigate(Destinations.Login)
            }, onSignUpClick = {
                navHostController.navigate(Destinations.Register)
            })
        }
        composable<Destinations.Login> {
            LoginScreen(onLoginSuccess = {
                navHostController.navigate(Destinations.Run) {
                    popUpTo(Destinations.Auth) {
                        inclusive = true
                    }
                }
            }, onSignUpClick = {
                navHostController.navigate(Destinations.Register) {
                    popUpTo(Destinations.Login) {
                        inclusive = true
                        saveState = true
                    }
                    restoreState = true
                }
            })
        }
        composable<Destinations.Register> {
            RegisterScreen(onSignInClick = {
                navHostController.navigate(Destinations.Login) {
                    popUpTo(Destinations.Register) {
                        inclusive = true
                        saveState = true
                    }
                    restoreState = true
                }
            }, onSuccessfulRegistration = {
                navHostController.navigate(Destinations.Login)
            })
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun NavGraphBuilder.runGraph(
    navHostController: NavHostController,
    onAnalyticsClick: () -> Unit,
) {
    navigation<Destinations.Run>(startDestination = Destinations.RunOverview) {
        composable<Destinations.RunOverview> {
            RunOverviewScreen(
                onStartRunClick = {
                    navHostController.navigate(Destinations.ActiveRun)
                },
                onAnalyticsClick = onAnalyticsClick,
                onLogoutClick = {
                    navHostController.navigate(Destinations.Auth){
                        popUpTo(Destinations.Run){
                            inclusive = true
                        }
                    }
                })
        }
        composable<Destinations.ActiveRun>(
            deepLinks = listOf(
                navDeepLink<Destinations.ActiveRun>(
                    basePath = "runwell://active_run"
                )
            )
        ) {
            val context = LocalContext.current
            ActiveRunScreen(
                onBack = {
                    navHostController.navigateUp()
                },
                onFinish = { navHostController.navigateUp() },
                onServiceToggle = { shouldServiceRun ->
                    if (shouldServiceRun) {
                        context.startService(
                            ActiveRunService.createStartIntent(
                                context = context,
                                activityClass = MainActivity::class.java
                            )
                        )
                    } else {
                        context.startService(
                            ActiveRunService.createStopIntent(context)
                        )
                    }
                })
        }
    }
}