package net.bortolan.tradinglog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import net.bortolan.tradinglog.db.TradingDatabase
import net.bortolan.tradinglog.db.Transaction
import net.bortolan.tradinglog.parsers.Parsers
import net.bortolan.tradinglog.ui.components.*
import net.bortolan.tradinglog.ui.theme.TradingLogTheme
import java.io.InputStreamReader
import java.math.BigDecimal
import java.util.*


class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    private val binanceImport =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val inputStream =
                contentResolver.openInputStream(uri ?: return@registerForActivityResult)
                    ?: return@registerForActivityResult

            val mimeType = contentResolver.getType(uri).orEmpty()
            if (mimeType != "text/comma-separated-values") {
                Toast.makeText(this, "File not supported", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val input = InputStreamReader(inputStream).use { it.readText() }
            when (Parsers.getType(input)) {
                Parsers.Type.BINANCE_REPORT -> viewModel.import(Parsers.binance(input))
                Parsers.Type.BINANCE_WITHDRAWALS -> viewModel.import(
                    Parsers.binance1(
                        input,
                        amountPositive = false
                    )
                )
                Parsers.Type.BINANCE_DEPOSITS -> viewModel.import(
                    Parsers.binance1(
                        input,
                        amountPositive = true
                    )
                )
                Parsers.Type.OTHER -> Toast.makeText(this, "File not supported", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val coinbaseImport =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val inputStream =
                contentResolver.openInputStream(uri ?: return@registerForActivityResult)
                    ?: return@registerForActivityResult

            val mimeType = contentResolver.getType(uri).orEmpty()
            if (mimeType != "text/comma-separated-values") {
                Toast.makeText(this, "File not supported", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            viewModel.import(Parsers.coinbase(InputStreamReader(inputStream).use { it.readText() }))
        }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val transactions by viewModel.transactions.observeAsState(emptyList())
            val portfolio by Transformations.map(viewModel.transactions) {
                Portfolio.portfolio(it).toList()
            }.observeAsState(emptyList())
            val report by Transformations.map(viewModel.transactions) {
                Portfolio.portfolio(
                    it.filter { transaction -> !transaction.depositOrWithdrawal() },
                    from = Date(2021 - 1900, 0, 1)
                ).toList()
            }.observeAsState(emptyList())
            TradingLogTheme {
                val listState = rememberLazyListState()
                val showButton by remember {
                    derivedStateOf {
                        listState
                    }
                }
                Scaffold(
                    /*topBar = {
                        TopAppBar(title = { Text(text = "Trading Log") })
                    },*/
                    floatingActionButton = {
                        val context = LocalContext.current

                        FloatingActionButton(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        context,
                                        NewTransaction::class.java
                                    )
                                )
                            },
                            backgroundColor = MaterialTheme.colors.primary,
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "add"
                            )
                        }
                    }
                ) {
                    LazyColumn(
                        Modifier.fillMaxHeight(),
                        contentPadding = PaddingValues(vertical = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        state = listState
                    ) {

                        item {
                            Header(
                                "Portfolio",
                                Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        item {
                            FlatCard(Modifier.padding(horizontal = 16.dp)) {
                                FlexibleColumn(portfolio.filter { it.second notEqualTo BigDecimal.ZERO }) {
                                    UIPair(it)
                                }
                            }
                        }

                        item {
                            Header(
                                "2021 to date",
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 16.dp)
                            )
                        }

                        item {
                            FlatCard(Modifier.padding(horizontal = 16.dp)) {
                                FlexibleColumn(report.filter { it.second notEqualTo BigDecimal.ZERO }) {
                                    UIReportPair(it)
                                }
                            }
                        }

                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Header("Latest Transactions")
                                /*Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "",
                                    Modifier.rotate(180f)
                                )*/
                            }
                        }

                        items(transactions/*.take(5)*/) {
                            TransactionRow(it, Modifier.padding(horizontal = 16.dp))
                        }

                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Button(onClick = {
                                        binanceImport.launch("*/*")
                                    }) {
                                        Text("Import from Binance")
                                    }
                                }
                                item {
                                    OutlinedButton(onClick = {
                                        coinbaseImport.launch("*/*")
                                    }) {
                                        Text("Import from Coinbase")
                                    }
                                }
                            }
                        }
                    }

                }

            }
        }
    }
}

class MainViewModel : ViewModel() {
    fun import(list: List<Transaction>) {
        dao.insertAll(list)
    }

    private val dao = TradingDatabase.instance.transactionsDao()
    val transactions = dao.getAll()

}

@Composable
fun Header(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.h5,
        modifier = modifier
    )
}
