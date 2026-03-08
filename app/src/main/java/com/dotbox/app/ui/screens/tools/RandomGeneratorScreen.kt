package com.dotbox.app.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.screens.settings.animationsEnabled
import com.dotbox.app.ui.screens.settings.hapticEnabled
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.ln
import kotlin.math.sqrt

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
    val view = LocalView.current
    val animEnabled = animationsEnabled(context)
    val hapticOn = hapticEnabled(context)
    var selectedTab by rememberSaveable { mutableStateOf(GeneratorTab.DICE) }

    // Dice state
    var diceCount by rememberSaveable { mutableIntStateOf(1) }
    var diceResults by rememberSaveable { mutableStateOf(listOf(1)) }
    var displayDice by remember { mutableStateOf(listOf(1)) }
    var isRolling by remember { mutableStateOf(false) }
    var shakeEnabled by rememberSaveable { mutableStateOf(false) }

    // Coin state
    var coinResult by rememberSaveable { mutableStateOf("Tap to flip!") }
    var isFlipping by remember { mutableStateOf(false) }
    val flipRotation = remember { Animatable(0f) }

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

    // Sync displayDice with diceResults when not rolling
    LaunchedEffect(diceResults) {
        if (!isRolling) displayDice = diceResults
    }

    // Dice rolling animation
    LaunchedEffect(isRolling) {
        if (isRolling && animEnabled) {
            // Cycle random numbers for ~1 second
            repeat(12) {
                displayDice = (1..diceCount).map { (1..6).random() }
                delay(80L)
            }
            displayDice = diceResults
            isRolling = false
            if (hapticOn) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } else if (isRolling) {
            displayDice = diceResults
            isRolling = false
        }
    }

    // Shake-to-Roll sensor
    DisposableEffect(shakeEnabled, selectedTab) {
        if (!shakeEnabled || selectedTab != GeneratorTab.DICE) {
            return@DisposableEffect onDispose { }
        }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastShakeTime = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble())
                val now = System.currentTimeMillis()
                if (magnitude > 20 && now - lastShakeTime > 800 && !isRolling) {
                    lastShakeTime = now
                    diceResults = (1..diceCount).map { (1..6).random() }
                    isRolling = true
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    fun animateBounce() {
        if (!animEnabled) return
        scope.launch {
            scale.animateTo(1.15f, spring(stiffness = Spring.StiffnessHigh))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    fun rollDice() {
        diceResults = (1..diceCount).map { (1..6).random() }
        if (animEnabled) {
            isRolling = true
        } else {
            displayDice = diceResults
            if (hapticOn) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        animateBounce()
    }

    fun flipCoin() {
        if (isFlipping) return
        val result = if ((0..1).random() == 0) "Heads" else "Tails"
        if (animEnabled) {
            isFlipping = true
            scope.launch {
                flipRotation.snapTo(0f)
                flipRotation.animateTo(1800f, tween(800))
                coinResult = result
                isFlipping = false
                if (hapticOn) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
            // Set result at midpoint for reveal
            scope.launch {
                delay(400)
                coinResult = result
            }
        } else {
            coinResult = result
            if (hapticOn) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        animateBounce()
    }

    fun generateNumber() {
        val min = numberMin.toLongOrNull() ?: 1
        val max = numberMax.toLongOrNull() ?: 100
        numberResult = if (min <= max) (min..max).random().toString() else "Invalid range"
        animateBounce()
        if (hapticOn) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
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

    // Password strength helpers
    fun passwordStrength(pw: String): Triple<String, Color, Float> {
        if (pw.isEmpty() || pw == "Select at least one option") return Triple("", Color.Transparent, 0f)
        var charsetSize = 0
        if (pw.any { it.isLowerCase() }) charsetSize += 26
        if (pw.any { it.isUpperCase() }) charsetSize += 26
        if (pw.any { it.isDigit() }) charsetSize += 10
        if (pw.any { !it.isLetterOrDigit() }) charsetSize += 32
        val entropy = if (charsetSize > 0) pw.length * (ln(charsetSize.toDouble()) / ln(2.0)) else 0.0
        return when {
            entropy < 28 -> Triple("Weak", Color(0xFFEF5350), 0.2f)
            entropy < 36 -> Triple("Fair", Color(0xFFFFA726), 0.4f)
            entropy < 60 -> Triple("Good", Color(0xFF66BB6A), 0.65f)
            entropy < 80 -> Triple("Strong", Color(0xFF42A5F5), 0.85f)
            else -> Triple("Very Strong", Color(0xFF42A5F5), 1f)
        }
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

                    // Shake-to-roll toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Shake to Roll",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Switch(
                            checked = shakeEnabled,
                            onCheckedChange = { shakeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                                checkedThumbColor = MaterialTheme.colorScheme.onTertiary,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dice results
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale.value),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        displayDice.forEach { value ->
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

                    if (displayDice.size > 1) {
                        Text(
                            text = "Total: ${displayDice.sum()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { rollDice() },
                        enabled = !isRolling,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRolling) "Rolling..." else "Roll", style = MaterialTheme.typography.titleMedium)
                    }
                }

                GeneratorTab.COIN -> {
                    val isHeads = coinResult == "Heads"
                    val isTails = coinResult == "Tails"
                    val hasResult = isHeads || isTails

                    val headsGold = Color(0xFFD4A843)
                    val headsDark = Color(0xFFB8922E)
                    val tailsSilver = Color(0xFF9EAAB8)
                    val tailsDark = Color(0xFF7A8A9A)

                    val coinBg by animateColorAsState(
                        targetValue = when {
                            isHeads -> Color(0xFF2A2418)
                            isTails -> Color(0xFF1E2228)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        animationSpec = tween(400),
                        label = "coinBg",
                    )
                    val coinAccent by animateColorAsState(
                        targetValue = when {
                            isHeads -> headsGold
                            isTails -> tailsSilver
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = tween(400),
                        label = "coinAccent",
                    )
                    val coinBorder by animateColorAsState(
                        targetValue = when {
                            isHeads -> headsDark
                            isTails -> tailsDark
                            else -> MaterialTheme.colorScheme.outline
                        },
                        animationSpec = tween(400),
                        label = "coinBorder",
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Coin
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(scale.value)
                            .graphicsLayer {
                                if (animEnabled) rotationX = flipRotation.value
                            }
                            .clip(CircleShape)
                            .background(coinBg)
                            .border(6.dp, coinBorder, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Inner decorative ring
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .border(1.5.dp, coinAccent.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                // Icon
                                Icon(
                                    imageVector = when {
                                        isHeads -> Icons.Outlined.StarOutline
                                        isTails -> Icons.Outlined.Park
                                        else -> Icons.Outlined.StarOutline
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = coinAccent,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Label on coin
                                Text(
                                    text = when {
                                        isHeads -> "HEADS"
                                        isTails -> "TAILS"
                                        else -> "FLIP"
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 3.sp,
                                    ),
                                    color = coinAccent,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (hasResult) coinResult else "Tap to flip!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = if (hasResult) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (hasResult) coinAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { flipCoin() },
                        enabled = !isFlipping,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            if (isFlipping) "Flipping..." else "Flip Coin",
                            style = MaterialTheme.typography.titleMedium,
                        )
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

                        // Password strength indicator
                        val (label, color, fraction) = passwordStrength(passwordResult)
                        if (label.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val animatedFraction by animateFloatAsState(
                                targetValue = fraction,
                                animationSpec = tween(400),
                                label = "strengthBar",
                            )
                            val animatedColor by animateColorAsState(
                                targetValue = color,
                                animationSpec = tween(400),
                                label = "strengthColor",
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(if (animEnabled) animatedFraction else fraction)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (animEnabled) animatedColor else color),
                                    )
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (animEnabled) animatedColor else color,
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
