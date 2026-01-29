package com.example.myvoronesh.features.album

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myvoronesh.R
import com.example.myvoronesh.core.ui.theme.Yelloww
import com.example.myvoronesh.features.album.models.LocationPhotos
import com.example.myvoronesh.features.album.models.Photo
import kotlinx.coroutines.delay

 @Composable
fun PhotoAlbumScreen(
    onBackClick: () -> Unit,
    viewModel: PhotoAlbumViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }
    val context = LocalContext.current
    var selectedPointId: String? by remember { mutableStateOf(null) }
    var photoToDelete: String? by remember { mutableStateOf(null) }

    val photoPickerLauncher =
        rememberLauncherForActivityResult<String, Uri?>(
            contract = ActivityResultContracts.GetContent()
        ) { resultUri ->
            resultUri?.let { uri ->
                selectedPointId?.let { pointId ->
                    viewModel.uploadPhoto(uri, pointId)
                }
            }
            selectedPointId = null
        }



    LaunchedEffect(uiState.successMessage, uiState.error) {
        if (uiState.successMessage != null || uiState.error != null) {
            delay(2000)
            viewModel.clearMessages()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.back),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            TopBar(
                onBackClick = onBackClick,
                totalPhotos = uiState.totalPhotos
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Yelloww)
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "😕", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error ?: "Ошибка",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadPhotos() },
                                colors = ButtonDefaults.buttonColors(containerColor = Yelloww)
                            ) {
                                Text("Повторить", color = Color.Black)
                            }
                        }
                    }
                }

                uiState.photosByLocation.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item { EmptyPhotoState() }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(
                            items = uiState.photosByLocation,
                            key = { it.pointId }
                        ) { locationPhotos ->
                            LocationPhotoSection(
                                locationPhotos = locationPhotos,
                                onPhotoClick = { photo ->
                                    selectedPhoto = photo
                                },
                                onPhotoDelete = { photoId -> photoToDelete = photoId },
                                onAddPhoto = {
                                    selectedPointId = locationPhotos.pointId
                                    photoPickerLauncher.launch("image/*")
                                },
                                isUploading = uiState.isUploading &&
                                        selectedPointId == locationPhotos.pointId
                            )

                        }
                    }
                }
            }
        }

        if (uiState.isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Yelloww)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Загрузка фото...")
                    }
                }
            }
        }
    }

    photoToDelete?.let { photoId ->
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text("Удалить фото?") },
            text = { Text("Это действие нельзя отменить") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePhoto(photoId)
                        photoToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    selectedPhoto?.let { photo ->

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
                selectedPhoto = null
                scale = 1f
                offsetX = 0f
                offsetY = 0f
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

                AsyncImage(
                    model = photo.imageUrl,
                    contentDescription = photo.description,
                    modifier = Modifier
                        .fillMaxSize()
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
                        selectedPhoto = null
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    },
                    modifier = Modifier
                        .padding(40.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White
                    )
                }
            }
        }
    }





}

@Composable
private fun TopBar(
    onBackClick: () -> Unit,
    totalPhotos: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Фотоальбом",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.first))
        )

        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun LocationPhotoSection(
    locationPhotos: LocationPhotos,
    onPhotoClick: (Photo) -> Unit,
    onPhotoDelete: (String) -> Unit,
    onAddPhoto: () -> Unit,
    isUploading: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Yelloww.copy(alpha = 0.1f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "📍", fontSize = 18.sp)
                        Text(
                            text = locationPhotos.locationName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = FontFamily(Font(R.font.first))
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = locationPhotos.questName,
                            fontSize = 14.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = locationPhotos.date,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = Yelloww.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = locationPhotos.photos.size.toString(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = locationPhotos.photos,
                    key = { it.id }
                ) { photo ->
                    PhotoItem(
                        photo = photo,
                        onClick = { onPhotoClick(photo) },
                        onDelete = { onPhotoDelete(photo.id) }
                    )

                }

                item {
                    AddPhotoButton(
                        onClick = onAddPhoto,
                        isLoading = isUploading
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoItem(
    photo: Photo,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .size(120.dp)
            .clickable { onClick() }
        ,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            if (!photo.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photo.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = photo.description,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            } else if (photo.imageRes != 0) {
                Image(
                    painter = painterResource(id = photo.imageRes),
                    contentDescription = photo.description,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)),
                            startY = 80f
                        )
                    )
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
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
}

@Composable
private fun AddPhotoButton(
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .size(120.dp)
            .clickable(enabled = !isLoading) { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        border = BorderStroke(2.dp, Yelloww.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Yelloww, modifier = Modifier.size(32.dp))
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "+", fontSize = 32.sp, color = Yelloww, fontWeight = FontWeight.Light)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Добавить", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun EmptyPhotoState() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📷", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Пока нет фотографий",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Посещайте точки квестов и добавляйте фото",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}