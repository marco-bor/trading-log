package net.bortolan.tradinglog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> FlexibleColumn(data: List<T>, content: @Composable (T) -> Unit) {
    if (data.size > 2) {
        // split in 2
        val half =
            if (data.size % 2 == 0) data.size / 2 else (data.size + 1) / 2

        Row(Modifier.padding(16.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                data.take(half).forEach { content(it) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                data.takeLast(data.size - half).forEach { content(it) }
            }
        }
    } else {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            data.forEach{ content(it) }
        }
    }
}