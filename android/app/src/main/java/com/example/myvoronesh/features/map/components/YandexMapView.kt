package com.example.myvoronesh.features.map.components

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.myvoronesh.R
import com.example.myvoronesh.features.map.models.QuestPoint
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@Composable
fun YandexMapView(
    points: List<QuestPoint>,
    mapUpdateTrigger: Int,
    onPointClick: (QuestPoint) -> Unit,
    modifier: Modifier = Modifier,
    initialPosition: Point = Point(51.672040, 39.184300),
    initialZoom: Float = 12f
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isMapReady by remember { mutableStateOf(false) }


    val placemarks = remember { mutableMapOf<String, PlacemarkMapObject>() }
    val tapListeners = remember { mutableMapOf<String, MapObjectTapListener>() }
    var initializedPointIds by remember { mutableStateOf(setOf<String>()) }

    val onPointClickState = rememberUpdatedState(onPointClick)


    // MapView

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            if (!MapKitFactory.getInstance().isValid) {
                MapKitFactory.setApiKey("YOUR_API_KEY")
                MapKitFactory.initialize(ctx)
            }

            MapView(ctx).apply {
                map.move(
                    CameraPosition(initialPosition, initialZoom, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 0f),
                    null
                )

                map.addCameraListener { _, _, _, finished ->
                    if (finished) {
                        isMapReady = true
                    }
                }

                mapView = this
            }
        }
    )
    // Инициализация точек
    LaunchedEffect(isMapReady, points, mapUpdateTrigger) {
        if (!isMapReady) return@LaunchedEffect
        if (points.isEmpty()) return@LaunchedEffect

        val mv = mapView ?: return@LaunchedEffect
        val currentPointIds = points.map { it.id }.toSet()

        if (initializedPointIds != currentPointIds) {
            Log.d("MapDebug", "Initializing ${points.size} points")

            mv.map.mapObjects.clear()
            placemarks.clear()
            tapListeners.clear()

            points.forEach { questPoint ->
                try {
                    val placemark = mv.map.mapObjects.addPlacemark(
                        Point(questPoint.latitude, questPoint.longitude)
                    )
                    val iconRes = if (questPoint.visited) {
                        R.drawable.pin_v
                    } else {
                        R.drawable.pin
                    }
                    val iconScale = 1.0f


                    placemark.setIcon(
                        ImageProvider.fromResource(context, R.drawable.pin),
                        IconStyle().apply {
                            scale = iconScale
                            anchor = PointF(0.5f, 1.0f)
                        }
                    )

                    placemarks[questPoint.id] = placemark

                    val tapListener = MapObjectTapListener { _, _ ->
                        onPointClickState.value(questPoint)
                        true
                    }
                    tapListeners[questPoint.id] = tapListener
                    placemark.addTapListener(tapListener)

                } catch (e: Exception) {
                    Log.e("MapDebug", "Error adding point: ${e.message}")
                }
            }

            initializedPointIds = currentPointIds
        }
    }
    // Lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView?.onStart()
                }

                Lifecycle.Event.ON_STOP -> {
                    mapView?.onStop()
                    MapKitFactory.getInstance().onStop()
                }

                else -> {}
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            placemarks.clear()
            tapListeners.clear()
        }
    }
}