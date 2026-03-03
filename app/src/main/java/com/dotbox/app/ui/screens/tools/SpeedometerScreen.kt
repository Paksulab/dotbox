package com.dotbox.app.ui.screens.tools

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import com.dotbox.app.ui.theme.NothingRed
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
fun SpeedometerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var isTracking by rememberSaveable { mutableStateOf(false) }
    var currentSpeed by remember { mutableDoubleStateOf(0.0) }
    var maxSpeed by remember { mutableDoubleStateOf(0.0) }
    var altitude by remember { mutableDoubleStateOf(0.0) }
    var useKmh by rememberSaveable { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // Speed is in m/s
                    val speedMs = if (location.hasSpeed()) location.speed.toDouble() else 0.0
                    currentSpeed = speedMs
                    if (speedMs > maxSpeed) maxSpeed = speedMs
                    if (location.hasAltitude()) altitude = location.altitude
                }
            }
        }
    }

    LaunchedEffect(isTracking, hasPermission) {
        if (isTracking && hasPermission) {
            try {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
                    .setMinUpdateIntervalMillis(200)
                    .build()
                fusedLocationClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper(),
                )
            } catch (e: SecurityException) {
                hasPermission = false
            }
        } else {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // Convert speed
    val displaySpeed = if (useKmh) currentSpeed * 3.6 else currentSpeed * 2.23694
    val displayMaxSpeed = if (useKmh) maxSpeed * 3.6 else maxSpeed * 2.23694
    val displayAltitude = if (useKmh) altitude else altitude * 3.28084
    val speedUnit = if (useKmh) "km/h" else "mph"
    val altUnit = if (useKmh) "m" else "ft"
    val maxGaugeSpeed = if (useKmh) 200.0 else 120.0

    val animatedSpeed by animateFloatAsState(
        targetValue = displaySpeed.toFloat(),
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "speed",
    )

    val speedColor by animateColorAsState(
        targetValue = when {
            displaySpeed < maxGaugeSpeed * 0.3 -> Color(0xFF4CAF50)
            displaySpeed < maxGaugeSpeed * 0.6 -> Color(0xFFFFC107)
            displaySpeed < maxGaugeSpeed * 0.8 -> Color(0xFFFF9800)
            else -> NothingRed
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "speedColor",
    )

    val outline = MaterialTheme.colorScheme.outline

    ToolScreenScaffold(title = "Speedometer", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (!hasPermission) {
                Text(
                    text = "Location permission required",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Grant Permission")
                }
            } else {
                // Unit toggle
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = useKmh,
                        onClick = { useKmh = true },
                        label = { Text("km/h") },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                    FilterChip(
                        selected = !useKmh,
                        onClick = { useKmh = false },
                        label = { Text("mph") },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Speed gauge
                Canvas(modifier = Modifier.size(260.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val padding = strokeWidth / 2 + 8.dp.toPx()
                    val arcSize = Size(size.width - padding * 2, size.height - padding * 2)

                    // Background arc
                    drawArc(
                        color = outline.copy(alpha = 0.2f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(padding, padding),
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )

                    // Speed arc
                    val sweep = ((animatedSpeed / maxGaugeSpeed.toFloat()) * 270f).coerceIn(0f, 270f)
                    drawArc(
                        color = speedColor,
                        startAngle = 135f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(padding, padding),
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                // Speed number
                Text(
                    text = "%.0f".format(displaySpeed),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = speedColor,
                )
                Text(
                    text = speedUnit,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.0f".format(displayMaxSpeed),
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Max $speedUnit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.0f".format(displayAltitude),
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Alt $altUnit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Start/Stop button
                Button(
                    onClick = {
                        if (isTracking) {
                            isTracking = false
                        } else {
                            maxSpeed = 0.0
                            currentSpeed = 0.0
                            isTracking = true
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTracking) NothingRed else MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(56.dp),
                ) {
                    Text(
                        text = if (isTracking) "Stop" else "Start",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
