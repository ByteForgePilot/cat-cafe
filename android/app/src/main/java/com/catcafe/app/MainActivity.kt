package com.catcafe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.catcafe.app.data.api.RetrofitClient
import com.catcafe.app.ui.navigation.NavGraph
import com.catcafe.app.ui.theme.CatCafeTheme
import com.catcafe.app.util.TokenManager
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(applicationContext)
        RetrofitClient.init(tokenManager)

        setContent {
            CatCafeTheme {
                val token by tokenManager.tokenFlow.collectAsState(initial = null)
                NavGraph(token = token)
            }
        }
    }
}
