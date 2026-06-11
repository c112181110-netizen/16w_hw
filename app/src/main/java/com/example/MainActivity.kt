package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.repository.FoodRepository
import com.example.ui.screens.FoodOrderApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FoodViewModel
import com.example.viewmodel.FoodViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite Database and Data Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FoodRepository(database.foodDao())
        val viewModelFactory = FoodViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                // Instantiate the injected FoodViewModel with the Compose viewmodel helper
                val viewModel: FoodViewModel = viewModel(factory = viewModelFactory)
                FoodOrderApp(viewModel = viewModel)
            }
        }
    }
}
