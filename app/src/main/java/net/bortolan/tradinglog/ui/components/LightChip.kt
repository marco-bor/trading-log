package net.bortolan.tradinglog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun LightChip(text: String, color: Color) {
    Text(
        text,
        style = TextStyle(color, fontSize = 12.sp, fontWeight = FontWeight.W500),
        modifier = Modifier
            .background(color.copy(.15f), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}