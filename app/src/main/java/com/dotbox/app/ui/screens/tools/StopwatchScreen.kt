package com.dotbox.app.ui.screens.tools

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dotbox.app.DotBoxApplication
import com.dotbox.app.MainActivity
import com.dotbox.app.R
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.screens.settings.animationsEnabled
import com.dotbox.app.ui.theme.JetBrainsMono
import com.dotbox.app.ui.theme.NothingRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun sendTimerNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    val notification = NotificationCompat.Builder(context, DotBoxApplication.CHANNEL_STOPWATCH)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Time's Up!")
        .setContentText("Your timer countdown has finished.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    NotificationManagerCompat.from(context).notify(
        System.currentTimeMillis().toInt(),
        notification,
    )
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val ms = (millis % 1000) / 10
    return "%02d:%02d.%02d".format(minutes, seconds, ms)
}

@Composable
fun StopwatchScreen(onBack: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    ToolScreenScaffold(title = "Stopwatch & Timer", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                },
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Stopwatch") },
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Timer") },
                )
            }

            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> StopwatchTab()
                    1 -> TimerTab()
                }
            }
        }
    }
}

@Composable
private fun StopwatchTab() {
    val context = LocalContext.current
    val animEnabled = animationsEnabled(context)
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var elapsedMillis by rememberSaveable { mutableLongStateOf(0L) }
    var startTime by rememberSaveable { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            startTime = System.currentTimeMillis() - elapsedMillis
            while (isRunning) {
                elapsedMillis = System.currentTimeMillis() - startTime
                delay(10)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Time display
        Text(
            text = formatTime(elapsedMillis),
            style = MaterialTheme.typography.displayLarge.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Reset / Lap
            FilledTonalIconButton(
                onClick = {
                    if (isRunning) {
                        laps.add(0, elapsedMillis)
                    } else {
                        elapsedMillis = 0L
                        laps.clear()
                    }
                },
                enabled = elapsedMillis > 0,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(56.dp).width(56.dp),
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Flag else Icons.Default.Stop,
                    contentDescription = if (isRunning) "Lap" else "Reset",
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Start / Pause
            FilledIconButton(
                onClick = { isRunning = !isRunning },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(72.dp).width(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRunning) NothingRed else MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                )
            }

            Spacer(modifier = Modifier.width(80.dp)) // Balance the layout
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Laps
        if (laps.isNotEmpty()) {
            Text(
                text = "LAPS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                itemsIndexed(laps) { index, lapTime ->
                    val lapNumber = laps.size - index
                    val lapDelta = if (index < laps.lastIndex) lapTime - laps[index + 1] else lapTime
                    AnimatedVisibility(
                        visible = true,
                        enter = if (animEnabled) slideInVertically(initialOffsetY = { -it }) + fadeIn() else fadeIn(snap()),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Lap $lapNumber",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = formatTime(lapDelta),
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = formatTime(lapTime),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (index < laps.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerTab() {
    val context = LocalContext.current
    val animEnabled = animationsEnabled(context)
    var totalSeconds by rememberSaveable { mutableIntStateOf(0) }
    var remainingMillis by rememberSaveable { mutableLongStateOf(0L) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var isSet by rememberSaveable { mutableStateOf(false) }
    var minutes by rememberSaveable { mutableIntStateOf(5) }
    var seconds by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(isRunning) {
        if (isRunning && remainingMillis > 0) {
            val startTime = System.currentTimeMillis()
            val startRemaining = remainingMillis
            while (isRunning && remainingMillis > 0) {
                remainingMillis = startRemaining - (System.currentTimeMillis() - startTime)
                if (remainingMillis <= 0) {
                    remainingMillis = 0
                    isRunning = false
                    sendTimerNotification(context)
                }
                delay(10)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        if (!isSet) {
            // Timer setup
            Text(
                text = "SET TIMER",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Minutes
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledTonalIconButton(
                        onClick = { minutes = (minutes + 1).coerceAtMost(99) },
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("▲") }
                    Text(
                        text = "%02d".format(minutes),
                        style = MaterialTheme.typography.displayMedium.copy(fontFamily = JetBrainsMono),
                    )
                    Text("min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FilledTonalIconButton(
                        onClick = { minutes = (minutes - 1).coerceAtLeast(0) },
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("▼") }
                }

                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayMedium.copy(fontFamily = JetBrainsMono),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                // Seconds
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledTonalIconButton(
                        onClick = { seconds = (seconds + 5).coerceAtMost(55) },
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("▲") }
                    Text(
                        text = "%02d".format(seconds),
                        style = MaterialTheme.typography.displayMedium.copy(fontFamily = JetBrainsMono),
                    )
                    Text("sec", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FilledTonalIconButton(
                        onClick = { seconds = (seconds - 5).coerceAtLeast(0) },
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("▼") }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            FilledIconButton(
                onClick = {
                    totalSeconds = minutes * 60 + seconds
                    remainingMillis = totalSeconds * 1000L
                    isSet = true
                    isRunning = true
                },
                enabled = minutes > 0 || seconds > 0,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(64.dp).width(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
            }
        } else {
            // Timer running
            val displayMinutes = (remainingMillis / 1000 / 60).toInt()
            val displaySeconds = ((remainingMillis / 1000) % 60).toInt()
            val displayMs = ((remainingMillis % 1000) / 10).toInt()

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                val timerProgress = if (totalSeconds > 0) remainingMillis.toFloat() / (totalSeconds * 1000f) else 0f
                val animatedTimerProgress by animateFloatAsState(
                    targetValue = timerProgress,
                    animationSpec = tween(100),
                    label = "timerProgress",
                )
                val progressColor = MaterialTheme.colorScheme.tertiary
                val bgColor = MaterialTheme.colorScheme.outline

                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background arc
                    drawArc(
                        color = bgColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                    // Progress arc
                    drawArc(
                        color = progressColor,
                        startAngle = 135f,
                        sweepAngle = 270f * (if (animEnabled) animatedTimerProgress else timerProgress),
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                // Time text inside the arc
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%02d:%02d.%02d".format(displayMinutes, displaySeconds, displayMs),
                        style = MaterialTheme.typography.displayLarge.copy(fontFamily = JetBrainsMono),
                        color = if (remainingMillis == 0L) NothingRed else MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            if (remainingMillis == 0L) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "TIME'S UP!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NothingRed,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Reset
                FilledTonalIconButton(
                    onClick = {
                        isRunning = false
                        isSet = false
                        remainingMillis = 0
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp).width(56.dp),
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Reset")
                }

                // Play/Pause
                FilledIconButton(
                    onClick = { isRunning = !isRunning },
                    enabled = remainingMillis > 0,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(72.dp).width(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isRunning) NothingRed else MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Resume",
                    )
                }
            }
        }
    }
}
