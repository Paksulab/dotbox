package com.dotbox.app.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.launch
import java.util.UUID

private enum class GeneratorTab(val label: String) {
    DICE("Dice"),
    COIN("Coin"),
    NUMBER("Number"),
    PASSWORD("Password"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RandomGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableStateOf(GeneratorTab.DICE) }

    // Dice state
    var diceCount by rememberSaveable { mutableIntStateOf(1) }
    var diceResults by rememberSaveable { mutableStateOf(listOf(1)) }

    // Coin state
    var coinResult by rememberSaveable { mutableStateOf("Tap to flip!") }

    // Number state
    var numberMin by rememberSaveable { mutableStateOf("1") }
    var numberMax by rememberSaveable { mutableStateOf("100") }
    var numberResult by rememberSaveable { mutableStateOf("—") }

    // Password state
    var passwordLength by rememberSaveable { mutableFloatStateOf(16f) }
    var useUppercase by rememberSaveable { mutableStateOf(true) }
    var useLowercase by rememberSaveable { mutableStateOf(true) }
    var useDigits by rememberSaveable { mutableStateOf(true) }
    var useSymbols by rememberSaveable { mutableStateOf(true) }
    var passwordResult by rememberSaveable { mutableStateOf("") }

    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    fun animateBounce() {
        scope.launch {
            scale.animateTo(1.15f, spring(stiffness = Spring.StiffnessHigh))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    fun rollDice() {
        diceResults = (1..diceCount).map { (1..6).random() }
        animateBounce()
    }

    fun flipCoin() {
        coinResult = if ((0..1).random() == 0) "Heads" else "Tails"
        animateBounce()
    }

    fun generateNumber() {
        val min = numberMin.toLongOrNull() ?: 1
        val max = numberMax.toLongOrNull() ?: 100
        numberResult = if (min <= max) (min..max).random().toString() else "Invalid range"
        animateBounce()
    }

    fun generatePassword() {
        val chars = buildString {
            if (useUppercase) append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
            if (useLowercase) append("abcdefghijklmnopqrstuvwxyz")
            if (useDigits) append("0123456789")
            if (useSymbols) append("!@#\$%^&*()_+-=[]{}|;:',.<>?")
        }
        passwordResult = if (chars.isEmpty()) {
            "Select at least one option"
        } else {
            (1..passwordLength.toInt()).map { chars.random() }.joinToString("")
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Generated", text))
    }

    ToolScreenScaffold(title = "Random Generator", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Tab selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GeneratorTab.entries.forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.label) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTab) {
                GeneratorTab.DICE -> {
                    // Dice count selector
                    Text(
                        text = "Number of dice: $diceCount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Slider(
                        value = diceCount.toFloat(),
                        onValueChange = { diceCount = it.toInt() },
                        valueRange = 1f..6f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.tertiary,
                            activeTrackColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dice results
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale.value),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        diceResults.forEach { value ->
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = value.toString(),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (diceResults.size > 1) {
                        Text(
                            text = "Total: ${diceResults.sum()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { rollDice() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Roll", style = MaterialTheme.typography.titleMedium)
                    }
                }

                GeneratorTab.COIN -> {
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(scale.value)
                            .clip(RoundedCornerShape(80.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (coinResult == "Heads") "H" else if (coinResult == "Tails") "T" else "?",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = coinResult,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { flipCoin() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Flip Coin", style = MaterialTheme.typography.titleMedium)
                    }
                }

                GeneratorTab.NUMBER -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = numberMin,
                            onValueChange = { numberMin = it.filter { c -> c.isDigit() || c == '-' } },
                            modifier = Modifier.weight(1f),
                            label = { Text("Min") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                                cursorColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                        OutlinedTextField(
                            value = numberMax,
                            onValueChange = { numberMax = it.filter { c -> c.isDigit() || c == '-' } },
                            modifier = Modifier.weight(1f),
                            label = { Text("Max") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                                cursorColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = numberResult,
                        modifier = Modifier.scale(scale.value),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.tertiary,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { generateNumber() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Generate", style = MaterialTheme.typography.titleMedium)
                    }
                }

                GeneratorTab.PASSWORD -> {
                    Text(
                        text = "Length: ${passwordLength.toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Slider(
                        value = passwordLength,
                        onValueChange = { passwordLength = it },
                        valueRange = 4f..64f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.tertiary,
                            activeTrackColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = useUppercase,
                            onClick = { useUppercase = !useUppercase },
                            label = { Text("A-Z") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                        FilterChip(
                            selected = useLowercase,
                            onClick = { useLowercase = !useLowercase },
                            label = { Text("a-z") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                        FilterChip(
                            selected = useDigits,
                            onClick = { useDigits = !useDigits },
                            label = { Text("0-9") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                        FilterChip(
                            selected = useSymbols,
                            onClick = { useSymbols = !useSymbols },
                            label = { Text("!@#") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (passwordResult.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = passwordResult,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { copyToClipboard(passwordResult) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { generatePassword() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Generate Password", style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // UUID generator
                    Button(
                        onClick = {
                            passwordResult = UUID.randomUUID().toString()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text("Generate UUID", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
