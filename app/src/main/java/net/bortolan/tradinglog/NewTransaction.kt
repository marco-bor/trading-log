package net.bortolan.tradinglog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.bortolan.tradinglog.db.Asset
import net.bortolan.tradinglog.db.TradingDatabase
import net.bortolan.tradinglog.db.Transaction
import net.bortolan.tradinglog.db.TransactionType
import net.bortolan.tradinglog.ui.theme.TradingLogTheme
import java.util.*

class NewTransaction : ComponentActivity() {
    private val viewModel by viewModels<NewTransactionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TradingLogTheme {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                try {
                                    viewModel.create()
                                    finish()
                                } catch (e: IllegalStateException) {
                                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                                }
                            },
                            backgroundColor = MaterialTheme.colors.primary
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "add"
                            )
                        }
                    }
                ) {
                    Column {
                        val type by viewModel.type.observeAsState(TransactionType.BUY)
                        ScrollableTabRow(selectedTabIndex = type.ordinal) {
                            TransactionType.values().forEach {
                                Tab(
                                    text = { Text(it.name) },
                                    selected = it == type,
                                    onClick = { viewModel.onTabChanged(it) })
                            }
                        }
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (type) {
                                TransactionType.BUY -> {
                                }
                                TransactionType.SELL -> {

                                }
                                TransactionType.CONVERT -> {
                                }
                                TransactionType.DEPOSIT -> {
                                    AssetInput(
                                        "Total amount",
                                        viewModel.toAmount,
                                        viewModel.toSymbol,
                                        required = true,
                                        canEditSymbol = false
                                    )
                                    AssetInput(
                                        "Fees", viewModel.feesAmount, viewModel.feesSymbol,
                                        canEditSymbol = false
                                    )

                                }
                                TransactionType.WITHDRAWAL -> {
                                    AssetInput(
                                        "Total amount",
                                        viewModel.fromAmount,
                                        viewModel.fromSymbol,
                                        required = true,
                                        canEditSymbol = false
                                    )
                                    AssetInput(
                                        "Fees", viewModel.feesAmount, viewModel.feesSymbol,
                                        canEditSymbol = false
                                    )

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

class NewTransactionViewModel : ViewModel() {
    fun createSoft(): Transaction? {
        val source = fromAmount.value?.toBigDecimalOrNull()?.let { Asset(it, fromSymbol.value!!) }
        val target = toAmount.value?.toBigDecimalOrNull()?.let { Asset(it, toSymbol.value!!) }
        if (source == null && target == null) return null

        val fees = feesAmount.value?.toBigDecimalOrNull()?.let { Asset(it, feesSymbol.value!!) }

        //check(source.qty <= 0f) { "Source asset must be negative" }
        //check(target.qty >= 0f) { "Target asset must be positive" }
        //check(fees?.qty ?: 0f <= 0f) { "Fees must be negative" }
        return Transaction(Date(), source, target, fees)
    }

    fun create() {
        TradingDatabase.instance.transactionsDao()
            .insert(createSoft()!!)
    }

    fun onTabChanged(_type: TransactionType) {
        type.value = _type

        when (_type) {
            TransactionType.BUY -> {
            }
            TransactionType.SELL -> {
            }
            TransactionType.CONVERT -> {
            }
            TransactionType.DEPOSIT -> {
                toSymbol.value = "EUR"
                feesSymbol.value = "EUR"
            }
            TransactionType.WITHDRAWAL -> {
                fromSymbol.value = "EUR"
                feesSymbol.value = "EUR"
            }
        }
    }

    val type = MutableLiveData(TransactionType.BUY)
    val fromSymbol = MutableLiveData("")
    val fromAmount = MutableLiveData("")
    val toSymbol = MutableLiveData("")
    val toAmount = MutableLiveData("")
    val feesSymbol = MutableLiveData("")
    val feesAmount = MutableLiveData("")
}

@Composable
fun AssetInput(
    header: String,
    amount: MutableLiveData<String>,
    symbol: MutableLiveData<String>,
    required: Boolean = false,
    canEditSymbol: Boolean = true
) {
    val currSymbol by symbol.observeAsState("")
    val currAmount by amount.observeAsState("")
    Column {
        Row {
            Text(header, style = MaterialTheme.typography.h6)
            /*if (required) {
                Text("(required)", style = MaterialTheme.typography.caption)
            }*/
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = currSymbol,
                onValueChange = { symbol.value = it },
                modifier = Modifier.weight(1f),
                label = { Text("Symbol") },
                //isError = if (required) currSymbol.isEmpty() else currAmount.isNotEmpty() and currSymbol.isEmpty(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Characters,
                ),
                enabled = canEditSymbol
            )
            OutlinedTextField(
                value = currAmount,
                onValueChange = { amount.value = it },
                modifier = Modifier.weight(1f),
                label = { Text("Amount") },
                //isError = if (required) currAmount.isEmpty() else currSymbol.isNotEmpty() and currAmount.isEmpty(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
        }
    }
}
