package com.example.myvoronesh.features.quests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myvoronesh.R
import com.example.myvoronesh.core.ui.theme.Yelloww
import com.example.myvoronesh.features.quests.models.Quest

@Composable
fun QuestDialog(
    onDismiss: () -> Unit,
    quests: List<Quest>,
    onQuestToggle: (String, Boolean) -> Unit,
    onSave: () -> Unit,
    onMapNeedRefresh: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(6.dp, Yelloww),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ВЫБЕРИ СВОЙ ПУТЬ",
                    fontFamily = FontFamily(Font(R.font.first)),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                )

                LazyColumn {
                    items(quests) { quest ->
                        QuestItem(
                            title = quest.title,
                            isSelected = quest.isEnabled,
                            onToggle = { enabled ->
                                onQuestToggle(quest.id, enabled)
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        onMapNeedRefresh() // 🔥 сигнал карте
                        onSave()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Yelloww)
                ) {
                    Text(
                        "Готово",
                        fontFamily = FontFamily(Font(R.font.first)),
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyQuestsDialog(
    onDismiss: () -> Unit,
    onGoToQuests: () -> Unit,
    onMapNeedRefresh: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            onMapNeedRefresh()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(6.dp, Yelloww),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "😔",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Нет активных квестов",
                    fontFamily = FontFamily(Font(R.font.first)),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Выберите квесты в разделе \"Квесты\"",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Закрыть")
                    }

                    Button(
                        onClick = onGoToQuests,
                        colors = ButtonDefaults.buttonColors(containerColor = Yelloww),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "К квестам",
                            color = Color.Black,
                            fontFamily = FontFamily(Font(R.font.first))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestItem(
    title: String,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontFamily = FontFamily(Font(R.font.first)),
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isSelected,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Yelloww,
                checkedTrackColor = Color.LightGray,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}