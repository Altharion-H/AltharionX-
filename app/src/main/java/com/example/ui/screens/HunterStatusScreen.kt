package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.CharacterRepository
import com.example.data.HunterFriend
import com.example.data.UserPreferences
import androidx.compose.material.icons.filled.Videocam
import com.example.ui.components.CyberCard
import com.example.ui.components.RankBadge
import com.example.ui.components.StatChip
import com.example.ui.components.SystemFeedbackHelper
import com.example.ui.components.SystemHeader
import com.example.ui.components.SystemNotificationHelper
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HunterStatusScreen(
    userPreferences: UserPreferences,
    onNavigateToRoster: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val hasContract = userPreferences.isContractSigned && userPreferences.activeCharacterId.isNotBlank()
    val activeCharacter = remember(userPreferences.activeCharacterId) {
        if (hasContract) CharacterRepository.getCharacterById(userPreferences.activeCharacterId)
        else CharacterRepository.characters.first()
    }

    var totalXp by remember { mutableStateOf(userPreferences.totalXp) }
    var hunterRank by remember { mutableStateOf(userPreferences.getHunterRank()) }
    var rankProgress by remember { mutableFloatStateOf(userPreferences.getRankProgressFraction()) }
    var equippedBadge by remember { mutableStateOf(userPreferences.equippedBadge) }
    var streakDays by remember { mutableStateOf(userPreferences.streakDays) }

    var userWeight by remember { mutableFloatStateOf(userPreferences.userWeightKg) }
    var userHeight by remember { mutableFloatStateOf(userPreferences.userHeightCm) }
    var shoulderCm by remember { mutableFloatStateOf(userPreferences.shoulderCm) }
    var chestCm by remember { mutableFloatStateOf(userPreferences.chestCm) }
    var waistCm by remember { mutableFloatStateOf(userPreferences.waistCm) }
    var armsCm by remember { mutableFloatStateOf(userPreferences.armsCm) }

    // Dialog & Navigation states
    var showEditStatsDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showTransformationMemoryModal by remember { mutableStateOf(false) }
    var showPenaltyModal by remember { mutableStateOf(false) }
    var showShareSuccessDialog by remember { mutableStateOf(false) }

    var activeTheme by remember { mutableStateOf(userPreferences.activeThemeName) }

    val streakTier = remember(streakDays) {
        userPreferences.getStreakEvolutionTier()
    }

    val isCoOpUnlocked = userPreferences.isCoOpUnlocked

    val vTaperRatio = remember(shoulderCm, waistCm) {
        if (waistCm > 0) String.format("%.2f", shoulderCm / waistCm) else "1.54"
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
                title = "HUNTER STATUS & IDENTITY",
                subtitle = "PLAYER BIOMETRICS • DUOLINGO STREAKS • CO-OP NETWORK"
            )
        }

        // Section 1: Authentication & Hunter Account Card (Google or Hunter ID Login)
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                borderColor = if (userPreferences.isLoggedIn) SystemCyan else SystemSurfaceBorder,
                backgroundColor = SystemSurfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (userPreferences.isLoggedIn) SystemCyan.copy(alpha = 0.2f) else SystemSurfaceBorder)
                                .border(1.5.dp, if (userPreferences.isLoggedIn) SystemCyan else TextMuted, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = if (userPreferences.isLoggedIn) SystemCyan else TextMuted, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (userPreferences.isLoggedIn) userPreferences.userName else "OFFLINE GUEST HUNTER",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (userPreferences.isLoggedIn) NeonGreen.copy(alpha = 0.15f) else SystemSurface)
                                        .border(0.8.dp, if (userPreferences.isLoggedIn) NeonGreen else TextMuted, RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = userPreferences.loginProvider.uppercase(),
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (userPreferences.isLoggedIn) NeonGreen else TextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (userPreferences.isLoggedIn) userPreferences.userEmail else "Offline Mode • Connect to Sync",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            showAuthDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userPreferences.isLoggedIn) SystemSurface else SystemCyan,
                            contentColor = if (userPreferences.isLoggedIn) SystemCyan else Color.Black
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SystemCyan),
                        shape = CutCornerShape(4.dp),
                        modifier = Modifier.height(36.dp).testTag("account_login_button")
                    ) {
                        Text(
                            text = if (userPreferences.isLoggedIn) "ACCOUNT" else "SIGN IN",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Section 2: Duolingo-Style Weekly Streak Evolution Widget
        item {
            Spacer(modifier = Modifier.height(10.dp))
            val flameColor = Color(streakTier.flameColorHex)
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = flameColor,
                borderWidth = 1.2.dp,
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Whatshot, contentDescription = null, tint = flameColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "WEEK ${streakTier.weekNumber}: ${streakTier.title}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = flameColor
                                )
                                Text(
                                    text = streakTier.auraDescription,
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Text(
                            text = "$streakDays DAYS",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = flameColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 7-Day Weekly Check-In Badges (M, T, W, T, F, S, S)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("M", "T", "W", "T", "F", "S", "S")
                        days.forEachIndexed { i, d ->
                            val isDone = i < (streakDays % 7).let { if (it == 0 && streakDays > 0) 7 else it }
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) flameColor.copy(alpha = 0.2f) else SystemSurfaceElevated)
                                    .border(1.dp, if (isDone) flameColor else SystemSurfaceBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = d,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = if (isDone) flameColor else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Duolingo-style Urgent Notification Reminder Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(CrimsonPenaltyRed.copy(alpha = 0.12f))
                            .border(1.dp, CrimsonPenaltyRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = CrimsonPenaltyRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚠️ DUOLINGO SYSTEM ALERT: Complete your daily quest before midnight or your phone locks into the Solo Leveling Penalty Zone!",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CrimsonPenaltyRed,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Rank & Sovereign Badge Identity Card
        item {
            Spacer(modifier = Modifier.height(10.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (equippedBadge.isNotBlank()) RankGold else MonarchPurple,
                borderWidth = 1.2.dp,
                backgroundColor = SystemSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HUNTER RANK CLASSIFICATION",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = hunterRank,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = if (equippedBadge.isNotBlank()) RankGold else SystemCyan
                            )
                        }
                        RankBadge(rank = hunterRank)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // XP Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SystemSurfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(rankProgress)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (equippedBadge.isNotBlank()) RankGold else SystemCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL XP: $totalXp", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("BOUND: ${activeCharacter.name}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    }

                    if (equippedBadge.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CutCornerShape(4.dp))
                                .background(RankGold.copy(alpha = 0.15f))
                                .border(1.dp, RankGold, CutCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = RankGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("EQUIPPED BADGE: ★ $equippedBadge ★", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = RankGold)
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Biometric Measurements & V-Taper Ratio
        item {
            Spacer(modifier = Modifier.height(10.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("BIOMETRIC MEASUREMENTS", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text(
                            text = "EDIT STATS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SystemCyan,
                            modifier = Modifier.clickable {
                                SystemFeedbackHelper.triggerHaptic(context)
                                showEditStatsDialog = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MetricItem("WEIGHT", "${userWeight}kg", Modifier.weight(1f))
                        MetricItem("HEIGHT", "${userHeight}cm", Modifier.weight(1f))
                        MetricItem("SHOULDERS", "${shoulderCm}cm", Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MetricItem("CHEST", "${chestCm}cm", Modifier.weight(1f))
                        MetricItem("WAIST", "${waistCm}cm", Modifier.weight(1f))
                        MetricItem("V-TAPER", "${vTaperRatio}:1", Modifier.weight(1f))
                    }
                }
            }
        }

        // Section 5: MULTI-THEME ENGINE SWITCHER (Minimalist & Vivid Themes)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MINIMALIST SYSTEM THEMES", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Select your preferred visual style across all screens and widgets.", fontSize = 10.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(10.dp))

                    val themes = listOf(
                        "MONARCH_AMOLED" to "Monarch AMOLED (Pitch Black / Cyan)",
                        "SHADOW_PURPLE" to "Shadow Velvet (Obsidian / Purple)",
                        "CRIMSON_PENALTY" to "Crimson Penalty (Matte Onyx / Red)",
                        "MINIMALIST_ZEN" to "Minimalist Zen (Clean Slate / Teal)",
                        "GOLDEN_ESCANOR" to "Golden Escanor (Bronze / Sun Gold)",
                        "CYBERPUNK_NEON" to "Cyberpunk 2077 (Navy / Amber)"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        themes.forEach { (tKey, tLabel) ->
                            val isSel = activeTheme == tKey
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CutCornerShape(4.dp))
                                    .background(if (isSel) SystemCyan.copy(alpha = 0.15f) else SystemSurfaceElevated)
                                    .border(1.dp, if (isSel) SystemCyan else SystemSurfaceBorder, CutCornerShape(4.dp))
                                    .clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        activeTheme = tKey
                                        userPreferences.activeThemeName = tKey
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSel) Icons.Default.CheckCircle else Icons.Default.ColorLens,
                                        contentDescription = null,
                                        tint = if (isSel) SystemCyan else TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tLabel,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSel) FontWeight.Black else FontWeight.Normal,
                                        color = if (isSel) SystemCyan else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 6: HUNTER CO-OP NETWORK & FRIEND DUELS (GATED BY 14-DAY STREAK)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isCoOpUnlocked) NeonGreen else SystemSurfaceBorder,
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = if (isCoOpUnlocked) NeonGreen else TextMuted, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HUNTER CO-OP & FRIEND DUELS",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                        if (isCoOpUnlocked) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonGreen.copy(alpha = 0.15f))
                                    .border(0.8.dp, NeonGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("UNLOCKED ✓", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = NeonGreen, fontWeight = FontWeight.Black)
                            }
                        } else {
                            Text("${streakDays}/14 DAYS", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RankGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (!isCoOpUnlocked) {
                        // Padlock restriction card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(SystemSurfaceElevated)
                                .border(1.dp, SystemSurfaceBorder, RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = RankGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("DISCIPLINE GATE: 14-DAY STREAK REQUIRED", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = RankGold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "To preserve high-standard hunter training, the Co-Op & Friend Raid Network unlocks only after you prove discipline with an unbroken 14-day streak.",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    lineHeight = 14.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                val progress = (streakDays.toFloat() / 14f).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(SystemSurfaceBorder)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progress)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(RankGold)
                                    )
                                }
                            }
                        }
                    } else {
                        // Unlocked Friend Leaderboard & Co-Op Raids
                        Text("HUNTER ASSOCIATION CO-OP LEADERBOARD", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                        Spacer(modifier = Modifier.height(6.dp))

                        userPreferences.getMockFriends().forEach { friend ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SystemSurfaceElevated)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(friend.name, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = TextPrimary)
                                        Text("${friend.rank} • Blueprint: ${friend.characterBlueprint}", fontSize = 9.sp, color = TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${friend.streak} Day Streak", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RankGold)
                                        Text("${friend.roadworkKm} km run", fontSize = 9.sp, color = SystemCyan)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    SystemFeedbackHelper.triggerHaptic(context)
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "ARISE: Monarch Contract System\nHunter: ${userPreferences.userName}\nRank: ${userPreferences.getHunterRank()}\nStreak: ${userPreferences.streakDays} Days 🔥\nRoadwork: ${userPreferences.totalRoadworkKm} km\nJoin the 30-Day Sovereign Challenge!"
                                        )
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Hunter Stats")
                                    context.startActivity(shareIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                                shape = CutCornerShape(4.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SHARE STAT CARD", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // Section 7: Action Triggers (Solo Leveling Penalty Zone & Memory Lane Time-Lapse)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        SystemFeedbackHelper.triggerHaptic(context)
                        showTransformationMemoryModal = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MonarchPurple, contentColor = Color.White),
                    shape = CutCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(44.dp).testTag("status_memory_lane_button")
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MEMORY LANE", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = {
                        SystemFeedbackHelper.triggerHaptic(context)
                        userPreferences.isPenaltyLockActive = true
                        showPenaltyModal = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPenaltyRed.copy(alpha = 0.2f), contentColor = CrimsonPenaltyRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPenaltyRed),
                    shape = CutCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(44.dp).testTag("enter_penalty_zone_button")
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PENALTY ZONE", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 8: SYSTEM NOTIFICATION DISPATCH & LIVE CAMERA PROTOCOLS
        item {
            var isCameraEnforced by remember { mutableStateOf(userPreferences.isCameraEnforcementEnabled) }
            var notifTestStatus by remember { mutableStateOf<String?>(null) }

            val notifPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    SystemFeedbackHelper.triggerHaptic(context)
                    notifTestStatus = "SYSTEM PERMISSION GRANTED! Alerts will dispatch to status bar."
                } else {
                    notifTestStatus = "Notification permission denied in OS settings."
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = SystemCyan,
                backgroundColor = SystemSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SYSTEM NOTIFICATIONS & CAMERA WITNESS",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Camera Witness Enforcement Toggle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(SystemSurface)
                            .border(1.dp, if (isCameraEnforced) SystemCyan else SystemSurfaceBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                SystemFeedbackHelper.triggerHaptic(context)
                                val newVal = !isCameraEnforced
                                isCameraEnforced = newVal
                                userPreferences.isCameraEnforcementEnabled = newVal
                            }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = if (isCameraEnforced) SystemCyan else TextMuted, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("LIVE CAMERA REP ENFORCEMENT", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(
                                        if (isCameraEnforced) "STRICT: Exercises are locked until reps are shown on camera" else "MANUAL: Checkboxes can be toggled without camera",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CutCornerShape(4.dp))
                                    .background(if (isCameraEnforced) SystemCyan else SystemSurfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (isCameraEnforced) "ENFORCED" else "OPTIONAL", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = if (isCameraEnforced) Color.Black else TextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "DUOLINGO-STYLE SYSTEM ALERTS & TIMERS:",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = RankGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Button(
                            onClick = { notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            colors = ButtonDefaults.buttonColors(containerColor = SystemCyan.copy(alpha = 0.2f), contentColor = SystemCyan),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("AUTHORIZE ANDROID 13+ SYSTEM NOTIFICATIONS", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                SystemNotificationHelper.sendDailyQuestNotification(context, 1, activeCharacter.name)
                                notifTestStatus = "Quest alert dispatched to Android Notification shade!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SystemSurface, contentColor = SystemCyan),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("TEST QUEST", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                SystemNotificationHelper.sendPenaltyWarningNotification(context, 2)
                                notifTestStatus = "🚨 Urgent 2-Hr Penalty lockout warning dispatched!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPenaltyRed.copy(alpha = 0.2f), contentColor = CrimsonPenaltyRed),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("TEST PENALTY", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                SystemNotificationHelper.sendStreakEvolutionNotification(context, streakDays, streakTier.title)
                                notifTestStatus = "🔥 Streak flame notification dispatched!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RankGold.copy(alpha = 0.2f), contentColor = RankGold),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("TEST STREAK", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (notifTestStatus != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = notifTestStatus!!, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = SystemCyan)
                    }
                }
            }
        }
    }

    // Google / Hunter ID Auth Modal
    if (showAuthDialog) {
        var authMode by remember { mutableStateOf("google") }
        var nameInput by remember { mutableStateOf("Hunter Jinwoo") }
        var emailInput by remember { mutableStateOf("hunter.shadow@monarch.system") }
        var hunterCodeInput by remember { mutableStateOf("HUNTER#9901") }

        Dialog(onDismissRequest = { showAuthDialog = false }) {
            CyberCard(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                borderColor = SystemCyan,
                backgroundColor = SystemAmoledBg
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HUNTER ACCESS & LOGIN", fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = SystemCyan)
                        IconButton(onClick = { showAuthDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { authMode = "google" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMode == "google") SystemCyan else SystemSurfaceElevated,
                                contentColor = if (authMode == "google") Color.Black else TextSecondary
                            ),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("GOOGLE SIGN IN", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { authMode = "hunter" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMode == "hunter") MonarchPurple else SystemSurfaceElevated,
                                contentColor = Color.White
                            ),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("HUNTER ID", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (authMode == "google") {
                        Text("Connect your Google Account to backup your 30-day physique memories, streak data, and sync with hunter friends.", fontSize = 10.sp, color = TextSecondary, lineHeight = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        StatInputField("Hunter Name", nameInput) { nameInput = it }
                        Spacer(modifier = Modifier.height(6.dp))
                        StatInputField("Google Email", emailInput) { emailInput = it }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                userPreferences.loginWithGoogle(nameInput, emailInput)
                                showAuthDialog = false
                                SystemFeedbackHelper.playSuccessFanfare()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("CONFIRM GOOGLE ACCOUNT SIGN-IN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    } else {
                        Text("Enter your official Hunter Association identification credentials.", fontSize = 10.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        StatInputField("Hunter Call-Sign", nameInput) { nameInput = it }
                        Spacer(modifier = Modifier.height(6.dp))
                        StatInputField("License ID Code", hunterCodeInput) { hunterCodeInput = it }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                userPreferences.loginWithHunterId(nameInput, hunterCodeInput)
                                showAuthDialog = false
                                SystemFeedbackHelper.playSuccessFanfare()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MonarchPurple, contentColor = Color.White),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("CONNECT HUNTER ID", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    }

                    if (userPreferences.isLoggedIn) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "SWITCH TO OFFLINE GUEST MODE",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CrimsonPenaltyRed,
                            modifier = Modifier.clickable {
                                userPreferences.logout()
                                showAuthDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    // Transformation Memory Modal
    if (showTransformationMemoryModal) {
        TransformationMemoryModal(
            character = activeCharacter,
            userPreferences = userPreferences,
            onDismiss = { showTransformationMemoryModal = false },
            onAwakeningComplete = {
                showTransformationMemoryModal = false
                equippedBadge = userPreferences.equippedBadge
                hunterRank = userPreferences.getHunterRank()
                totalXp = userPreferences.totalXp
            }
        )
    }

    // Solo Leveling Penalty Zone Lockout Screen
    if (showPenaltyModal || userPreferences.isPenaltyLockActive) {
        PenaltyLockoutScreen(
            userPreferences = userPreferences,
            onPenaltyCleared = {
                showPenaltyModal = false
                totalXp = userPreferences.totalXp
                hunterRank = userPreferences.getHunterRank()
            }
        )
    }

    // Edit Stats Dialog
    if (showEditStatsDialog) {
        var weightInput by remember { mutableStateOf(userWeight.toString()) }
        var heightInput by remember { mutableStateOf(userHeight.toString()) }
        var shoulderInput by remember { mutableStateOf(shoulderCm.toString()) }
        var chestInput by remember { mutableStateOf(chestCm.toString()) }
        var waistInput by remember { mutableStateOf(waistCm.toString()) }
        var armsInput by remember { mutableStateOf(armsCm.toString()) }

        Dialog(onDismissRequest = { showEditStatsDialog = false }) {
            CyberCard(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                borderColor = SystemCyan,
                backgroundColor = SystemAmoledBg
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("EDIT BIOMETRIC STATS", fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = SystemCyan)
                        IconButton(onClick = { showEditStatsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    StatInputField("Weight (kg)", weightInput) { weightInput = it }
                    Spacer(modifier = Modifier.height(6.dp))
                    StatInputField("Height (cm)", heightInput) { heightInput = it }
                    Spacer(modifier = Modifier.height(6.dp))
                    StatInputField("Shoulders (cm)", shoulderInput) { shoulderInput = it }
                    Spacer(modifier = Modifier.height(6.dp))
                    StatInputField("Chest (cm)", chestInput) { chestInput = it }
                    Spacer(modifier = Modifier.height(6.dp))
                    StatInputField("Waist (cm)", waistInput) { waistInput = it }
                    Spacer(modifier = Modifier.height(6.dp))
                    StatInputField("Arms (cm)", armsInput) { armsInput = it }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            userWeight = weightInput.toFloatOrNull() ?: userWeight
                            userHeight = heightInput.toFloatOrNull() ?: userHeight
                            shoulderCm = shoulderInput.toFloatOrNull() ?: shoulderCm
                            chestCm = chestInput.toFloatOrNull() ?: chestCm
                            waistCm = waistInput.toFloatOrNull() ?: waistCm
                            armsCm = armsInput.toFloatOrNull() ?: armsCm

                            userPreferences.userWeightKg = userWeight
                            userPreferences.userHeightCm = userHeight
                            userPreferences.shoulderCm = shoulderCm
                            userPreferences.chestCm = chestCm
                            userPreferences.waistCm = waistCm
                            userPreferences.armsCm = armsCm

                            showEditStatsDialog = false
                            SystemFeedbackHelper.playSuccessFanfare()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                        shape = CutCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("SAVE & UPDATE V-TAPER", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SystemSurface)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
        Text(value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun StatInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SystemCyan,
            unfocusedBorderColor = SystemSurfaceBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
