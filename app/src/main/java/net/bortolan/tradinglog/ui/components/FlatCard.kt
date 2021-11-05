package net.bortolan.tradinglog.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.bortolan.tradinglog.ui.theme.stroke

@Composable
fun FlatCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth().then(modifier),
        elevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colors.stroke)
    ) {
        content()
    }
}