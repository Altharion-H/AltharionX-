package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AnimeCharacter
import com.example.data.CharacterRepository
import com.example.data.DailyWorkout
import com.example.data.UserPreferences
import com.example.data.WorkoutProtocolGenerator
import com.example.ui.components.CyberCard
import com.example.ui.components.LiveCameraRepCounterModal
import com.example.ui.components.SystemFeedbackHelper
import com.example.ui.components.SystemHeader
import com.example.ui.components.SystemNotificationHelper
import com.example.ui.theme.CrimsonPenaltyRed
import com.example.ui.theme.MonarchPurple
import com.example.ui.theme.RankGold
import com.example.ui.theme.SystemAmoledBg
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemGreen
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceBorder
import com.example.ui.theme.SystemSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayTrackerScreen(
    userPreferences: UserPreferences,
    onNavigateToTimer: (Int) -> Unit,
    onNavigateToRoster: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val hasContract = userPreferences.isContractSigned && userPreferences.activeCharacterId.isNotBlank()
    val activeCharacter = remember(userPreferences.activeCharacterId) {
        if (hasContract) CharacterRepository.getCharacterById(userPreferences.activeCharacterId)
        else CharacterRepository.characters.first()
    }

    val workouts30Days = remember(activeCharacter.id, userPreferences.isHellMode) {
        WorkoutProtocolGenerator.generate30Days(activeCharacter, userPreferences.isHellMode)
    }

    var selectedDayNumber by remember { mutableStateOf<Int?>(null) }
    var completedDays by remember { mutableStateOf(userPreferences.getCompletedDays()) }
    var isGymMode by remember { mutableStateOf(userPreferences.isGymMode) }

    // Developer Test Day 30 Photo Judgment modal & Memory Lane Time-Lapse Studio modal
    var showDevDay30Modal by remember { mutableStateOf(false) }
    var showMemoryLaneModal by remember { mutableStateOf(false) }
    var memoryLaneTargetDay by remember { mutableIntStateOf(1) }

    if (!hasContract) {
        // Uncontracted Hunter Guard
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SystemAmoledBg)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CrimsonPenaltyRed.copy(alpha = 0.15f))
                    .border(1.5.dp, CrimsonPenaltyRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = CrimsonPenaltyRed, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NO CONTRACT SIGNED",
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You are currently an Uncontracted Hunter (0 / 30 Days). Please choose a character blueprint and sign the 30-Day Blood Oath Contract in the Roster tab to unlock Day 1.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onNavigateToRoster,
                colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                shape = CutCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("BROWSE 30 ROSTER & SIGN CONTRACT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        }
        return
    }

    if (selectedDayNumber != null) {
        BackHandler { selectedDayNumber = null }
        val dayWorkout = workouts30Days.find { it.dayNumber == selectedDayNumber } ?: workouts30Days.first()

        DailyQuestExecutionScreen(
            dailyWorkout = dayWorkout,
            character = activeCharacter,
            userPreferences = userPreferences,
            isGymMode = isGymMode,
            onToggleEquipment = { newMode ->
                isGymMode = newMode
                userPreferences.isGymMode = newMode
                SystemFeedbackHelper.triggerHaptic(context)
            },
            onBack = { selectedDayNumber = null },
            onDayCompletedToggle = { day, isDone ->
                userPreferences.setDayCompleted(day, isDone)
                completedDays = userPreferences.getCompletedDays()
                if (isDone) {
                    userPreferences.addXp(dayWorkout.xpReward)
                    SystemFeedbackHelper.playSuccessFanfare()
                }
            },
            onTriggerTimer = onNavigateToTimer,
            onOpenMemoryLaneForDay = { day ->
                memoryLaneTargetDay = day
                showMemoryLaneModal = true
            }
        )
    } else {
        // 30-Day Grid Calendar Screen with Sequential Unlocking
        CalendarGridScreen(
            character = activeCharacter,
            workouts = workouts30Days,
            completedDays = completedDays,
            loggedPhotoCount = userPreferences.getAllDailyPhotos().size,
            isHellMode = userPreferences.isHellMode,
            onSelectDay = { dayNum ->
                SystemFeedbackHelper.triggerHaptic(context)
                selectedDayNumber = dayNum
            },
            onOpenDevTestJudgment = {
                showDevDay30Modal = true
            },
            onOpenMemoryLane = {
                memoryLaneTargetDay = (completedDays.maxOrNull() ?: 1).coerceIn(1, 30)
                showMemoryLaneModal = true
            },
            modifier = modifier
        )
    }

    if (showDevDay30Modal) {
        Day30EvaluationModal(
            character = activeCharacter,
            userPreferences = userPreferences,
            onDismiss = { showDevDay30Modal = false },
            onAwakeningComplete = {
                showDevDay30Modal = false
                completedDays = userPreferences.getCompletedDays()
            }
        )
    }

    if (showMemoryLaneModal) {
        TransformationMemoryModal(
            character = activeCharacter,
            userPreferences = userPreferences,
            initialUploadDay = memoryLaneTargetDay,
            onDismiss = { showMemoryLaneModal = false },
            onAwakeningComplete = {
                showMemoryLaneModal = false
                completedDays = userPreferences.getCompletedDays()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalendarGridScreen(
    character: AnimeCharacter,
    workouts: List<DailyWorkout>,
    completedDays: Set<Int>,
    loggedPhotoCount: Int,
    isHellMode: Boolean,
    onSelectDay: (Int) -> Unit,
    onOpenDevTestJudgment: () -> Unit,
    onOpenMemoryLane: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var lockedDayAlert by remember { mutableStateOf<Int?>(null) }

    val completionPercentage = remember(completedDays) {
        ((completedDays.size.toFloat() / 30f) * 100f).toInt()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SystemAmoledBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SystemHeader(
                    title = if (isHellMode) "HELL OVERLOAD (MONTH 2)" else "30-DAY MONARCH SYSTEM",
                    subtitle = "BOUND PROTOCOL: ${character.name.uppercase()}",
                    modifier = Modifier.weight(1f)
                )

                // Optional Testing Utility gear icon
                IconButton(
                    onClick = {
                        SystemFeedbackHelper.triggerHaptic(context)
                        onOpenDevTestJudgment()
                    },
                    modifier = Modifier.testTag("dev_test_gear_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Test Day 30 Photo Judgment",
                        tint = RankGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Progress Summary Card
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                borderColor = if (isHellMode) CrimsonPenaltyRed else SystemCyan,
                borderWidth = 1.dp,
                backgroundColor = SystemSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CALENDAR PROGRESSION",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${completedDays.size} / 30 DAYS CLEARED ($completionPercentage%)",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = if (isHellMode) CrimsonPenaltyRed else SystemCyan
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MonarchPurple.copy(alpha = 0.2f))
                                .border(1.dp, MonarchPurple, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = character.sovereignBadge.title.take(14),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = RankGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SystemSurfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(completionPercentage / 100f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isHellMode) CrimsonPenaltyRed else SystemCyan)
                        )
                    }
                }
            }
        }

        // 4 Weeks progression sections
        val weeks = listOf(
            1 to "WEEK 1 (DAYS 1-7): Foundation & Volume Conditioning",
            2 to "WEEK 2 (DAYS 8-14): Hypertrophy & Weighted Overload",
            3 to "WEEK 3 (DAYS 15-21): Peak Intensity & Supersets",
            4 to "WEEK 4 (DAYS 22-30): Limit Breaker & Monarch Trial"
        )

        weeks.forEach { (weekNum, weekTitle) ->
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = weekTitle.uppercase(),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = if (weekNum == 4) CrimsonPenaltyRed else TextPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            val weekDays = workouts.filter { it.weekNumber == weekNum }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weekDays.forEach { workout ->
                        val isDone = completedDays.contains(workout.dayNumber)
                        // Sequential Unlock Rule: Day 1 unlocked; Day N unlocked only if Day N-1 is completed
                        val isUnlocked = workout.dayNumber == 1 || completedDays.contains(workout.dayNumber - 1)

                        SequentialDayCell(
                            workout = workout,
                            isCompleted = isDone,
                            isUnlocked = isUnlocked,
                            onClick = {
                                if (isUnlocked) {
                                    onSelectDay(workout.dayNumber)
                                } else {
                                    SystemFeedbackHelper.playBeepTone()
                                    lockedDayAlert = workout.dayNumber
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (lockedDayAlert != null) {
        val lockedDay = lockedDayAlert!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { lockedDayAlert = null }) {
            CyberCard(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                borderColor = CrimsonPenaltyRed,
                backgroundColor = SystemAmoledBg
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CrimsonPenaltyRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DAY $lockedDay IS LOCKED",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = CrimsonPenaltyRed
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The Monarch Contract strictly enforces progressive overload. You must check off 100% of the exercises in Day ${lockedDay - 1} before Day $lockedDay unlocks.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { lockedDayAlert = null },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPenaltyRed, contentColor = Color.White),
                        shape = CutCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("UNDERSTOOD", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SequentialDayCell(
    workout: DailyWorkout,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isCompleted -> SystemCyan
        isUnlocked -> SystemSurfaceBorder
        else -> SystemSurfaceBorder.copy(alpha = 0.3f)
    }

    val bgColor = when {
        isCompleted -> SystemCyan.copy(alpha = 0.15f)
        isUnlocked -> SystemSurfaceElevated
        else -> SystemSurface.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .width(96.dp)
            .height(80.dp)
            .clip(CutCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, CutCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
            .testTag("calendar_day_${workout.dayNumber}"),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAY ${workout.dayNumber}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = if (isCompleted) SystemCyan else if (isUnlocked) TextPrimary else TextMuted
                )
                if (isCompleted) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Done", tint = SystemCyan, modifier = Modifier.size(12.dp))
                } else if (!isUnlocked) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = TextMuted, modifier = Modifier.size(12.dp))
                }
            }

            Text(
                text = if (!isUnlocked) "PADLOCKED" else workout.focusTitle.take(12),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = if (!isUnlocked) TextMuted else TextSecondary,
                maxLines = 1
            )

            Text(
                text = "+${workout.xpReward} XP",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) RankGold else TextMuted
            )
        }
    }
}

@Composable
private fun DailyQuestExecutionScreen(
    dailyWorkout: DailyWorkout,
    character: AnimeCharacter,
    userPreferences: UserPreferences,
    isGymMode: Boolean,
    onToggleEquipment: (Boolean) -> Unit,
    onBack: () -> Unit,
    onDayCompletedToggle: (Int, Boolean) -> Unit,
    onTriggerTimer: (Int) -> Unit,
    onOpenMemoryLaneForDay: (Int) -> Unit
) {
    val context = LocalContext.current
    var checkedExerciseIds by remember {
        mutableStateOf(userPreferences.getCheckedExercises(dailyWorkout.dayNumber))
    }
    var cameraVerifiedExercises by remember {
        mutableStateOf(userPreferences.getCameraVerifiedExercises(dailyWorkout.dayNumber))
    }
    var isDayCompleted by remember {
        mutableStateOf(userPreferences.getCompletedDays().contains(dailyWorkout.dayNumber))
    }
    var isBossFinisherChecked by remember { mutableStateOf(checkedExerciseIds.contains("boss_finisher")) }

    // Active camera exercise: Triple(exerciseId, targetReps, exerciseName)
    var activeCameraExercise by remember { mutableStateOf<Triple<String, Int, String>?>(null) }
    var enforcementAlertNotice by remember { mutableStateOf<String?>(null) }

    val exercises = if (isGymMode) dailyWorkout.exercisesGym else dailyWorkout.exercisesHome

    val allExercisesDone = remember(checkedExerciseIds, exercises, isBossFinisherChecked, cameraVerifiedExercises) {
        val exerciseIds = exercises.map { it.id }.toSet()
        val allChecked = checkedExerciseIds.containsAll(exerciseIds) && isBossFinisherChecked
        if (userPreferences.isCameraEnforcementEnabled) {
            allChecked && cameraVerifiedExercises.containsAll(exerciseIds) && cameraVerifiedExercises.contains("boss_finisher")
        } else {
            allChecked
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemAmoledBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Back Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_button_quest")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SystemCyan)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DAY ${dailyWorkout.dayNumber} QUEST EXECUTION",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Text(
                        text = dailyWorkout.phaseTitle,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (dailyWorkout.isHellModeActive) CrimsonPenaltyRed else MonarchPurple
                    )
                }
            }
        }

        // Live Camera Enforcement Banner
        item {
            Spacer(modifier = Modifier.height(8.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (userPreferences.isCameraEnforcementEnabled) SystemCyan else SystemSurfaceBorder,
                backgroundColor = SystemSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = if (userPreferences.isCameraEnforcementEnabled) SystemGreen else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "LIVE CAMERA WITNESS PROTOCOL",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = if (userPreferences.isCameraEnforcementEnabled)
                                    "ACTIVE: Every rep must be verified live on camera before progression unlocks"
                                else "MANUAL OVERRIDE: Camera verification optional",
                                fontSize = 9.sp,
                                color = TextSecondary,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Equipment Toggle
        item {
            Spacer(modifier = Modifier.height(8.dp))
            CyberCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SystemSurfaceElevated) {
                Row(modifier = Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CutCornerShape(4.dp))
                            .background(if (isGymMode) SystemCyan else SystemSurface)
                            .clickable { onToggleEquipment(true) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("FULL GYM / BARBELL", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = if (isGymMode) Color.Black else TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CutCornerShape(4.dp))
                            .background(if (!isGymMode) SystemCyan else SystemSurface)
                            .clickable { onToggleEquipment(false) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("HOME / CALISTHENICS", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = if (!isGymMode) Color.Black else TextSecondary)
                    }
                }
            }
        }

        // Exercises List
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "[DAILY HARDCORE EXERCISES: ${cameraVerifiedExercises.filter { it != "boss_finisher" }.size}/${exercises.size} CAMERA VERIFIED]",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (cameraVerifiedExercises.containsAll(exercises.map { it.id })) SystemGreen else TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(exercises, key = { it.id }) { exercise ->
            val isVerified = cameraVerifiedExercises.contains(exercise.id)
            val isChecked = checkedExerciseIds.contains(exercise.id)
            val parsedTargetReps = remember(exercise.reps) {
                val digits = exercise.reps.filter { it.isDigit() }
                digits.toIntOrNull() ?: 12
            }

            CyberCard(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                borderColor = if (isVerified) SystemGreen.copy(alpha = 0.7f) else if (isChecked) SystemCyan.copy(alpha = 0.5f) else SystemSurfaceBorder,
                backgroundColor = if (isVerified) SystemGreen.copy(alpha = 0.05f) else if (isChecked) SystemSurfaceElevated.copy(alpha = 0.6f) else SystemSurface
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                if (userPreferences.isCameraEnforcementEnabled && !isVerified) {
                                    // Gated: Force camera verification before manual checkbox
                                    SystemFeedbackHelper.playBeepTone()
                                    enforcementAlertNotice = "MONARCH PROTOCOL: ${exercise.name} requires ${parsedTargetReps} reps verified live on camera before checking off!"
                                    activeCameraExercise = Triple(exercise.id, parsedTargetReps, exercise.name)
                                } else {
                                    SystemFeedbackHelper.triggerHaptic(context)
                                    SystemFeedbackHelper.playBeepTone()
                                    userPreferences.toggleExerciseChecked(dailyWorkout.dayNumber, exercise.id)
                                    checkedExerciseIds = userPreferences.getCheckedExercises(dailyWorkout.dayNumber)
                                    if (!isChecked) {
                                        userPreferences.addXp(25)
                                        onTriggerTimer(exercise.restSeconds)
                                    }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = if (isVerified) SystemGreen else SystemCyan,
                                checkmarkColor = Color.Black,
                                uncheckedColor = SystemSurfaceBorder
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = exercise.name.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = if (isVerified) SystemGreen else if (isChecked) TextMuted else TextPrimary)
                                Text(text = exercise.targetMuscle, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MonarchPurple)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "${exercise.sets} SETS", fontSize = 10.sp, color = SystemCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text(text = "• ${exercise.reps} REPS", fontSize = 10.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                                Text(text = "• TEMPO: ${exercise.tempo}", fontSize = 10.sp, color = RankGold, fontFamily = FontFamily.Monospace)
                                Text(text = "• REST: ${exercise.restSeconds}s", fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = exercise.rpeCue, fontSize = 10.sp, color = if (isChecked) TextMuted else TextSecondary)
                        }
                    }

                    // Live Camera Rep Verification Action Button
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isVerified) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(SystemGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SystemGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CAMERA VERIFIED: ${userPreferences.getVerifiedReps(dailyWorkout.dayNumber, exercise.id)} REPS WITNESSED LIVE",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = SystemGreen
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                activeCameraExercise = Triple(exercise.id, parsedTargetReps, exercise.name)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SystemCyan.copy(alpha = 0.2f),
                                contentColor = SystemCyan
                            ),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("camera_launch_button_${exercise.id}")
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHOW $parsedTargetReps REPS LIVE ON CAMERA",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Daily Boss Finisher Quest (Also camera verified)
        item {
            val isBossVerified = cameraVerifiedExercises.contains("boss_finisher")
            Spacer(modifier = Modifier.height(12.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isBossVerified) SystemGreen else CrimsonPenaltyRed,
                borderWidth = 1.2.dp,
                backgroundColor = if (isBossVerified) SystemGreen.copy(alpha = 0.08f) else CrimsonPenaltyRed.copy(alpha = 0.08f)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = isBossFinisherChecked,
                            onCheckedChange = {
                                if (userPreferences.isCameraEnforcementEnabled && !isBossVerified) {
                                    SystemFeedbackHelper.playBeepTone()
                                    enforcementAlertNotice = "BOSS PROTOCOL: You must verify 50 Reps on camera before claiming this finisher!"
                                    activeCameraExercise = Triple("boss_finisher", 50, dailyWorkout.penaltyBossQuest)
                                } else {
                                    SystemFeedbackHelper.triggerHaptic(context)
                                    SystemFeedbackHelper.playBeepTone()
                                    userPreferences.toggleExerciseChecked(dailyWorkout.dayNumber, "boss_finisher")
                                    checkedExerciseIds = userPreferences.getCheckedExercises(dailyWorkout.dayNumber)
                                    isBossFinisherChecked = checkedExerciseIds.contains("boss_finisher")
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = if (isBossVerified) SystemGreen else CrimsonPenaltyRed,
                                checkmarkColor = Color.White,
                                uncheckedColor = CrimsonPenaltyRed
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MANDATORY BOSS FINISHER QUEST",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = if (isBossVerified) SystemGreen else CrimsonPenaltyRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = dailyWorkout.penaltyBossQuest, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = dailyWorkout.penaltyDescription, fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    if (isBossVerified) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(SystemGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SystemGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BOSS FINISHER CAMERA WITNESSED & CLEARED (+50 XP)",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = SystemGreen
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                activeCameraExercise = Triple("boss_finisher", 50, dailyWorkout.penaltyBossQuest)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPenaltyRed.copy(alpha = 0.25f),
                                contentColor = CrimsonPenaltyRed
                            ),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("camera_boss_finisher_button")
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHOW BOSS FINISHER ON CAMERA (50 REPS)",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Daily Progress Photo Check-In for Memory Lane Time-Lapse
        item {
            val hasPhotoToday = !userPreferences.getDailyPhoto(dailyWorkout.dayNumber).isNullOrBlank()
            Spacer(modifier = Modifier.height(12.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (hasPhotoToday) SystemGreen else MonarchPurple,
                backgroundColor = SystemSurfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (hasPhotoToday) Icons.Default.CheckCircle else Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = if (hasPhotoToday) SystemGreen else SystemCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (hasPhotoToday) "DAY ${dailyWorkout.dayNumber} PHOTO LOGGED IN MEMORY LANE" else "MEMORY LANE • DAY ${dailyWorkout.dayNumber} PHOTO CHECK-IN",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = if (hasPhotoToday) SystemGreen else TextPrimary
                            )
                            Text(
                                text = "Log daily physique snapshots to animate your weekly & 30-day time-lapse reel",
                                fontSize = 9.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            onOpenMemoryLaneForDay(dailyWorkout.dayNumber)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MonarchPurple,
                            contentColor = Color.White
                        ),
                        shape = CutCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp).testTag("daily_photo_memory_lane_button")
                    ) {
                        Text(
                            text = if (hasPhotoToday) "TIME-LAPSE" else "LOG PHOTO",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Complete Day Button (Only enabled when 100% of exercises and Boss Finisher are camera verified & checked)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    SystemFeedbackHelper.triggerHaptic(context)
                    val newCompleted = !isDayCompleted
                    isDayCompleted = newCompleted
                    onDayCompletedToggle(dailyWorkout.dayNumber, newCompleted)
                    if (newCompleted) {
                        SystemNotificationHelper.sendDailyQuestNotification(
                            context,
                            dailyWorkout.dayNumber + 1,
                            character.name
                        )
                    }
                },
                enabled = allExercisesDone || isDayCompleted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDayCompleted) SystemGreen.copy(alpha = 0.25f) else SystemCyan,
                    contentColor = if (isDayCompleted) SystemGreen else Color.Black,
                    disabledContainerColor = SystemSurfaceElevated,
                    disabledContentColor = TextMuted
                ),
                shape = CutCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("claim_day_quest_reward_button")
            ) {
                if (isDayCompleted) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "DAY ${dailyWorkout.dayNumber} QUEST CLEARED (+${dailyWorkout.xpReward} XP)", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                } else if (!allExercisesDone) {
                    Text(
                        text = if (userPreferences.isCameraEnforcementEnabled)
                            "VERIFY 100% EXERCISES ON LIVE CAMERA TO UNLOCK DAY ${dailyWorkout.dayNumber + 1}"
                        else "CHECK OFF 100% EXERCISES & BOSS TO UNLOCK DAY ${dailyWorkout.dayNumber + 1}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                } else {
                    Icon(imageVector = Icons.Default.Whatshot, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "CLAIM DAY ${dailyWorkout.dayNumber} REWARD & UNLOCK DAY ${dailyWorkout.dayNumber + 1}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }

    // Live Camera Repetition Counter Modal
    activeCameraExercise?.let { (exId, targetReps, exName) ->
        val muscle = exercises.find { it.id == exId }?.targetMuscle ?: "Full Body / Core"
        val rest = exercises.find { it.id == exId }?.restSeconds ?: 60

        LiveCameraRepCounterModal(
            exerciseName = exName,
            targetReps = targetReps,
            targetMuscle = muscle,
            onVerificationCompleted = { countedReps ->
                userPreferences.markExerciseCameraVerified(dailyWorkout.dayNumber, exId, countedReps)
                cameraVerifiedExercises = userPreferences.getCameraVerifiedExercises(dailyWorkout.dayNumber)
                checkedExerciseIds = userPreferences.getCheckedExercises(dailyWorkout.dayNumber)
                if (exId == "boss_finisher") {
                    isBossFinisherChecked = true
                }
                userPreferences.addXp(25)
                onTriggerTimer(rest)
                activeCameraExercise = null
            },
            onDismiss = { activeCameraExercise = null }
        )
    }

    // Camera Enforcement Alert Notice
    enforcementAlertNotice?.let { notice ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { enforcementAlertNotice = null }) {
            CyberCard(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                borderColor = SystemCyan,
                backgroundColor = SystemAmoledBg
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE CAMERA WITNESS REQUIRED",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = SystemCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = notice,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { enforcementAlertNotice = null },
                        colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                        shape = CutCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("LAUNCH CAMERA & EXECUTE REPS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
