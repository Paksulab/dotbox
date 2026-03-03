package com.dotbox.app.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

private data class Currency(val code: String, val name: String, val symbol: String)

private val currencies = listOf(
    Currency("USD", "US Dollar", "$"),
    Currency("EUR", "Euro", "€"),
    Currency("GBP", "British Pound", "£"),
    Currency("JPY", "Japanese Yen", "¥"),
    Currency("CAD", "Canadian Dollar", "C$"),
    Currency("AUD", "Australian Dollar", "A$"),
    Currency("CHF", "Swiss Franc", "CHF"),
    Currency("CNY", "Chinese Yuan", "¥"),
    Currency("INR", "Indian Rupee", "₹"),
    Currency("KRW", "South Korean Won", "₩"),
    Currency("BRL", "Brazilian Real", "R$"),
    Currency("MXN", "Mexican Peso", "MX$"),
    Currency("SEK", "Swedish Krona", "kr"),
    Currency("NOK", "Norwegian Krone", "kr"),
    Currency("TRY", "Turkish Lira", "₺"),
    Currency("PLN", "Polish Zloty", "zł"),
    Currency("HUF", "Hungarian Forint", "Ft"),
    Currency("CZK", "Czech Koruna", "Kč"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(onBack: () -> Unit) {
    var fromCurrencyCode by rememberSaveable { mutableStateOf(currencies[0].code) }
    var toCurrencyCode by rememberSaveable { mutableStateOf(currencies[1].code) }
    val fromCurrency = currencies.find { it.code == fromCurrencyCode } ?: currencies[0]
    val toCurrency = currencies.find { it.code == toCurrencyCode } ?: currencies[1]
    var amount by rememberSaveable { mutableStateOf("1") }
    var rates by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var lastUpdated by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun fetchRates(base: String) {
        scope.launch {
            isLoading = true
            errorMsg = null
            try {
                val result = withContext(Dispatchers.IO) {
                    val json = URL("https://open.er-api.com/v6/latest/$base").readText()
                    val jsonObj = JSONObject(json)
                    if (jsonObj.getString("result") == "success") {
                        val ratesObj = jsonObj.getJSONObject("rates")
                        val map = mutableMapOf<String, Double>()
                        ratesObj.keys().forEach { key -> map[key] = ratesObj.getDouble(key) }
                        map to jsonObj.optString("time_last_update_utc", "")
                    } else {
                        null
                    }
                }
                if (result != null) {
                    rates = result.first
                    lastUpdated = result.second.take(16) // Trim to just date+time
                } else {
                    errorMsg = "Failed to fetch rates"
                }
            } catch (e: Exception) {
                errorMsg = "Network error. Check connection."
            }
            isLoading = false
        }
    }

    // Fetch rates on first load
    LaunchedEffect(fromCurrency.code) {
        if (rates.isEmpty() || !rates.containsKey(toCurrency.code)) {
            fetchRates(fromCurrency.code)
        }
    }

    val inputAmount = amount.toDoubleOrNull() ?: 0.0
    val rate = rates[toCurrency.code] ?: 0.0
    val convertedAmount = inputAmount * rate

    ToolScreenScaffold(title = "Currency Converter", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // From currency
            CurrencySelector(
                label = "FROM",
                currency = fromCurrency,
                currencies = currencies,
                onCurrencySelected = {
                    fromCurrencyCode = it.code
                    rates = emptyMap() // Force refetch
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Amount input
            OutlinedTextField(
                value = amount,
                onValueChange = { newVal ->
                    if (newVal.isEmpty() || newVal.matches("^\\d*\\.?\\d*$".toRegex())) {
                        amount = newVal
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMono),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Swap button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    onClick = {
                        val temp = fromCurrencyCode
                        fromCurrencyCode = toCurrencyCode
                        toCurrencyCode = temp
                        rates = emptyMap()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap currencies",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp),
                    )
                }
                if (rate > 0) {
                    Text(
                        text = "1 ${fromCurrency.code} = ${"%.4f".format(rate)} ${toCurrency.code}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // To currency
            CurrencySelector(
                label = "TO",
                currency = toCurrency,
                currencies = currencies,
                onCurrencySelected = { toCurrencyCode = it.code },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Result
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        strokeWidth = 3.dp,
                    )
                } else if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Text(
                        text = "${toCurrency.symbol} ${"%.2f".format(convertedAmount)}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${toCurrency.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Refresh & last updated
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (lastUpdated.isNotEmpty()) {
                    Text(
                        text = "Updated: $lastUpdated",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                IconButton(onClick = { fetchRates(fromCurrency.code) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh rates",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySelector(
    label: String,
    currency: Currency,
    currencies: List<Currency>,
    onCurrencySelected: (Currency) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = "${currency.symbol}  ${currency.code} — ${currency.name}",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                ),
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                currencies.forEach { c ->
                    DropdownMenuItem(
                        text = { Text("${c.symbol}  ${c.code} — ${c.name}") },
                        onClick = {
                            onCurrencySelected(c)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
