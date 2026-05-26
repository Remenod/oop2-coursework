package com.remenod.oop2_coursework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.remenod.oop2_coursework.presentation.navigation.AppNavHost
import com.remenod.oop2_coursework.ui.theme.Oop2courseworkTheme

class MainActivity : ComponentActivity() {
    private val appContainer = AppContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Oop2courseworkTheme {
                AppNavHost(appContainer)
            }
        }
    }
}
