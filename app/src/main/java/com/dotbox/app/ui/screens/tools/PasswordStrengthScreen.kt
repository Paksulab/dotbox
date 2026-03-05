package com.dotbox.app.ui.screens.tools

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlin.math.ln
import kotlin.math.pow

private val ColorVeryWeak = Color(0xFFEF5350)
private val ColorWeak = Color(0xFFFF7043)
private val ColorFair = Color(0xFFFFB74D)
private val ColorStrong = Color(0xFF66BB6A)
private val ColorVeryStrong = Color(0xFF42A5F5)

private val specialChars = "!@#\$%^&*()-_=+[]{}|;:',.<>?/`~\"\\"

private fun hasLowercase(pw: String) = pw.any { it.isLowerCase() }
private fun hasUppercase(pw: String) = pw.any { it.isUpperCase() }
private fun hasDigits(pw: String) = pw.any { it.isDigit() }
private fun hasSpecial(pw: String) = pw.any { it in specialChars }

private fun hasCommonPatterns(pw: String): Boolean {
    val lower = pw.lowercase()
    val patterns = listOf(
        "123", "234", "345", "456", "567", "678", "789", "890",
        "abc", "bcd", "cde", "def", "efg", "fgh", "ghi", "hij",
        "ijk", "jkl", "klm", "lmn", "mno", "nop", "opq", "pqr",
        "qrs", "rst", "stu", "tuv", "uvw", "vwx", "wxy", "xyz",
        "qwerty", "qwertz", "asdf", "zxcv", "password", "pass",
        "1234", "abcd", "letmein", "welcome", "monkey", "dragon",
        "master", "admin", "login"
    )
    return patterns.any { lower.contains(it) }
}

private fun isAllSameCharType(pw: String): Boolean {
    if (pw.isEmpty()) return false
    val allLower = pw.all { it.isLowerCase() }
    val allUpper = pw.all { it.isUpperCase() }
    val allDigit = pw.all { it.isDigit() }
    val allSpecial = pw.all { it in specialChars }
    return allLower || allUpper || allDigit || allSpecial
}

private fun calculateScore(pw: String): Int {
    if (pw.isEmpty()) return 0
    var score = 0
    if (pw.length >= 8) score += 20
    if (pw.length >= 12) score += 10
    if (pw.length >= 16) score += 10
    if (hasLowercase(pw)) score += 15
    if (hasUppercase(pw)) score += 15
    if (hasDigits(pw)) score += 15
    if (hasSpecial(pw)) score += 15
    if (isAllSameCharType(pw)) score -= 20
    if (hasCommonPatterns(pw)) score -= 10
    return score.coerceIn(0, 100)
}

private fun strengthLabel(score: Int): String = when {
    score < 20 -> "Very Weak"
    score < 40 -> "Weak"
    score < 60 -> "Fair"
    score < 80 -> "Strong"
    else -> "Very Strong"
}

private fun strengthColor(score: Int): Color = when {
    score < 20 -> ColorVeryWeak
    score < 40 -> ColorWeak
    score < 60 -> ColorFair
    score < 80 -> ColorStrong
    else -> ColorVeryStrong
}

private fun charsetSize(pw: String): Int {
    var size = 0
    if (hasLowercase(pw)) size += 26
    if (hasUppercase(pw)) size += 26
    if (hasDigits(pw)) size += 10
    if (hasSpecial(pw)) size += 32
    return size.coerceAtLeast(1)
}

private fun log2(value: Double): Double = ln(value) / ln(2.0)

private fun calculateEntropy(pw: String): Double {
    if (pw.isEmpty()) return 0.0
    val cs = charsetSize(pw).toDouble()
    return pw.length.toDouble() * log2(cs)
}

