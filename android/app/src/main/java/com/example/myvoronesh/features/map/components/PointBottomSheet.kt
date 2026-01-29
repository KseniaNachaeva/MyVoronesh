package com.example.myvoronesh.features.map.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myvoronesh.core.ui.theme.Grey
import com.example.myvoronesh.core.ui.theme.GreyD
import com.example.myvoronesh.core.ui.theme.Yelloww
import com.example.myvoronesh.data.remote.PhotoDto
import com.example.myvoronesh.features.album.PhotoRepository
import com.example.myvoronesh.features.map.models.QuestPoint

import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


import kotlinx.coroutines.launch

@Composable
fun PointBottomSheet(
    point: QuestPoint,
    onDismiss: () -> Unit,
    onVisitToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoRepository = remember { PhotoRepository(context) }

    // Состояния
    var photos by remember { mutableStateOf<List<PhotoDto>>(emptyList()) }
    var isLoadingPhotos by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    // 🔹 НОВОЕ: состояние для увеличенного фото
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Загружаем фото при открытии
    LaunchedEffect(point.id) {
        isLoadingPhotos = true
        photoRepository.getPhotosForPoint(point.id).fold(
            onSuccess = { photos = it },
            onFailure = { /* игнорируем ошибку */ }
        )
        isLoadingPhotos = false
    }

    // Лаунчер для выбора фото
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploading = true
                uploadError = null

                photoRepository.uploadPhoto(
                    imageUri = selectedUri,
                    pointId = point.id,
                    description = point.title
                ).fold(
                    onSuccess = { uploadedPhoto ->
                        photos = photos + PhotoDto(
                            id = uploadedPhoto.id,
                            photoUrl = uploadedPhoto.photoUrl,
                            fullUrl = uploadedPhoto.fullUrl,
                            description = uploadedPhoto.description,
                            createdAt = null
                        )
                        isUploading = false
                    },
                    onFailure = { error ->
                        uploadError = error.message
                        isUploading = false
                    }
                )
            }
        }
    }

    fun deletePhoto(photoId: String) {
        scope.launch {
            photoRepository.deletePhoto(photoId).fold(
                onSuccess = {
                    photos = photos.filter { it.id != photoId }
                },
                onFailure = {  }
            )
            showDeleteDialog = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp)
                .align(Alignment.BottomCenter)
                .padding(8.dp)
                .clickable(enabled = false) { },
            color = Grey,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                        .then(
                            if (!point.imageUrl.isNullOrEmpty()) {
                                Modifier.clickable {
                                    selectedPhotoUrl = point.imageUrl
                                }
                            } else Modifier
                        )
                ) {
                    if (!point.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(point.imageUrl)
                                .crossfade(true)
                                .error(point.imageRes)
                                .build(),
                            contentDescription = point.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = point.imageRes),
                            contentDescription = point.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = point.title,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = point.description,
                        modifier = Modifier.padding(16.dp),
                        color = Color.Black,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = point.address ?: "Адрес не указан",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Black,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Посетил",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = point.visited,
                            onCheckedChange = onVisitToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Yelloww,
                                checkedTrackColor = GreyD,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = GreyD
                            )
                        )
                    }
                }

                // Секция "Ваши фото"
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ваши фото",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (photos.isNotEmpty()) {
                                Text(
                                    text = "${photos.size} шт.",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        when {
                            isLoadingPhotos -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Yelloww)
                                }
                            }

                            isUploading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = Yelloww)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Загрузка фото...", color = Color.Gray)
                                    }
                                }
                            }

                            else -> {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(photos) { photo ->
                                        PhotoItem(
                                            photoUrl = photo.fullUrl,
                                            onDelete = { showDeleteDialog = photo.id }
                                        )
                                    }

                                    item {
                                        AddPhotoButton(
                                            onClick = { photoPickerLauncher.launch("image/*") }
                                        )
                                    }
                                }
                            }
                        }

                        uploadError?.let { error ->
                            Text(
                                text = "Ошибка: $error",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 🔹 ДИАЛОГ УДАЛЕНИЯ (без изменений)
    showDeleteDialog?.let { photoId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Удалить фото?") },
            text = { Text("Это действие нельзя отменить") },
            confirmButton = {
                TextButton(
                    onClick = { deletePhoto(photoId) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    // 🔹 НОВОЕ: ПОЛНОЭКРАННЫЙ ПРОСМОТР С ZOOM
    selectedPhotoUrl?.let { url ->
        var scale by remember { mutableStateOf(1f) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
            val newScale = (scale * zoomChange).coerceIn(1f, 5f)
            if (newScale == 1f) {
                offsetX = 0f
                offsetY = 0f
            } else {
                offsetX += panChange.x
                offsetY += panChange.y
            }
            scale = newScale
        }

        Dialog(
            onDismissRequest = {
                selectedPhotoUrl = null
                scale = 1f
                offsetX = 0f
                offsetY = 0f
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            // 🔹 Полноэкранный Box без учёта системных отступов
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) { // чтобы клик за пределами работал
                        detectTapGestures(onTap = {
                            selectedPhotoUrl = null
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        })
                    }
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Фото точки",
                    modifier = Modifier
                        .fillMaxSize() // ← растягиваем на весь экран
                        .transformable(transformState)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        },
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = {
                        selectedPhotoUrl = null
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    },
                    modifier = Modifier
                        .padding(40.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoItem(
    photoUrl: String,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Кнопка удаления
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(28.dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    RoundedCornerShape(14.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Удалить",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddPhotoButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 2.dp,
                color = Yelloww.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Добавить",
                tint = Yelloww,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Добавить",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}