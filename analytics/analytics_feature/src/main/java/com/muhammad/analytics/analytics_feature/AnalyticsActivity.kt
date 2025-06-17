package com.muhammad.analytics.analytics_feature

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.splitcompat.SplitCompat
import com.muhammad.analytics.data.di.analyticsDataModule
import com.muhammad.analytics.presentation.AnalyticsDashboardScreen
import com.muhammad.analytics.presentation.di.analyticsModule
import com.muhammad.core.presentation.designsystem.RunwellTheme
import org.koin.core.context.loadKoinModules

class AnalyticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ), navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        loadKoinModules(
            listOf(
                analyticsModule, analyticsDataModule
            )
        )
        SplitCompat.installActivity(this)
        setContent {
            RunwellTheme {
                val navHostController = rememberNavController()
                NavHost(
                    navController = navHostController,
                    startDestination = "analytics_dashboard"
                ) {
                    composable("analytics_dashboard") {
                        AnalyticsDashboardScreen(onBackClick = {
                            finish()
                        })
                    }
                }
            }
        }
    }
}