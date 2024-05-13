package com.example.campusmarketplace.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.campusmarketplace.R

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val DBluePrimary = hexToColor(R.color.DBluePrimary.toString())
val DBlueSecondary = hexToColor(R.color.DBlueSecondary.toString())
val DBlueTertiary = hexToColor(R.color.DBlueTertiary.toString())

val LBluePrimary = hexToColor(R.color.LBluePrimary.toString())
val LBlueSecondary = hexToColor(R.color.LBlueSecondary.toString())
val LBlueTertiary = hexToColor(R.color.LBlueTertiary.toString())

fun hexToColor(hexColor: String): Color {
    val hex = if (hexColor.startsWith("#")) hexColor.substring(1) else hexColor
    // Parse the hexadecimal string into a Long value
    val colorLong = hex.toLongOrNull(16) ?: return Color.Transparent
    // Create a Color object from the Long value
    return Color(colorLong)
}