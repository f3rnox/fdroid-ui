package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun AppIcon(
    iconUrl: String,
    appName: String,
    modifier: Modifier = Modifier,
    packageName: String = "",
    fallbackColor: Color = MaterialTheme.colorScheme.primary,
    fontSize: TextUnit = 16.sp,
    onSuccess: () -> Unit = {}
) {
    var currentUrl by remember(iconUrl) { mutableStateOf(iconUrl) }
    var triedFallback by remember(iconUrl) { mutableStateOf(false) }

    if (currentUrl.isNotEmpty()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentUrl)
                .crossfade(true)
                .build(),
            contentDescription = "$appName icon",
            modifier = modifier,
            contentScale = ContentScale.Fit,
            onError = {
                if (packageName.isNotEmpty() && !triedFallback) {
                    triedFallback = true
                    // Try the standard F-Droid repository fallback
                    currentUrl = "https://f-droid.org/repo/icons-640/$packageName.png"
                } else {
                    currentUrl = ""
                }
            },
            onSuccess = { onSuccess() }
        )
    } else {
        Box(
            modifier = modifier
                .background(fallbackColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appName.take(1).uppercase(),
                color = fallbackColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize
            )
        }
    }
}
