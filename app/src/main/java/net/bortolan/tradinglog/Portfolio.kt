package net.bortolan.tradinglog

import net.bortolan.tradinglog.db.Asset
import net.bortolan.tradinglog.db.Symbol
import net.bortolan.tradinglog.db.Transaction
import java.math.BigDecimal
import java.util.*

object Portfolio {
    fun portfolio(
        transactions: List<Transaction>,
        from: Date = Date(0),
        to: Date = Date()
    ): Map<Symbol, BigDecimal> {
        val portfolio = mutableMapOf<Symbol, BigDecimal>()
        transactions
            .filter { it.date in from..to }
            .forEach {
                it.source?.let { it1 -> findAndSum(portfolio, it1) }
                it.target?.let { it1 -> findAndSum(portfolio, it1) }
                it.fee?.let { it1 -> findAndSum(portfolio, it1) }
            }
        return portfolio
    }

    private fun findAndSum(map: MutableMap<Symbol, BigDecimal>, asset: Asset) {
        map[asset.symbol] = map[asset.symbol]?.plus(asset.qty) ?: asset.qty
    }
}