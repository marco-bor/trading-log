package net.bortolan.tradinglog.ui.components

import android.text.format.DateUtils
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.bortolan.tradinglog.db.Symbol
import net.bortolan.tradinglog.db.Transaction
import net.bortolan.tradinglog.db.TransactionType
import net.bortolan.tradinglog.format
import net.bortolan.tradinglog.ui.theme.arrow
import net.bortolan.tradinglog.ui.theme.green
import net.bortolan.tradinglog.ui.theme.lightChip
import net.bortolan.tradinglog.ui.theme.red
import java.math.BigDecimal


@Composable
fun TransactionRow(transaction: Transaction, modifier: Modifier = Modifier) {
    val type = transaction.getType("EUR")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val context = LocalContext.current
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LightChip(type.name, MaterialTheme.colors.lightChip(type))

            Text(
                DateUtils.formatDateTime(
                    context,
                    transaction.date.time,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_WEEKDAY
                ),
                modifier = Modifier.alpha(.5f),
                style = TextStyle(fontSize = 12.sp)
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stroke = MaterialTheme.colors.arrow
            when (type) {
                TransactionType.DEPOSIT -> {
                    UIPair(
                        transaction.target!!.symbol to transaction.target!!.qty
                    )
                }
                TransactionType.WITHDRAWAL -> {
                    UIPair(
                        transaction.source!!.symbol to transaction.source!!.qty.add(
                            transaction.fee?.qty ?: BigDecimal.ZERO
                        ).abs()
                    )
                }
                TransactionType.BUY -> {
                    val subtotal = transaction.source!!.qty
                    val fees =
                        if (transaction.fee?.symbol == transaction.source?.symbol)
                            transaction.fee?.qty ?: BigDecimal.ZERO
                        else BigDecimal.ZERO

                    UIPair(transaction.target!!.symbol to transaction.target!!.qty)
                    Arrow(color = stroke, modifier = Modifier.weight(1f), inverse = true)
                    UIPair(transaction.source!!.symbol to (subtotal + fees).abs())
                }
                TransactionType.SELL -> {
                    val subtotal = transaction.target!!.qty
                    val fees =
                        if (transaction.fee?.symbol == transaction.source?.symbol)
                            transaction.fee?.qty ?: BigDecimal.ZERO
                        else BigDecimal.ZERO
                    UIPair(transaction.source!!.symbol to transaction.source!!.qty.abs())
                    Arrow(color = stroke, modifier = Modifier.weight(1f))
                    UIPair(transaction.target!!.symbol to (subtotal + fees).abs())
                }
                TransactionType.CONVERT -> {
                    UIPair(transaction.source!!.symbol to transaction.source!!.qty.abs())
                    Arrow(color = stroke, modifier = Modifier.weight(1f))
                    UIPair(transaction.target!!.symbol to transaction.target!!.qty)
                }
            }
        }

        /*
        TODO expand row on click:
        fees, estimated price on BUY/SELL
         */
    }
}

@Composable
fun UIPair(pair: Pair<Symbol, BigDecimal>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(pair.first, style = MaterialTheme.typography.body1, fontWeight = FontWeight.W700)
        Text(pair.second.format(), style = MaterialTheme.typography.body2)
    }
}

@Composable
fun UIReportPair(pair: Pair<Symbol, BigDecimal>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            Icons.Default.KeyboardArrowUp,
            "",
            tint = if (pair.second > BigDecimal.ZERO) MaterialTheme.colors.green else MaterialTheme.colors.red,
            modifier = Modifier.rotate(if (pair.second > BigDecimal.ZERO) 0f else 180f)
        )
        Text(pair.first, style = MaterialTheme.typography.body1, fontWeight = FontWeight.W700)
        Text(pair.second.format(), style = MaterialTheme.typography.body2)
    }
}
