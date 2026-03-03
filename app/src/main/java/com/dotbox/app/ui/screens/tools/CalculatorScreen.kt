package com.dotbox.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import com.dotbox.app.ui.theme.NothingRed
import java.text.DecimalFormat
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    var display by rememberSaveable { mutableStateOf("0") }
    var currentNumber by rememberSaveable { mutableStateOf("") }
    var previousNumber by rememberSaveable { mutableStateOf("") }
    var operation by rememberSaveable { mutableStateOf("") }
    var shouldResetDisplay by rememberSaveable { mutableStateOf(false) }
    var expression by rememberSaveable { mutableStateOf("") }

    fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble() && !value.isInfinite() && !value.isNaN()) {
            value.toLong().toString()
        } else {
            val df = DecimalFormat("#.##########")
            df.format(value)
        }
    }

    fun calculate(): Double? {
        val prev = previousNumber.toDoubleOrNull() ?: return null
        val curr = currentNumber.toDoubleOrNull() ?: display.toDoubleOrNull() ?: return null
        return when (operation) {
            "+" -> prev + curr
            "-" -> prev - curr
            "×" -> prev * curr
            "÷" -> if (curr != 0.0) prev / curr else Double.NaN
            "^" -> prev.pow(curr)
            else -> null
        }
    }

    fun onNumber(num: String) {
        if (shouldResetDisplay) {
            display = num
            currentNumber = num
            shouldResetDisplay = false
        } else {
            if (display == "0" && num != ".") {
                display = num
            } else if (num == "." && display.contains(".")) {
                return
            } else {
                display += num
            }
            currentNumber = display
        }
    }

    fun onOperator(op: String) {
        if (operation.isNotEmpty() && currentNumber.isNotEmpty()) {
            val result = calculate()
            if (result != null) {
                display = formatNumber(result)
                previousNumber = display
                expression = "$display $op"
            }
        } else {
            previousNumber = display
            expression = "$display $op"
        }
        operation = op
        currentNumber = ""
        shouldResetDisplay = true
    }

    fun onEquals() {
        if (operation.isNotEmpty()) {
            val result = calculate()
            if (result != null) {
                display = if (result.isNaN()) "Error" else formatNumber(result)
                expression = ""
                operation = ""
                previousNumber = ""
                currentNumber = display
                shouldResetDisplay = true
            }
        }
    }

    fun onClear() {
        display = "0"
        currentNumber = ""
        previousNumber = ""
        operation = ""
        expression = ""
        shouldResetDisplay = false
    }

    fun onBackspace() {
        if (display.length > 1) {
            display = display.dropLast(1)
            currentNumber = display
        } else {
            display = "0"
            currentNumber = ""
        }
    }

    fun onScientific(func: String) {
        val value = display.toDoubleOrNull() ?: return
        val result = when (func) {
            "sin" -> sin(Math.toRadians(value))
            "cos" -> cos(Math.toRadians(value))
            "tan" -> tan(Math.toRadians(value))
            "√" -> sqrt(value)
            "ln" -> ln(value)
            "%" -> value / 100.0
            "±" -> -value
            "x²" -> value.pow(2)
            else -> return
        }
        display = if (result.isNaN() || result.isInfinite()) "Error" else formatNumber(result)
        currentNumber = display
        shouldResetDisplay = true
    }

    fun onToggleSign() = onScientific("±")
    fun onPercent() = onScientific("%")

    ToolScreenScaffold(title = "Calculator", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            // Expression display
            Text(
                text = expression,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.End,
            )
            // Main display
            Text(
                text = display,
                style = MaterialTheme.typography.displayMedium.copy(fontFamily = JetBrainsMono),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                textAlign = TextAlign.End,
                maxLines = 2,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Scientific row
            CalcRow {
                SciFuncButton("sin", Modifier.weight(1f)) { onScientific("sin") }
                SciFuncButton("cos", Modifier.weight(1f)) { onScientific("cos") }
                SciFuncButton("tan", Modifier.weight(1f)) { onScientific("tan") }
                SciFuncButton("√", Modifier.weight(1f)) { onScientific("√") }
                SciFuncButton("ln", Modifier.weight(1f)) { onScientific("ln") }
            }
            Spacer(modifier = Modifier.height(4.dp))
            CalcRow {
                SciFuncButton("x²", Modifier.weight(1f)) { onScientific("x²") }
                SciFuncButton("xⁿ", Modifier.weight(1f)) { onOperator("^") }
                SciFuncButton("±", Modifier.weight(1f)) { onToggleSign() }
                SciFuncButton("%", Modifier.weight(1f)) { onPercent() }
                SciFuncButton("⌫", Modifier.weight(1f)) { onBackspace() }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main keypad
            CalcRow {
                CalcButton("C", Modifier.weight(1f), isAccent = true) { onClear() }
                CalcButton("(", Modifier.weight(1f)) { onNumber("(") }
                CalcButton(")", Modifier.weight(1f)) { onNumber(")") }
                CalcOperatorButton("÷", Modifier.weight(1f)) { onOperator("÷") }
            }
            Spacer(modifier = Modifier.height(4.dp))
            CalcRow {
                CalcButton("7", Modifier.weight(1f)) { onNumber("7") }
                CalcButton("8", Modifier.weight(1f)) { onNumber("8") }
                CalcButton("9", Modifier.weight(1f)) { onNumber("9") }
                CalcOperatorButton("×", Modifier.weight(1f)) { onOperator("×") }
            }
            Spacer(modifier = Modifier.height(4.dp))
            CalcRow {
                CalcButton("4", Modifier.weight(1f)) { onNumber("4") }
                CalcButton("5", Modifier.weight(1f)) { onNumber("5") }
                CalcButton("6", Modifier.weight(1f)) { onNumber("6") }
                CalcOperatorButton("-", Modifier.weight(1f)) { onOperator("-") }
            }
            Spacer(modifier = Modifier.height(4.dp))
            CalcRow {
                CalcButton("1", Modifier.weight(1f)) { onNumber("1") }
                CalcButton("2", Modifier.weight(1f)) { onNumber("2") }
                CalcButton("3", Modifier.weight(1f)) { onNumber("3") }
                CalcOperatorButton("+", Modifier.weight(1f)) { onOperator("+") }
            }
            Spacer(modifier = Modifier.height(4.dp))
            CalcRow {
                CalcButton("0", Modifier.weight(2f)) { onNumber("0") }
                CalcButton(".", Modifier.weight(1f)) { onNumber(".") }
                CalcEqualsButton("=", Modifier.weight(1f)) { onEquals() }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CalcRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = content,
    )
}

@Composable
private fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isAccent) {
                NothingRed.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isAccent) {
                NothingRed
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
        )
    }
}

@Composable
private fun CalcOperatorButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
        )
    }
}

@Composable
private fun CalcEqualsButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
        )
    }
}

@Composable
private fun SciFuncButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMono),
        )
    }
}
