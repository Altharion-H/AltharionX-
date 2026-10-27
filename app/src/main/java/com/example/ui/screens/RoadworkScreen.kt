package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserPreferences
import com.example.ui.components.CyberCard
import com.example.ui.components.SystemFeedbackHelper
import com.example.ui.components.SystemHeader
import com.example.ui.theme.CrimsonPenaltyRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MonarchPurple
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.RankGold
import com.example.ui.theme.SystemAmoledBg
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceBorder
import com.example.ui.theme.SystemSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun RoadworkScreen(
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Roadwork Run State
    var isRunning by remember { mutableStateOf(false) }
    var runElapsedSeconds by remember { mutableIntStateOf(0) }
    var currentDistanceKm by remember { mutableFloatStateOf(0.0f) }
    val targetDistanceKm = 5.0f
    var showRunCompleteDialog by remember { mutableStateOf(false) }

    // Jump Rope / Skipping State
    var skipsCount by remember { mutableIntStateOf(0) }
    val targetSkips = 1000
    var isSkippingTimerActive by remember { mutableStateOf(false) }
    var skippingRoundSeconds by remember { mutableIntStateOf(180) } // 3 min boxing round

    // Sprint Intervals State
    var sprintIntervalCurrent by remember { mutableIntStateOf(1) }
    val sprintIntervalTotal = 10

    // Timer effect for 5km run
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            runElapsedSeconds++
            // Simulates real-time roadwork pace (~5.0 min/km or ~3.33 meters per second)
            currentDistanceKm += 0.0033f
            if (currentDistanceKm >= targetDistanceKm) {
                currentDistanceKm = targetDistanceKm
                isRunning = false
                showRunCompleteDialog = true
                userPreferences.addRoadworkKm(targetDistanceKm)
                userPreferences.addXp(250)
                SystemFeedbackHelper.playSuccessFanfare()
                SystemFeedbackHelper.triggerHaptic(context)
            }
        }
    }

    // Timer effect for skipping round
    LaunchedEffect(isSkippingTimerActive) {
        while (isSkippingTimerActive && skippingRoundSeconds > 0) {
            delay(1000L)
            skippingRoundSeconds--
            // Adds ~2 skips per second average boxing cadence
            skipsCount += 2
            if (skippingRoundSeconds <= 0) {
                isSkippingTimerActive = false
                skippingRoundSeconds = 180
                userPreferences.addSkips(skipsCount)
                userPreferences.addXp(100)
                SystemFeedbackHelper.playSuccessFanfare()
            }
        }
    }

    val runMinutes = runElapsedSeconds / 60
    val runSeconds = runElapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", runMinutes, runSeconds)
    val paceString = if (currentDistanceKm > 0.05f) {
        val paceSecPerKm = (runElapsedSeconds / currentDistanceKm).toInt()
        String.format("%d:%02d /km", paceSecPerKm / 60, paceSecPerKm % 60)
    } else {
        "--:-- /km"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SystemAmoledBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        item {
            SystemHeader(
                title = "ROADWORK & ENDURANCE PROTOCOL",
                subtitle = "5KM TIMED RUN • 1000-SKIP CADENCE • 10x100M SHADOW SPRINTS"
            )
        }

        // Section 1: Lifetime Roadwork Stats Banner
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                borderColor = SystemCyan,
                borderWidth = 1.dp,
                backgroundColor = SystemSurfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL RUN", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            String.format("%.1f KM", userPreferences.totalRoadworkKm),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = SystemCyan
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(SystemSurfaceBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL SKIPS", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${userPreferences.totalSkips}",
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = RankGold
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(SystemSurfaceBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SAITAMA QUEST", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("10KM DAILY", fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NeonGreen)
                    }
                }
            }
        }

        // Section 2: 5KM TIMED ROADWORK RUN TRACKER
        item {
            Spacer(modifier = Modifier.height(10.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isRunning) NeonGreen else SystemSurfaceBorder,
                borderWidth = if (isRunning) 1.5.dp else 1.dp,
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, tint = if (isRunning) NeonGreen else SystemCyan, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "5KM MONARCH ROADWORK RUN",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                        if (isRunning) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonGreen.copy(alpha = 0.2f))
                                    .border(1.dp, NeonGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("LIVE RUNNING", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = NeonGreen, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Distance Counter Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("DISTANCE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                            Text(
                                text = String.format("%.2f", currentDistanceKm),
                                fontSize = 38.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = SystemCyan
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("GOAL: 5.00 KM", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TIME: $timeFormatted",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    val runProgress = (currentDistanceKm / targetDistanceKm).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(SystemSurfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(runProgress)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(NeonGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Checkpoints row (1km, 2.5km, 4km, 5km)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CheckpointBadge("1.0KM", currentDistanceKm >= 1.0f)
                        CheckpointBadge("2.5KM (HALF)", currentDistanceKm >= 2.5f)
                        CheckpointBadge("4.0KM", currentDistanceKm >= 4.0f)
                        CheckpointBadge("5.0KM GOAL", currentDistanceKm >= 5.0f)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                isRunning = !isRunning
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) CrimsonPenaltyRed else NeonGreen,
                                contentColor = Color.Black
                            ),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("start_run_button")
                        ) {
                            Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isRunning) "PAUSE ROADWORK" else "START 5KM RUN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                isRunning = false
                                if (currentDistanceKm > 0.2f) {
                                    userPreferences.addRoadworkKm(currentDistanceKm)
                                }
                                currentDistanceKm = 0.0f
                                runElapsedSeconds = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SystemSurfaceElevated, contentColor = TextMuted),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SystemSurfaceBorder),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Section 3: JUMP ROPE / SKIPPING CADENCE TRACKER
        item {
            Spacer(modifier = Modifier.height(14.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = RankGold.copy(alpha = 0.5f),
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = RankGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "JUMP ROPE / SKIPPING CADENCE",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                        Text("$skipsCount / $targetSkips SKIPS", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RankGold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "High-cadence rope skipping burns stubborn fat, conditions calves, and sharpens kinetic speed like a world champion boxer.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Round Timer Box (3 Minute Boxing Round)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val roundMin = skippingRoundSeconds / 60
                        val roundSec = skippingRoundSeconds % 60
                        Text(
                            text = "3-MIN ROUND: ${String.format("%d:%02d", roundMin, roundSec)}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SystemSurfaceElevated)
                                    .clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        skipsCount += 50
                                        userPreferences.addSkips(50)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("+50", fontSize = 10.sp, color = RankGold, fontFamily = FontFamily.Monospace)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SystemSurfaceElevated)
                                    .clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        skipsCount += 100
                                        userPreferences.addSkips(100)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("+100", fontSize = 10.sp, color = RankGold, fontFamily = FontFamily.Monospace)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SystemSurfaceElevated)
                                    .clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        skipsCount += 250
                                        userPreferences.addSkips(250)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("+250", fontSize = 10.sp, color = RankGold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            isSkippingTimerActive = !isSkippingTimerActive
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSkippingTimerActive) CrimsonPenaltyRed else RankGold,
                            contentColor = Color.Black
                        ),
                        shape = CutCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text(
                            text = if (isSkippingTimerActive) "PAUSE SKIPPING ROUND" else "START 3-MIN SKIPPING CADENCE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section 4: 10 x 100M SHADOW DASH SPRINTS
        item {
            Spacer(modifier = Modifier.height(14.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = MonarchPurple.copy(alpha = 0.5f),
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MonarchPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "10 x 100M SHADOW SPRINTS",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                        Text("INTERVAL $sprintIntervalCurrent / $sprintIntervalTotal", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = MonarchPurple)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All-out maximum velocity intervals to recruit fast-twitch muscle fibers, spike growth hormone, and expand aerobic threshold.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 1..sprintIntervalTotal) {
                            val done = i < sprintIntervalCurrent
                            val active = i == sprintIntervalCurrent
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (done) NeonGreen else if (active) MonarchPurple else SystemSurfaceBorder)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            if (sprintIntervalCurrent < sprintIntervalTotal) {
                                sprintIntervalCurrent++
                                userPreferences.addXp(30)
                                SystemFeedbackHelper.playBeepTone()
                            } else {
                                sprintIntervalCurrent = 1
                                userPreferences.addXp(150)
                                userPreferences.addRoadworkKm(1.0f)
                                SystemFeedbackHelper.playSuccessFanfare()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MonarchPurple, contentColor = Color.White),
                        shape = CutCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text(
                            text = if (sprintIntervalCurrent < sprintIntervalTotal) "CHECK OFF INTERVAL $sprintIntervalCurrent (+30 XP)" else "COMPLETE ALL 10 SPRINTS (+150 XP)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckpointBadge(label: String, reached: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (reached) NeonGreen.copy(alpha = 0.15f) else SystemSurfaceElevated)
            .border(1.dp, if (reached) NeonGreen else SystemSurfaceBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (reached) FontWeight.Black else FontWeight.Normal,
            color = if (reached) NeonGreen else TextMuted
        )
    }
}
