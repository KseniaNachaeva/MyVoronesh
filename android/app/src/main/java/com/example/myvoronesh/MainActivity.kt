package com.example.myvoronesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.myvoronesh.core.ui.theme.MyVORONESHTheme
import com.example.myvoronesh.core.navigation.AppNavigation
import com.yandex.mapkit.MapKitFactory

class MainActivity : ComponentActivity() {

    companion object {
        init {
            try {
                MapKitFactory.setApiKey("39de89b9-f9c7-4207-b3c6-004f498c274e")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            MapKitFactory.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        enableEdgeToEdge()

        setContent {
            MyVORONESHTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            MapKitFactory.getInstance()?.onStart()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        try {
            MapKitFactory.getInstance()?.onStop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onStop()
    }
}