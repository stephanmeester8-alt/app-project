package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainScaffold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AIVaultsViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AIVaultsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScaffold(viewModel = viewModel)
            }
        }
    }
}

