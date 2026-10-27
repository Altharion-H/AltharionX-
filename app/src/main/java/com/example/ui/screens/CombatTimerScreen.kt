package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
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
import com.example.data.CharacterRepository
import com.example.data.UserPreferences
import com.example.ui.components.CyberCard
import com.example.ui.components.SystemFeedbackHelper
import com.example.ui.components.SystemHeader
import com.example.ui.theme.CrimsonPenaltyRed
import com.example.ui.theme.MonarchPurple
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
fun CombatTimerScreen(
    userPreferences: UserPreferences,
    initialRestSeconds: Int = 90,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeCharacter = remember(userPreferences.activeCharacterId) {
        CharacterRepository.getCharacterById(userPreferences.activeCharacterId)
    }

    // Timer states
    var totalTimeSeconds by remember { mutableIntStateOf(initialRestSeconds) }
    var timeRemainingSeconds by remember { mutableIntStateOf(initialRestSeconds) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Failure Set state
    var failureSetsCount by remember { mutableIntStateOf(userPreferences.failureSetsToday) }

    // Hydration & Nutrition states
    var waterLoggedMl by remember { mutableIntStateOf(userPreferences.waterMlToday) }
    var proteinLoggedG by remember { mutableIntStateOf(userPreferences.proteinLoggedToday) }

    val waterGoalMl = (activeCharacter.hydrationGoalLiters * 1000).toInt()
    val proteinGoalG = (userPreferences.userWeightKg * activeCharacter.proteinPerKg).toInt()

    // Countdown tick
    LaunchedEffect(isTimerRunning, timeRemainingSeconds) {
        if (isTimerRunning && timeRemainingSeconds > 0) {
            delay(1000L)
            timeRemainingSeconds--
            if (timeRemainingSeconds == 0) {
                isTimerRunning = false
                SystemFeedbackHelper.playBeepTone()
                SystemFeedbackHelper.triggerHaptic(context)
            }
        }
    }

    val progressFraction = remember(timeRemainingSeconds, totalTimeSeconds) {
        if (totalTimeSeconds > 0) timeRemainingSeconds.toFloat() / totalTimeSeconds.toFloat() else 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "timer_progress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SystemAmoledBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        item {
            SystemHeader(
                title = "COMBAT TIMER & LOG",
                subtitle = "REST CADENCE • FAILURE TRACKER • MACRO INTAKE"
            )
        }

        // Rest Timer Card
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                borderColor = if (isTimerRunning) SystemCyan else SystemSurfaceBorder,
                borderWidth = 1.2.dp,
                backgroundColor = SystemSurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TACTICAL REST TIMER",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular Countdown Ring
                    Box(
                        modifier = Modifier.size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = SystemSurfaceBorder,
                            strokeWidth = 8.dp,
                        )
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (timeRemainingSeconds <= 5) CrimsonPenaltyRed else SystemCyan,
                            strokeWidth = 8.dp,
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val minutes = timeRemainingSeconds / 60
                            val seconds = timeRemainingSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (timeRemainingSeconds <= 5) CrimsonPenaltyRed else TextPrimary
                            )
                            Text(
                                text = if (isTimerRunning) "RECOVERING" else "PAUSED",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isTimerRunning) SystemCyan else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quick Adjust Buttons (+/- 15s)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickTimeButton(text = "-15s") {
                            timeRemainingSeconds = (timeRemainingSeconds - 15).coerceAtLeast(0)
                        }
                        QuickTimeButton(text = "45s") {
                            totalTimeSeconds = 45
                            timeRemainingSeconds = 45
                        }
                        QuickTimeButton(text = "60s") {
                            totalTimeSeconds = 60
                            timeRemainingSeconds = 60
                        }
                        QuickTimeButton(text = "90s") {
                            totalTimeSeconds = 90
                            timeRemainingSeconds = 90
                        }
                        QuickTimeButton(text = "120s") {
                            totalTimeSeconds = 120
                            timeRemainingSeconds = 120
                        }
                        QuickTimeButton(text = "+15s") {
                            timeRemainingSeconds += 15
                            totalTimeSeconds = maxOf(totalTimeSeconds, timeRemainingSeconds)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Timer Play / Pause / Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                isTimerRunning = !isTimerRunning
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTimerRunning) CrimsonPenaltyRed else SystemCyan,
                                contentColor = Color.Black
                            ),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("toggle_timer_button")
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTimerRunning) "PAUSE" else "START TIMER",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                isTimerRunning = false
                                timeRemainingSeconds = totalTimeSeconds
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SystemSurface,
                                contentColor = TextPrimary
                            ),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .border(1.dp, SystemSurfaceBorder, CutCornerShape(6.dp))
                                .testTag("reset_timer_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Failure Set Counter Section
        item {
            Spacer(modifier = Modifier.height(10.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = CrimsonPenaltyRed,
                borderWidth = 1.dp,
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonPenaltyRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = CrimsonPenaltyRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MECHANICAL FAILURE SETS",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "SETS TAKEN TO TRUE RPE 10 CONCENTRIC FAILURE",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted
                                )
                            }
                        }

                        Text(
                            text = "$failureSetsCount",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = CrimsonPenaltyRed
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            SystemFeedbackHelper.playBeepTone()
                            failureSetsCount++
                            userPreferences.failureSetsToday = failureSetsCount
                            userPreferences.addXp(50)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonPenaltyRed,
                            contentColor = Color.White
                        ),
                        shape = CutCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("log_failure_set_button")
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOG FAILURE SET (+50 XP)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Daily Hydration & Macro Tracker
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "[DAILY MONARCH NUTRITION & HYDRATION]",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Water Card
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocalDrink, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HYDRATION GOAL",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "$waterLoggedMl / $waterGoalMl ml",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = SystemCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val waterFraction = (waterLoggedMl.toFloat() / waterGoalMl.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SystemSurfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(waterFraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(SystemCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAddMacroButton(text = "+250ml") {
                            SystemFeedbackHelper.triggerHaptic(context)
                            waterLoggedMl += 250
                            userPreferences.waterMlToday = waterLoggedMl
                        }
                        QuickAddMacroButton(text = "+500ml") {
                            SystemFeedbackHelper.triggerHaptic(context)
                            waterLoggedMl += 500
                            userPreferences.waterMlToday = waterLoggedMl
                        }
                        QuickAddMacroButton(text = "+1000ml") {
                            SystemFeedbackHelper.triggerHaptic(context)
                            waterLoggedMl += 1000
                            userPreferences.waterMlToday = waterLoggedMl
                        }
                    }
                }
            }
        }

        // Protein Card
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = RankGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PROTEIN TARGET",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "$proteinLoggedG / $proteinGoalG g",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = RankGold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val proteinFraction = (proteinLoggedG.toFloat() / proteinGoalG.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SystemSurfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(proteinFraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(RankGold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAddMacroButton(text = "+25g") {
                            SystemFeedbackHelper.triggerHaptic(context)
                            proteinLoggedG += 25
                            userPreferences.proteinLoggedToday = proteinLoggedG
                        }
                        QuickAddMacroButton(text = "+40g") {
                            SystemFeedbackHelper.triggerHaptic(context)
                            proteinLoggedG += 40
                            userPreferences.proteinLoggedToday = proteinLoggedG
                        }
                        QuickAddMacroButton(text = "+60g") {
                            SystemFeedbackHelper.triggerHaptic(context)
                            proteinLoggedG += 60
                            userPreferences.proteinLoggedToday = proteinLoggedG
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTimeButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SystemSurface)
            .border(0.8.dp, SystemSurfaceBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun QuickAddMacroButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SystemSurfaceElevated)
            .border(0.8.dp, SystemSurfaceBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
