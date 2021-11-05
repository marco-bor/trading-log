package net.bortolan.tradinglog.parsers

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import net.bortolan.tradinglog.db.Asset
import net.bortolan.tradinglog.db.Transaction
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class ParsersTest {

    @Test
    fun parse_binance_csv() {
        val csv = """
            UTC_Time,Account,Operation,Coin,Change,Remark
            2021-05-09 12:06:40,Spot,Fee,DOGE,-0.28570000,""
            2021-05-09 12:06:40,Spot,Buy,DOGE,285.70000000,""
            2021-05-09 12:06:40,Spot,Transaction Related,EUR,-99.99500000,""
            2021-05-19 11:30:56,Spot,Fee,DOGE,-0.37030000,""
            2021-05-19 11:30:56,Spot,Transaction Related,EUR,-99.98100000,""
            2021-05-19 11:30:56,Spot,Buy,DOGE,370.30000000,""
            2021-05-19 13:47:39,Spot,Buy,DOGE,375.60000000,""
            2021-05-19 13:47:39,Spot,Transaction Related,EUR,-99.98472000,""
            2021-05-19 13:47:39,Spot,Fee,DOGE,-0.37560000,""
            2021-05-19 13:48:59,Spot,Transaction Related,DOGE,-375.60000000,""
            2021-05-19 13:48:59,Spot,Fee,EUR,-0.10282426,""
            2021-05-19 13:48:59,Spot,Buy,EUR,102.82425600,""
        """.trimIndent()
        val transactions = Parsers.binance(csv).sortedBy { it.date }

        val expected = listOf(
            Transaction(
                Date(1620562000000),
                Asset((-99.995).toBigDecimal(), "EUR"),
                Asset(285.7.toBigDecimal(), "DOGE"),
                Asset((-0.2857).toBigDecimal(), "DOGE")
            ),
            Transaction(
                Date(1621423856000),
                Asset((-99.981).toBigDecimal(), "EUR"),
                Asset(370.3.toBigDecimal(), "DOGE"),
                Asset((-0.3703).toBigDecimal(), "DOGE")
            ),
            Transaction(
                Date(1621432059000),
                Asset((-99.98472).toBigDecimal(), "EUR"),
                Asset(375.6.toBigDecimal(), "DOGE"),
                Asset((-0.3756).toBigDecimal(), "DOGE")
            ),
            Transaction(
                Date(1621432139000),
                Asset((-375.6).toBigDecimal(), "DOGE"),
                Asset(102.82425600.toBigDecimal(), "EUR"),
                Asset((-0.10282426).toBigDecimal(), "EUR")
            )
        ).sortedBy { it.date }

        assertArrayEquals(expected.toTypedArray(), transactions.toTypedArray())
    }

    @Test
    fun parse_binance_csv_1() {
        val csv = """
            UTC_Time,Account,Operation,Coin,Change,Remark
            2021-02-07 10:06:33,Spot,Transaction Related,EUR,-98.10000000,""
            2021-02-07 10:06:33,Spot,Deposit,EUR,98.10000000,""
            2021-02-07 10:06:33,Spot,Transaction Related,DOGE,1884.90000000,""
            2021-02-07 10:21:51,Spot,Deposit,EUR,98.10000000,""
            2021-02-07 10:21:51,Spot,Transaction Related,EUR,-98.10000000,""
            2021-02-07 10:21:51,Spot,Transaction Related,XRP,262.80000000,""
            2021-02-08 17:22:07,Spot,Transaction Related,DOGE,-371.40000000,""
            2021-02-08 17:22:07,Spot,Transaction Related,EUR,24.00000000,""
            2021-02-11 16:28:12,Spot,Transaction Related,ADA,29.90000000,""
            2021-02-11 16:28:12,Spot,Transaction Related,EUR,-24.00000000,""
            2021-03-17 10:21:59,Spot,Transaction Related,ADA,-29.90000000,""
            2021-03-17 10:21:59,Spot,Transaction Related,EUR,31.27000000,""
            2021-04-20 09:18:48,Spot,Transaction Related,DOGE,-1513.50000000,""
            2021-04-20 09:18:48,Spot,Transaction Related,EUR,507.54000000,""
        """.trimIndent()
        val transactions = Parsers.binance(csv).sortedBy { it.date }
        val expected = listOf(
            /*Transaction(
                0,
                Date(1612692393000 - 1),
                null,
                Asset(98.1f, "EUR")
            ),*/
            Transaction(
                Date(1612692393000),
                Asset((-98.1).toBigDecimal(), "EUR"),
                Asset(1884.9.toBigDecimal(), "DOGE")
            ),
            /*Transaction(
                0,
                Date(1612693311000 - 1),
                null,
                Asset(98.1f, "EUR")
            ),*/
            Transaction(
                Date(1612693311000),
                Asset((-98.1).toBigDecimal(), "EUR"),
                Asset(262.8.toBigDecimal(), "XRP")
            ),
            Transaction(
                Date(1612804927000),
                Asset((-371.4).toBigDecimal(), "DOGE"),
                Asset(24.0.toBigDecimal(), "EUR")
            ),
            Transaction(
                Date(1613060892000),
                Asset((-24).toBigDecimal(), "EUR"),
                Asset(29.9.toBigDecimal(), "ADA")
            ),
            Transaction(
                Date(1615976519000),
                Asset((-29.9).toBigDecimal(), "ADA"),
                Asset(31.27.toBigDecimal(), "EUR")
            ),
            Transaction(
                Date(1618910328000),
                Asset((-1513.5).toBigDecimal(), "DOGE"),
                Asset(507.54.toBigDecimal(), "EUR")
            )
        )
        assertArrayEquals(expected.toTypedArray(), transactions.toTypedArray())

    }

    @Test
    fun parse_binance1_csv_withdrawals() {
        val csv = """
            Date(UTC),Coin,Amount,Status,Payment Method,Indicated Amount,Fee,Order ID
            2021-05-20 20:21:02,EUR,340.77,Successful,bank transfer,341.57,0.80,CJW712761827822473216
        """.trimIndent()

        val transactions = Parsers.binance1(csv, false).sortedBy { it.date }
        val expected = listOf(
            Transaction(
                Date(1621542062000 - 1),
                source = Asset((-340.77).toBigDecimal(), "EUR"),
                fee = Asset((-0.8).toBigDecimal(), "EUR")
            )
        )

        assertArrayEquals(expected.toTypedArray(), transactions.toTypedArray())
    }

    @Test
    fun parse_binance1_csv_deposits() {
        val csv = """
            Date(UTC),Coin,Amount,Status,Payment Method,Indicated Amount,Fee,Order ID
            2021-03-05 13:45:17,EUR,196.2,Failed, ,200,3.80,530cd256cabe41dbbb2c533bcad64228
            2021-02-07 10:21:25,EUR,98.1,Successful, ,100,1.90,55260b157111485ca0f3d7b624974e4e
            2021-02-07 10:06:07,EUR,98.1,Successful, ,100,1.90,6701084d0dc1410e96cfdc968a01ae91
        """.trimIndent()

        val transactions = Parsers.binance1(csv, true).sortedBy { it.date }
        val expected = listOf(
            Transaction(
                Date(1612692367000 - 1),
                target = Asset(100.toBigDecimal(), "EUR"),
                fee = Asset((-1.9).toBigDecimal(), "EUR")
            ),
            Transaction(
                Date(1612693285000 - 1),
                target = Asset(100.toBigDecimal(), "EUR"),
                fee = Asset((-1.9).toBigDecimal(), "EUR")
            )
        )

        assertArrayEquals(expected.toTypedArray(), transactions.toTypedArray())
    }

    @Test
    fun parse_binance_new_report() {
        val csv = """
            User_ID,UTC_Time,Account,Operation,Coin,Change,Remark
            75667306,2021-08-16 00:29:00,Spot,Transaction Related,XRP,-91.31741940,""
            75667306,2021-08-16 00:29:00,Spot,Transaction Related,EUR,100.00000000,""
        """.trimIndent()

        val transactions = Parsers.binance(csv).sortedBy { it.date }

        val expected = listOf(
            Transaction(
                Date(1629073740000),
                source = Asset((-91.31741940).toBigDecimal(), "XRP"),
                target = Asset(100.toBigDecimal(), "EUR")
            ),
        )

        assertArrayEquals(expected.toTypedArray(), transactions.toTypedArray())

    }

    @Test
    fun parse_coinbase_csv__buy() {
        val csv = """
            "You can use this transaction report to inform your likely tax obligations. For US customers, Sells, Converts, and Rewards Income, and Coinbase Earn transactions are taxable events. For final tax obligations, please consult your tax advisor."



            Transactions
            User,bortolanmarco@gmail.com,5e3ff839b72a821e8612fcec

            Timestamp,Transaction Type,Asset,Quantity Transacted,EUR Spot Price at Transaction,EUR Subtotal,EUR Total (inclusive of fees),EUR Fees,Notes
            2020-02-14T10:51:39Z,Buy,BTC,0.01006655,9552.43,96.16,100.00,3.84,"Bought 0,01006655 BTC for 100,00 € EUR"
        """.trimIndent()

        val transactions = Parsers.coinbase(csv)
            .sortedBy { it.date }

        val expected = listOf(
            Transaction(
                Date(1581677499000),
                source = Asset((-96.16).toBigDecimal(), "EUR"),
                target = Asset(0.01006655.toBigDecimal(), "BTC"),
                fee = Asset((-3.84).toBigDecimal(), "EUR"),
            ),
        )
        assertArrayEquals(expected.toTypedArray(), transactions.toTypedArray())
    }

    @Test
    fun parse_coinbase_csv__convert() {
        val csv = """
            "You can use this transaction report to inform your likely tax obligations. For US customers, Sells, Converts, and Rewards Income, and Coinbase Earn transactions are taxable events. For final tax obligations, please consult your tax advisor."



            Transactions
            User,bortolanmarco@gmail.com,5e3ff839b72a821e8612fcec

            Timestamp,Transaction Type,Asset,Quantity Transacted,EUR Spot Price at Transaction,EUR Subtotal,EUR Total (inclusive of fees),EUR Fees,Notes
            2020-02-16T16:52:48Z,Convert,BTC,0.002225,9015.73,19.86,20.06,0.200000,"Converted 0,002225 BTC to 318,3483088 XLM"
        """.trimIndent()

        val transactions = Parsers.coinbase(csv)
            .sortedBy { it.date }

        val expected = listOf(
            Transaction(
                Date(1581871968000),
                source = Asset((-0.002225).toBigDecimal(), "BTC"),
                target = Asset(318.3483088.toBigDecimal(), "XLM"),
            ),
        )
        assertArrayEquals(expected.toTypedArray(), transactions.toTypedArray())
    }

    @Test
    fun parse_coinbase_csv__sell() {
        val csv = """
            "You can use this transaction report to inform your likely tax obligations. For US customers, Sells, Converts, and Rewards Income, and Coinbase Earn transactions are taxable events. For final tax obligations, please consult your tax advisor."



            Transactions
            User,bortolanmarco@gmail.com,5e3ff839b72a821e8612fcec

            Timestamp,Transaction Type,Asset,Quantity Transacted,EUR Spot Price at Transaction,EUR Subtotal,EUR Total (inclusive of fees),EUR Fees,Notes
            2020-09-28T09:58:01Z,Sell,BTC,0.00346917,9284.64,32.21,30.22,1.99,"Sold 0,00346917 BTC for 30,22 € EUR"
        """.trimIndent()

        val transactions = Parsers.coinbase(csv)
            .sortedBy { it.date }

        val expected = listOf(
            Transaction(
                Date(1601287081000),
                source = Asset((-0.00346917).toBigDecimal(), "BTC"),
                target = Asset(32.21.toBigDecimal(), "EUR"),
                fee = Asset((-1.99).toBigDecimal(), "EUR"),
            ),
        )
        assertArrayEquals(expected.toTypedArray(), transactions.toTypedArray())
    }

    @Test
    fun parse_coinbase_csv__earn() {
        val csv = """
            "You can use this transaction report to inform your likely tax obligations. For US customers, Sells, Converts, and Rewards Income, and Coinbase Earn transactions are taxable events. For final tax obligations, please consult your tax advisor."



            Transactions
            User,bortolanmarco@gmail.com,5e3ff839b72a821e8612fcec

            Timestamp,Transaction Type,Asset,Quantity Transacted,EUR Spot Price at Transaction,EUR Subtotal,EUR Total (inclusive of fees),EUR Fees,Notes
            2021-02-06T12:23:58Z,Coinbase Earn,XLM,6.2766956,0.280000,1.76,1.76,0.00,"Received 6,2766956 XLM from Coinbase Earn"
        """.trimIndent()

        val transactions = Parsers.coinbase(csv)
            .sortedBy { it.date }

        val expected = listOf(
            Transaction(
                Date(1612614238000),
                target = Asset(6.2766956.toBigDecimal(), "XLM"),
            ),
        )
        assertArrayEquals(expected.toTypedArray(), transactions.toTypedArray())
    }


    @Test
    fun csv_string_escape() {
        val str = "Samsung A5 24\"\""
        assertArrayEquals(arrayOf("Samsung A5 24\""), csvReader().readAll(str)[0].toTypedArray())
    }
}