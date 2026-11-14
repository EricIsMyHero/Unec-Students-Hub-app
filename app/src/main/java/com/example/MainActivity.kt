package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MainShell
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.stringsFor
import com.example.ui.theme.UnecTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as UnecApp
        setContent {
            val mainViewModel: MainViewModel = viewModel(factory = app.viewModelFactory)
            val prefs by mainViewModel.prefsState.collectAsState()
            val strings = stringsFor(prefs.lang)

            CompositionLocalProvider(LocalStrings provides strings) {
                UnecTheme(themeKey = prefs.themeKey) {
                    MainShell(
                        factory = app.viewModelFactory,
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }
}
