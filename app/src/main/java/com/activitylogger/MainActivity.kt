package com.activitylogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.activitylogger.ui.navigation.ActivityLoggerApp
import com.activitylogger.ui.theme.ActivityLoggerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ActivityLoggerTheme {
                ActivityLoggerApp()
            }
        }
    }
}
