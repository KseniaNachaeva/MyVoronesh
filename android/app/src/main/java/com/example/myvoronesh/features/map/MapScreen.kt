package com.example.myvoronesh.features.map

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.myvoronesh.R
import com.example.myvoronesh.core.ui.theme.Yelloww
import com.example.myvoronesh.features.album.PhotoAlbumScreen
import com.example.myvoronesh.features.goals.GoalsScreen
import com.example.myvoronesh.features.map.components.MapContent
import com.example.myvoronesh.features.profile.ProfileScreen
import com.example.myvoronesh.features.quests.EmptyQuestsDialog
import com.example.myvoronesh.features.quests.QuestsScreen
import com.example.myvoronesh.features.quests.QuestDialog
import com.example.myvoronesh.features.quests.QuestsViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myvoronesh.features.map.components.PointBottomSheet


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    questsViewModel: QuestsViewModel = viewModel(),
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val questsForDialog by questsViewModel.questsForDialog.collectAsStateWithLifecycle()
    var showQuestDialog by remember { mutableStateOf(false) }

    var mapUpdateTrigger by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.fillMaxSize()) {
        when (uiState.selectedScreen) {
            BottomNavScreen.MAP -> {
                MapContent(
                    viewModel = viewModel,
                    mapUpdateTrigger = mapUpdateTrigger,
                    onQuestsClick = { showQuestDialog = true }
                )
            }
            BottomNavScreen.PROFILE -> {
                ProfileScreen(
                    onBackClick = {
                        viewModel.selectScreen(BottomNavScreen.MAP)
                    },
                    onLogoutClick = onLogout
                )
            }
            BottomNavScreen.GALLERY -> {
                PhotoAlbumScreen(
                    onBackClick = {
                        viewModel.selectScreen(BottomNavScreen.MAP)
                    }
                )
            }
            BottomNavScreen.QUESTS -> {
                QuestsScreen(
                    onBackClick = {
                        viewModel.selectScreen(BottomNavScreen.MAP)
                    },
                    viewModel = questsViewModel
                )
            }
            BottomNavScreen.GOALS -> {
                GoalsScreen(
                    onBackClick = {
                        viewModel.selectScreen(BottomNavScreen.MAP)
                    }
                )
            }
        }

        if (showQuestDialog) {
            if (questsForDialog.isNotEmpty()) {
                QuestDialog(
                    onDismiss = { showQuestDialog = false },
                    quests = questsForDialog,
                    onQuestToggle = { questId, isActive ->
                        questsViewModel.toggleQuestInDialog(questId, isActive)
                    },
                    onSave = {
                        showQuestDialog = false
                    },
                    onMapNeedRefresh = {
                        mapUpdateTrigger++
                    }
                )
            } else {
                EmptyQuestsDialog(
                    onDismiss = { showQuestDialog = false },
                    onGoToQuests = {
                        showQuestDialog = false
                        viewModel.selectScreen(BottomNavScreen.QUESTS)
                    },
                    onMapNeedRefresh = {
                        mapUpdateTrigger++
                    }
                )
            }
        }

        BottomAppBar(
            containerColor = Color.White.copy(alpha = 1f),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 12.dp)
            ) {
                NavButtonLeft(
                    iconRes = R.drawable.account,
                    label = "Я",
                    isSelected = uiState.selectedScreen == BottomNavScreen.PROFILE,
                    onClick = {
                        viewModel.selectScreen(BottomNavScreen.PROFILE)
                    }
                )
                NavButtonLeft(
                    iconRes = R.drawable.mapp,
                    label = "Фото",
                    isSelected = uiState.selectedScreen == BottomNavScreen.GALLERY,
                    onClick = {
                        viewModel.selectScreen(BottomNavScreen.GALLERY)
                    }
                )
                NavButtonFalse(
                    iconRes = R.drawable.map,
                    contentDescription = "Home",
                    isSelected = uiState.selectedScreen == BottomNavScreen.MAP,
                    onClick = {
                        viewModel.selectScreen(BottomNavScreen.MAP)
                    }
                )
                NavButtonRight (
                    iconRes = R.drawable.photo,
                    label = "Квесты",
                    isSelected = uiState.selectedScreen == BottomNavScreen.QUESTS,
                    onClick = {
                        viewModel.selectScreen(BottomNavScreen.QUESTS)
                    }
                )
                NavButtonRight(
                    iconRes = R.drawable.trophy,
                    label = "Цели",
                    isSelected = uiState.selectedScreen == BottomNavScreen.GOALS,
                    onClick = {
                        viewModel.selectScreen(BottomNavScreen.GOALS)
                    }
                )
            }
        }
    }
    uiState.selectedPoint?.let { point ->
        PointBottomSheet(
            point = point,
            onDismiss = { viewModel.dismissBottomSheet() },
            onVisitToggle = { visited ->
                viewModel.toggleVisited(point.id, visited)
            }
        )
    }
}

@Composable
private fun NavButtonRight(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Yelloww else Color.White
    val textColor = if (isSelected) Color.Black else Color.Gray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .animateContentSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            if (isSelected) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = textColor,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
@Composable
private fun NavButtonLeft(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Yelloww else Color.White
    val textColor = if (isSelected) Color.Black else Color.Gray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .animateContentSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            if (isSelected) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = textColor,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
@Composable
private fun NavButtonFalse(
    iconRes: Int,
    contentDescription: String, // оставляем для accessibility
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Yelloww else Color.White
    val contentColor = if (isSelected) Color.Black else Color.Gray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .size(48.dp), // фиксированный размер для иконки-кнопки (по желанию)
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
