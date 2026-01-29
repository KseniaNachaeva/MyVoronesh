package com.example.myvoronesh.features.profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myvoronesh.R
import com.example.myvoronesh.core.ui.theme.Yelloww
import com.example.myvoronesh.features.profile.models.Profile
import com.example.myvoronesh.profile.components.DateVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.myvoronesh.core.ui.theme.Purple40
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditProfileDialog(
    currentProfile: Profile,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
    isSaving: Boolean
) {
    var name by rememberSaveable(currentProfile.name) {
        mutableStateOf(currentProfile.name)
    }

    // Изменено: используем TextFieldValue вместо String
    var birthDate by remember(currentProfile.birthDate) {
        mutableStateOf(
            TextFieldValue(
                text = currentProfile.birthDate,
                selection = TextRange(currentProfile.birthDate.length)
            )
        )
    }

    var email by rememberSaveable(currentProfile.email) {
        mutableStateOf(currentProfile.email)
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(4.dp, Yelloww),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Редактировать профиль",
                    fontFamily = FontFamily(Font(R.font.first)),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Имя
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Имя *") },
                    placeholder = { Text("Введите имя") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = Color.Red) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple40,
                        focusedLabelColor = Purple40,
                        cursorColor = Purple40
                    ),
                    singleLine = true
                )

                // Дата рождения (исправленная версия)
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { newValue ->
                        // Извлекаем только цифры (максимум 8)
                        val digits = newValue.text.filter { it.isDigit() }.take(8)

                        // Форматируем с точками
                        val formatted = formatDateInput(digits)

                        // Вычисляем правильную позицию курсора
                        val cursorPosition = when {
                            digits.length <= 2 -> digits.length
                            digits.length <= 4 -> digits.length + 1  // +1 точка
                            else -> digits.length + 2                 // +2 точки
                        }.coerceAtMost(formatted.length)

                        birthDate = TextFieldValue(
                            text = formatted,
                            selection = TextRange(cursorPosition)
                        )

                        birthDateError = when {
                            formatted.isBlank() -> null
                            formatted.length < 10 -> "Введите дату полностью"
                            !isValidBirthDate(formatted) -> "Некорректная дата"
                            else -> null
                        }
                    },
                    label = { Text("Дата рождения") },
                    placeholder = { Text("дд.ММ.гггг") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = birthDateError != null,
                    supportingText = birthDateError?.let { { Text(it, color = Color.Red) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple40,
                        focusedLabelColor = Purple40,
                        cursorColor = Purple40
                    )
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("example@mail.com") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple40,
                        focusedLabelColor = Purple40,
                        cursorColor = Purple40
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isSaving,
                        border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Отмена", color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            if (name.trim().isEmpty()) {
                                nameError = "Имя обязательно"
                                return@Button
                            }

                            onSave(
                                name.trim(),
                                birthDate.text.trim(),  // Изменено: .text для получения строки
                                email.trim()
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isSaving && birthDateError == null,
                        colors = ButtonDefaults.buttonColors(containerColor = Yelloww),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Сохранить",
                                fontFamily = FontFamily(Font(R.font.first)),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatDateInput(digits: String): String {
    return when {
        digits.length <= 2 -> digits
        digits.length <= 4 -> "${digits.substring(0, 2)}.${digits.substring(2)}"
        else -> "${digits.substring(0, 2)}.${digits.substring(2, 4)}.${digits.substring(4)}"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun isValidBirthDate(value: String): Boolean {
    return try {
        if (!Regex("""\d{2}\.\d{2}\.\d{4}""").matches(value)) return false

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val date = LocalDate.parse(value, formatter)

        date.isBefore(LocalDate.now()) &&
                date.isAfter(LocalDate.of(1900, 1, 1))
    } catch (e: Exception) {
        false
    }
}

