package com.example.myvoronesh.features.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myvoronesh.R
import com.example.myvoronesh.features.map.MapViewModel

@Composable
fun MapContent(
    viewModel: MapViewModel,
    mapUpdateTrigger: Int,
    onQuestsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    Box(modifier = modifier.fillMaxSize()) {
        key(uiState.visiblePoints) {
            YandexMapView(
                points = uiState.visiblePoints,
                mapUpdateTrigger = mapUpdateTrigger,
                onPointClick = viewModel::onPointClick,
                modifier = Modifier.fillMaxSize()
            )
        }

        FloatingActionButton(
            onClick = onQuestsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .size(43.dp),
            containerColor = Color.White,
            contentColor = Color.Black,
            shape = RoundedCornerShape(17.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.maps),
                contentDescription = "Квесты",
                modifier = Modifier
                    .size(25.dp)
                    .alpha(0.8f)
            )
        }

        if (uiState.visiblePoints.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 48.dp, start = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "Точек: ${uiState.visiblePoints.size}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.Black
                )
            }
        }

    }
}