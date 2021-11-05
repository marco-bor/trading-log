package net.bortolan.tradinglog.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.bortolan.tradinglog.equalsTo
import net.bortolan.tradinglog.format
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.util.*

typealias Symbol = String

data class Asset(var qty: BigDecimal, var symbol: Symbol) {
    override fun equals(other: Any?): Boolean {
        if (other is Asset) {
            return symbol == other.symbol && qty equalsTo other.qty
        }
        return false
    }

    override fun hashCode(): Int {
        var result = qty.hashCode()
        result = 31 * result + symbol.hashCode()
        return result
    }
}

enum class TransactionType { BUY, SELL, CONVERT, DEPOSIT, WITHDRAWAL }

@Entity
data class Transaction(
    @PrimaryKey
    var date: Date,
    @Embedded(prefix = "source_")
    var source: Asset? = null,
    @Embedded(prefix = "target_")
    var target: Asset? = null,
    @Embedded(prefix = "fee_")
    var fee: Asset? = null
) {
    fun getType(baseCurrency: Symbol = "EUR"): TransactionType = when {
        source == null && target != null -> TransactionType.DEPOSIT
        source != null && target == null -> TransactionType.WITHDRAWAL
        source != null && target != null -> {
            when {
                source!!.symbol == baseCurrency -> TransactionType.BUY
                target!!.symbol == baseCurrency -> TransactionType.SELL
                else -> TransactionType.CONVERT
            }

        }
        else -> throw IllegalArgumentException("invalid transaction type")
    }

    fun depositOrWithdrawal() = source == null || target == null
}
