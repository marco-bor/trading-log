package net.bortolan.tradinglog.parsers

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import net.bortolan.tradinglog.db.Asset
import net.bortolan.tradinglog.db.Transaction
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


object Parsers {
    private val binanceDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val coinbaseDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    enum class Type { BINANCE_REPORT, BINANCE_WITHDRAWALS, BINANCE_DEPOSITS, OTHER }

    private val binanceReportFirstLine = "UTC_Time,Operation,Coin,Change,Remark".split(",")
    private val binanceWithdrawalsDepositsFirstLine =
        "Date(UTC),Coin,Amount,Status,Payment Method,Indicated Amount,Fee,Order ID".split(',')

    fun getType(input: String): Type {
        if (input.isEmpty()) return Type.OTHER

        val firstLine = input.lineSequence().first().split(',')
        return when {
            firstLine.containsAll(binanceReportFirstLine) -> Type.BINANCE_REPORT
            firstLine.containsAll(binanceWithdrawalsDepositsFirstLine) -> {
                if (firstLine.size == 1) return Type.OTHER
                val paymentMethod = firstLine[1].split(",")[4].trim()
                if (paymentMethod.isEmpty()) return Type.BINANCE_DEPOSITS
                return Type.BINANCE_WITHDRAWALS
            }
            else -> Type.OTHER
        }
    }

    fun binance(input: String): List<Transaction> {
        val firstLine = input.lineSequence().first().split(',')
        val dateIndex = firstLine.indexOf(binanceReportFirstLine[0])
        val opIndex = firstLine.indexOf(binanceReportFirstLine[1])
        val coinIndex = firstLine.indexOf(binanceReportFirstLine[2])
        val changeIndex = firstLine.indexOf(binanceReportFirstLine[3])
        val csv = csvReader().readAll(input)
        val rows = csv
            .drop(1 /*drop header*/)
            .map {
                BinanceRow(
                    date = binanceDateFormat.parse(it[dateIndex])!!,
                    account = "Binance",
                    operation = it[opIndex],
                    coin = it[coinIndex],
                    change = it[changeIndex].toBigDecimal()
                )
            }
        return rows
            .filter { it.operation != "Deposit" }
            .groupBy { it.date }
            .map { entry ->
                Transaction(
                    entry.key,
                    source = entry.value.firstOrNull { it.operation != "Fee" && it.change < BigDecimal.ZERO }
                        ?.let { Asset(it.change, it.coin) },
                    target = entry.value.firstOrNull { it.operation != "Fee" && it.change > BigDecimal.ZERO }
                        ?.let { Asset(it.change, it.coin) },
                    fee = entry.value.firstOrNull() { it.operation == "Fee" }
                        ?.let { Asset(it.change, it.coin) }
                )
            }/*deposits*/ /*rows.filter { it.operation == "Deposit" }
            .map {
                Transaction(
                    Date(it.date.time - 1),
                    source = null,
                    target = Asset(it.change, it.coin)
                )
            } +*/ /*operations*/

    }

    fun binance1(input: String, amountPositive: Boolean): List<Transaction> {
        val csv = csvReader().readAll(input)

        val rows = csv.drop(1).map {
            BinanceRow1(
                binanceDateFormat.parse(it[0])!!,
                it[1],
                it[2].toBigDecimal() * (if (amountPositive) BigDecimal.ONE else -BigDecimal.ONE),
                it[3] == "Successful",
                it[5].toBigDecimal() * (if (amountPositive) BigDecimal.ONE else -BigDecimal.ONE),
                -it[6].toBigDecimal()
            )
        }

        return rows
            .filter { it.successful }
            .map {
                Transaction(
                    Date(it.date.time - 1),
                    source = if (amountPositive) null else Asset(it.amount, it.coin),
                    target = if (amountPositive) Asset(it.total, it.coin) else null,
                    fee = Asset(it.fee, it.coin)
                )
            }
    }

    fun coinbase(badinput: String): List<Transaction> {
        val input = badinput.lines().drop(7).joinToString("\n")
        val csv = csvReader().readAll(input)

        val headers = csv[0]
        val baseCurr = headers[4].substringBefore(" ")
        val rows = csv
            .drop(1)
            .map {
                CoinbaseRow(
                    coinbaseDateFormat.parse(it[0])!!,
                    it[1],
                    it[2],
                    baseCurr,
                    it[3].toBigDecimal(),
                    it[4].toBigDecimal(),
                    it[5].toBigDecimal(),
                    it[6].toBigDecimal(),
                    it[7].toBigDecimal(),
                    it[8].trim('"')
                )
            }

        return rows.map {
            when (it.transactionType) {
                "Buy" -> {
                    Transaction(
                        it.date,
                        Asset(-it.subtotal, baseCurr),
                        Asset(it.quantity, it.asset),
                        Asset(-it.fees, baseCurr)
                    )
                }
                "Sell" -> {
                    Transaction(
                        it.date,
                        Asset(-it.quantity, it.asset),
                        Asset(it.subtotal, baseCurr),
                        Asset(-it.fees, baseCurr)
                    )
                }
                "Convert" -> {
                    val descParts = it.description.split(" ")
                    Transaction(
                        it.date,
                        Asset(-descParts[1].replace(",", ".").toBigDecimal(), descParts[2]),
                        Asset(descParts[4].replace(",", ".").toBigDecimal(), descParts[5])
                    )
                }
                "Coinbase Earn" -> {
                    Transaction(it.date, null, Asset(it.quantity, it.asset))
                }
                else -> TODO("Type ${it.transactionType} not implemented")
            }
        }
    }
}

/**
 * Data for binance reports
 */
data class BinanceRow(
    val date: Date,
    val account: String,
    val operation: String,
    val coin: String,
    val change: BigDecimal
)

/**
 * Data for binance withdrawals and deposits
 */
data class BinanceRow1(
    val date: Date,
    val coin: String,
    val amount: BigDecimal,
    val successful: Boolean,
    val total: BigDecimal,
    val fee: BigDecimal
)

data class CoinbaseRow(
    val date: Date,
    val transactionType: String,
    val asset: String,
    val baseCurrency: String,
    val quantity: BigDecimal,
    val spotPrice: BigDecimal,
    val subtotal: BigDecimal,
    val total: BigDecimal,
    val fees: BigDecimal,
    val description: String,
)