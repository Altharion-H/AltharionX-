package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserPreferences
import com.example.ui.components.SystemFeedbackHelper
import com.example.ui.screens.AiMealSystemScreen
import com.example.ui.screens.CombatTimerScreen
import com.example.ui.screens.DayTrackerScreen
import com.example.ui.screens.HunterStatusScreen
import com.example.ui.screens.PenaltyLockoutScreen
import com.example.ui.screens.RoadworkScreen
import com.example.ui.screens.RosterScannerScreen
import com.example.ui.theme.SystemAmoledBg
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceBorder
import com.example.ui.theme.TextMuted

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    ROSTER("ROSTER", Icons.Default.FitnessCenter, "tab_roster"),
    SYSTEM_30("30-DAY", Icons.Default.CalendarMonth, "tab_30_day"),
    ROADWORK("ROADWORK", Icons.AutoMirrored.Filled.DirectionsRun, "tab_roadwork"),
    AI_MEALS("AI MEALS", Icons.Default.Restaurant, "tab_ai_meals"),
    HUNTER_STATUS("HUNTER", Icons.Default.Shield, "tab_hunter")
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    var currentTab by remember { mutableStateOf(AppTab.ROSTER) }
    var showActiveCombatTimer by remember { mutableStateOf(false) }
    var timerPresetSeconds by remember { mutableIntStateOf(90) }
    var isPenaltyActive by remember { mutableStateOf(userPreferences.isPenaltyLockActive) }

    val activeTheme = userPreferences.activeThemeName
    val themeAccentColor = when (activeTheme) {
        "SHADOW_PURPLE" -> Color(0xFF9D4EDD)
        "CRIMSON_PENALTY" -> Color(0xFFFF0055)
        "MINIMALIST_ZEN" -> Color(0xFF2DD4BF)
        "GOLDEN_ESCANOR" -> Color(0xFFFFB800)
        "CYBERPUNK_NEON" -> Color(0xFFFFB703)
        else -> SystemCyan
    }

    val themeBgColor = when (activeTheme) {
        "SHADOW_PURPLE" -> Color(0xFF07030C)
        "CRIMSON_PENALTY" -> Color(0xFF090204)
        "MINIMALIST_ZEN" -> Color(0xFF0D1117)
        "GOLDEN_ESCANOR" -> Color(0xFF0A0804)
        "CYBERPUNK_NEON" -> Color(0xFF050711)
        else -> SystemAmoledBg
    }

    if (isPenaltyActive) {
        PenaltyLockoutScreen(
            userPreferences = userPreferences,
            onPenaltyCleared = {
                userPreferences.isPenaltyLockActive = false
                isPenaltyActive = false
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020306)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .background(themeBgColor)
                .border(0.8.dp, SystemSurfaceBorder.copy(alpha = 0.5f))
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = themeBgColor,
                contentWindowInsets = WindowInsets.navigationBars,
                bottomBar = {
                    NativeSystemBottomBar(
                        currentTab = currentTab,
                        accentColor = themeAccentColor,
                        onTabSelected = { tab ->
                            SystemFeedbackHelper.triggerHaptic(context)
                            SystemFeedbackHelper.playBeepTone()
                            showActiveCombatTimer = false
                            currentTab = tab
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (showActiveCombatTimer) {
                        CombatTimerScreen(
                            userPreferences = userPreferences,
                            initialRestSeconds = timerPresetSeconds
                        )
                    } else {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_crossfade"
                        ) { tab ->
                            when (tab) {
                                AppTab.ROSTER -> RosterScannerScreen(
                                    userPreferences = userPreferences,
                                    onContractSigned = {
                                        currentTab = AppTab.SYSTEM_30
                                    }
                                )
                                AppTab.SYSTEM_30 -> DayTrackerScreen(
                                    userPreferences = userPreferences,
                                    onNavigateToTimer = { restSec ->
                                        timerPresetSeconds = restSec
                                        showActiveCombatTimer = true
                                    },
                                    onNavigateToRoster = {
                                        currentTab = AppTab.ROSTER
                                    }
                                )
                                AppTab.ROADWORK -> RoadworkScreen(
                                    userPreferences = userPreferences
                                )
                                AppTab.AI_MEALS -> AiMealSystemScreen(
                                    userPreferences = userPreferences,
                                    onNavigateToRoster = {
                                        currentTab = AppTab.ROSTER
                                    }
                                )
                                AppTab.HUNTER_STATUS -> HunterStatusScreen(
                                    userPreferences = userPreferences,
                                    onNavigateToRoster = {
                                        currentTab = AppTab.ROSTER
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NativeSystemBottomBar(
    currentTab: AppTab,
    accentColor: Color,
    onTabSelected: (AppTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SystemSurface)
            .border(1.dp, SystemSurfaceBorder)
            .padding(vertical = 6.dp, horizontal = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                val iconTint = if (isSelected) accentColor else TextMuted
                val textCol = if (isSelected) accentColor else TextMuted

                Box(
                    modifier = Modifier
                        .clip(CutCornerShape(4.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.12f) else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .testTag(tab.tag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = iconTint,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.title,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = textCol,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
    }
}
