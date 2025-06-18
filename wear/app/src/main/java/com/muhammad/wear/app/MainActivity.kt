package com.muhammad.wear.app

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.muhammad.core.notification.ActiveRunService
import com.muhammad.core.presentation.designsystem_wear.RunwellTheme
import com.muhammad.wear.run.presentation.TrackerScreen

class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            RunwellTheme{
                TrackerScreen(onServiceToggle = {shouldStartRunning ->
                    if(shouldStartRunning){
                       startService(ActiveRunService.createStartIntent(applicationContext, this::class.java))
                    } else{
                        stopService(ActiveRunService.createStopIntent(context = applicationContext))
                    }
                })
            }
        }
    }
}