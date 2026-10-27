package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserPreferences
import com.example.ui.components.CyberCard
import com.example.ui.components.LiveCameraRepCounterModal
import com.example.ui.components.SystemFeedbackHelper
import com.example.ui.theme.CrimsonPenaltyRed
import com.example.ui.theme.RankGold
import com.example.ui.theme.SystemAmoledBg
import com.example.ui.theme.SystemGreen
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceBorder
import com.example.ui.theme.SystemSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun PenaltyLockoutScreen(
    userPreferences: UserPreferences,
    onPenaltyCleared: () -> Unit
) {
    val context = LocalContext.current

    // Pulsing Red Warning Glow
    val infiniteTransition = rememberInfiniteTransition(label = "penalty_pulse")
    val alphaGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    var countdownSeconds by remember { mutableIntStateOf(14400) } // 4 Hours Desert Penalty
    var burpeesChecked by remember { mutableStateOf(false) }
    var squatsChecked by remember { mutableStateOf(false) }
    var roadworkChecked by remember { mutableStateOf(false) }
    var activeCameraPenalty by remember { mutableStateOf<Pair<String, Int>?>(null) }

    LaunchedEffect(Unit) {
        while (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
    }

    val hours = countdownSeconds / 3600
    val minutes = (countdownSeconds % 3600) / 60
    val seconds = countdownSeconds % 60
    val timerString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val allQuestsCompleted = burpeesChecked && squatsChecked && roadworkChecked

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070002))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CyberCard(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            borderColor = CrimsonPenaltyRed,
            borderWidth = 2.dp,
            backgroundColor = SystemAmoledBg
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Header
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(CrimsonPenaltyRed.copy(alpha = alphaGlow))
                        .border(2.dp, CrimsonPenaltyRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonPenaltyRed, modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "[SYSTEM WARNING: PENALTY QUEST ACTIVE]",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = CrimsonPenaltyRed,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "You failed to complete your daily Monarch training quest before midnight. The System has locked you inside the Solo Leveling Desert Penalty Zone. Complete all 3 emergency penalty quests to unlock the system.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Countdown Timer Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CutCornerShape(6.dp))
                        .background(CrimsonPenaltyRed.copy(alpha = 0.15f))
                        .border(1.dp, CrimsonPenaltyRed, CutCornerShape(6.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DESERT PENALTY SURVIVAL TIME REMAINING", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CrimsonPenaltyRed, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(timerString, fontSize = 28.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "SURVIVAL QUESTS (ALL MANDATORY):",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quest 1: 100 Burpees
                PenaltyCheckItem(
                    title = "100 PENALTY BURPEES",
                    subtitle = "Chest touching floor with vertical jump at top",
                    isChecked = burpeesChecked,
                    onToggle = {
                        SystemFeedbackHelper.triggerHaptic(context)
                        burpeesChecked = !burpeesChecked
                    },
                    onCameraClick = {
                        SystemFeedbackHelper.triggerHaptic(context)
                        activeCameraPenalty = "100 PENALTY BURPEES" to 100
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quest 2: 200 Squats
                PenaltyCheckItem(
                    title = "200 PENALTY SQUATS",
                    subtitle = "Hip crease strictly below knee level (full depth)",
                    isChecked = squatsChecked,
                    onToggle = {
                        SystemFeedbackHelper.triggerHaptic(context)
                        squatsChecked = !squatsChecked
                    },
                    onCameraClick = {
                        SystemFeedbackHelper.triggerHaptic(context)
                        activeCameraPenalty = "200 PENALTY SQUATS" to 200
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quest 3: 5km Survival Roadwork
                PenaltyCheckItem(
                    title = "5KM EMERGENCY SURVIVAL ROADWORK",
                    subtitle = "Run 5.00 km non-stop to escape the Sand Centipede",
                    isChecked = roadworkChecked,
                    onToggle = {
                        SystemFeedbackHelper.triggerHaptic(context)
                        roadworkChecked = !roadworkChecked
                    },
                    onCameraClick = null
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        SystemFeedbackHelper.triggerHaptic(context)
                        userPreferences.isPenaltyLockActive = false
                        userPreferences.addXp(300)
                        userPreferences.addEarnedBadge("PENALTY ZONE SURVIVOR")
                        SystemFeedbackHelper.playSuccessFanfare()
                        onPenaltyCleared()
                    },
                    enabled = allQuestsCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allQuestsCompleted) CrimsonPenaltyRed else SystemSurfaceElevated,
                        contentColor = if (allQuestsCompleted) Color.White else TextMuted
                    ),
                    shape = CutCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_penalty_survival_button")
                ) {
                    Icon(if (allQuestsCompleted) Icons.Default.Shield else Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (allQuestsCompleted) "SUBMIT PENALTY SURVIVAL & UNLOCK SYSTEM" else "LOCKED: COMPLETE ALL 3 QUESTS ABOVE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dev Bypass Button for instant testing
                Text(
                    text = "[DEVELOPER BYPASS FOR TESTING]",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    modifier = Modifier.clickable {
                        userPreferences.isPenaltyLockActive = false
                        onPenaltyCleared()
                    }
                )
            }
        }
    }

    // Live Camera Rep Counter Modal for Penalty
    activeCameraPenalty?.let { (title, targetReps) ->
        LiveCameraRepCounterModal(
            exerciseName = title,
            targetReps = targetReps,
            targetMuscle = "Penalty Desert Survival",
            onVerificationCompleted = {
                if (targetReps == 100) burpeesChecked = true
                if (targetReps == 200) squatsChecked = true
                activeCameraPenalty = null
            },
            onDismiss = { activeCameraPenalty = null }
        )
    }
}

@Composable
private fun PenaltyCheckItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onCameraClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(4.dp))
            .background(if (isChecked) CrimsonPenaltyRed.copy(alpha = 0.2f) else SystemSurfaceElevated)
            .border(1.dp, if (isChecked) CrimsonPenaltyRed else SystemSurfaceBorder, CutCornerShape(4.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isChecked) CrimsonPenaltyRed else TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, fontSize = 9.sp, color = TextSecondary)
                }
                Icon(
                    imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isChecked) CrimsonPenaltyRed else TextMuted,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onToggle() }
                )
            }

            if (onCameraClick != null && !isChecked) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onCameraClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPenaltyRed.copy(alpha = 0.25f), contentColor = CrimsonPenaltyRed),
                    shape = CutCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().height(32.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("COUNT REPS ON LIVE CAMERA", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                }
            } else if (isChecked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("✓ PENALTY QUEST VERIFIED", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = SystemGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}
