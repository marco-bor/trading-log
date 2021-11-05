package net.bortolan.tradinglog

import java.math.BigDecimal
import java.text.NumberFormat

private val numberFormat = NumberFormat.getNumberInstance()

fun BigDecimal.format(): String {
    return numberFormat.format(this)
}

infix fun BigDecimal.equalsTo(other: BigDecimal): Boolean{
    return compareTo(other) == 0
}

infix fun BigDecimal.notEqualTo(other: BigDecimal): Boolean{
    return compareTo(other) != 0
}