private fun estimateCrackTime(pw: String): String {
    if (pw.isEmpty()) return "Instant"
    val cs = charsetSize(pw).toDouble()
    val length = pw.length.toDouble()
    val combinations = cs.pow(length)
    val guessesPerSecond = 10_000_000_000.0
    val averageSeconds = combinations / guessesPerSecond / 2.0

    return when {
        averageSeconds < 0.001 -> "Instant"
        averageSeconds < 1.0 -> "Less than a second"
        averageSeconds < 60.0 -> "%.1f seconds".format(averageSeconds)
        averageSeconds < 3600.0 -> "%.1f minutes".format(averageSeconds / 60.0)
        averageSeconds < 86400.0 -> "%.1f hours".format(averageSeconds / 3600.0)
        averageSeconds < 31_536_000.0 -> "%.1f days".format(averageSeconds / 86400.0)
        averageSeconds < 31_536_000.0 * 100 -> "%.1f years".format(averageSeconds / 31_536_000.0)
        averageSeconds < 31_536_000.0 * 1_000_000 -> "%.0f centuries".format(averageSeconds / 31_536_000.0 / 100.0)
        else -> "Millions of centuries"
    }
}

@Composable
fun PasswordStrengthScreen(onBack: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val score = remember(password) { calculateScore(password) }
    val entropy = remember(password) { calculateEntropy(password) }
    val crackTime = remember(password) { estimateCrackTime(password) }
    val label = remember(score) { strengthLabel(score) }
    val color = strengthColor(score)

    val animatedFraction by animateFloatAsState(
        targetValue = if (password.isEmpty()) 0f else score / 100f,
        animationSpec = tween(durationMillis = 400),
        label = "strengthBar"
    )
    val animatedColor by animateColorAsState(
        targetValue = if (password.isEmpty()) Color.Gray else color,
        animationSpec = tween(durationMillis = 400),
        label = "strengthColor"
    )

    ToolScreenScaffold(
        title = "Password Strength",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                                else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password"
                                else "Show password"
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Strength Meter
            if (password.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                color = animatedColor
                            )
                        )
                        Text(
                            text = "$score / 100",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMono,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = animatedFraction)
                                .clip(RoundedCornerShape(6.dp))
                                .background(animatedColor)
                        )
                    }
                }

                // Entropy & Crack Time
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Entropy",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "%.1f bits".format(entropy),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Crack Time",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = crackTime,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.SemiBold,
                                    color = animatedColor
                                )
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Charset Size",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "${charsetSize(password)} characters",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                // Checklist
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Checklist",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        ChecklistItem("At least 8 characters", password.length >= 8)
                        ChecklistItem("Contains uppercase letter", hasUppercase(password))
                        ChecklistItem("Contains lowercase letter", hasLowercase(password))
                        ChecklistItem("Contains digit", hasDigits(password))
                        ChecklistItem("Contains special character", hasSpecial(password))
                        ChecklistItem("No common patterns", !hasCommonPatterns(password))
                    }
                }

                // Tips Section
                val tips = remember(password) { buildTips(password) }
                if (tips.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Tips to Improve",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            tips.forEach { tip ->
                                Text(
                                    text = "• $tip",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChecklistItem(label: String, passed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (passed) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = if (passed) "Pass" else "Fail",
            tint = if (passed) ColorStrong else ColorVeryWeak,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (passed) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        )
    }
}

private fun buildTips(pw: String): List<String> {
    if (pw.isEmpty()) return emptyList()
    val tips = mutableListOf<String>()
    if (pw.length < 8) tips.add("Use at least 8 characters for a basic level of security.")
    else if (pw.length < 12) tips.add("Aim for 12+ characters for better protection.")
    else if (pw.length < 16) tips.add("Consider 16+ characters for maximum strength.")
    if (!hasUppercase(pw)) tips.add("Add uppercase letters (A-Z) to increase complexity.")
    if (!hasLowercase(pw)) tips.add("Add lowercase letters (a-z) to increase complexity.")
    if (!hasDigits(pw)) tips.add("Include numbers (0-9) to broaden the character set.")
    if (!hasSpecial(pw)) tips.add("Use special characters (!@#\$%^&*) for extra entropy.")
    if (hasCommonPatterns(pw)) tips.add("Avoid common patterns like '123', 'abc', 'qwerty', or 'password'.")
    if (isAllSameCharType(pw)) tips.add("Mix different character types instead of using only one kind.")
    return tips
}
