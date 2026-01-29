package com.example.myvoronesh.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myvoronesh.R
import com.example.myvoronesh.core.ui.components.*
import com.example.myvoronesh.core.ui.theme.Yelloww

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSendReset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(id = R.drawable.back),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.8f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Восстановление пароля",
                color = Yelloww,
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.first)),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Введите ваш email, чтобы получить ссылку для восстановления",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.first)),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            ShadowTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("введите email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            ShadowButton(
                text = "Отправить",
                onClick = {
                    onSendReset(email)
                    onBackClick()
                },
                containerColor = Yelloww,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBackClick) {
                Text("← Назад", color = Color.White, fontFamily = FontFamily(Font(R.font.first)))
            }
        }
    }
}