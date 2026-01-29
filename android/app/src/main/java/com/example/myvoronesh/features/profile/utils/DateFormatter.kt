package com.example.myvoronesh.profile.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun formatBirthDate(value: String): String {
    if (value.isBlank()) return "—"

    // Если сервер уже прислал dd.MM.yyyy
    if (Regex("""\d{2}\.\d{2}\.\d{4}""").matches(value)) {
        return value
    }

    // Если вдруг пришли только цифры
    if (value.length == 8 && value.all { it.isDigit() }) {
        return "${value.substring(0,2)}." +
                "${value.substring(2,4)}." +
                "${value.substring(4,8)}"
    }

    return "—"
}
fun normalizeBirthDateFromApi(value: String): String {
    if (value.isBlank()) return ""

    // YYYY-MM-DD → dd.MM.yyyy
    if (Regex("""\d{4}-\d{2}-\d{2}""").matches(value)) {
        val (y, m, d) = value.split("-")
        return "$d.$m.$y"
    }

    // уже dd.MM.yyyy
    if (Regex("""\d{2}\.\d{2}\.\d{4}""").matches(value)) {
        return value
    }

    return ""
}

fun normalizeBirthDateToApi(value: String): String? {
    if (value.isBlank()) return null

    // dd.MM.yyyy → yyyy-MM-dd
    if (Regex("""\d{2}\.\d{2}\.\d{4}""").matches(value)) {
        val (d, m, y) = value.split(".")
        return "$y-$m-$d"
    }

    // уже yyyy-MM-dd
    if (Regex("""\d{4}-\d{2}-\d{2}""").matches(value)) {
        return value
    }

    return null
}





