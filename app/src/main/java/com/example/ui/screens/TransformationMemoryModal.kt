package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.AnimeCharacter
import com.example.data.GeminiVisionService
import com.example.data.MemoryLaneFrame
import com.example.data.UserPreferences
import com.example.ui.components.CyberCard
import com.example.ui.components.RealTimeCameraBodyScannerModal
import com.example.ui.components.SystemFeedbackHelper
import com.example.ui.theme.CrimsonPenaltyRed
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
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

enum class MemoryLaneFilterMode(val label: String) {
    ALL_30_DAYS("30-DAY REEL"),
    WEEKLY_STAGES("WEEKLY STAGES"),
    UPLOADED_ONLY("LOGGED PHOTOS")
}

@Composable
fun TransformationMemoryModal(
    character: AnimeCharacter,
    userPreferences: UserPreferences,
    initialUploadDay: Int = 1,
    onDismiss: () -> Unit,
    onAwakeningComplete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedWeekStage by remember { mutableIntStateOf(0) }
    var uploadDayTarget by remember { mutableIntStateOf(initialUploadDay) }

    val completedDaysCount = remember { userPreferences.getCompletedDays().size }
    val isDay30Reached = completedDaysCount >= 30

    var dailyPhotos by remember {
        mutableStateOf(userPreferences.getAllDailyPhotos())
    }

    val memoryReport = remember(character, dailyPhotos) {
        GeminiVisionService.buildTransformationMemory(character, dailyPhotos)
    }

    val all30Frames = remember(character, dailyPhotos) {
        GeminiVisionService.buildMemoryLaneFrames(character, dailyPhotos)
    }

    // Time-Lapse Animation Player State
    var filterMode by remember { mutableStateOf(MemoryLaneFilterMode.ALL_30_DAYS) }
    var isPlaying by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) } // 0.5x, 1x, 2x
    var isSplitCompareMode by remember { mutableStateOf(false) }
    var splitSliderFraction by remember { mutableFloatStateOf(0.5f) }
    var showLiveCameraForDay by remember { mutableStateOf(false) }

    // Filtered playlist of frames for the Time-Lapse reel
    val activePlaylist: List<MemoryLaneFrame> = remember(all30Frames, filterMode, dailyPhotos) {
        when (filterMode) {
            MemoryLaneFilterMode.ALL_30_DAYS -> all30Frames
            MemoryLaneFilterMode.WEEKLY_STAGES -> all30Frames.filter {
                it.dayNumber in listOf(1, 7, 14, 21, 28, 30)
            }
            MemoryLaneFilterMode.UPLOADED_ONLY -> {
                val uploadedDays = dailyPhotos.keys.sorted()
                if (uploadedDays.isNotEmpty()) {
                    all30Frames.filter { it.dayNumber in uploadedDays }
                } else {
                    all30Frames.filter { it.dayNumber in listOf(1, 7, 14, 21, 30) }
                }
            }
        }
    }

    var currentFrameIndex by remember { mutableIntStateOf(0) }
    val safeIndex = currentFrameIndex.coerceIn(0, (activePlaylist.size - 1).coerceAtLeast(0))
    val currentFrame = activePlaylist.getOrElse(safeIndex) { all30Frames.first() }

    // Gemini Live AI Commentary & Voice TTS
    var aiCommentary by remember { mutableStateOf<String?>(null) }
    var isGeneratingAiCommentary by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.US
            }
        }
        ttsEngine = engine
        onDispose {
            engine?.stop()
            engine?.shutdown()
        }
    }

    // Automatic Time-Lapse Loop
    LaunchedEffect(isPlaying, playbackSpeed, activePlaylist.size, isSplitCompareMode) {
        if (!isPlaying || isSplitCompareMode || activePlaylist.isEmpty()) return@LaunchedEffect
        val baseIntervalMs = when {
            filterMode == MemoryLaneFilterMode.WEEKLY_STAGES -> 1400L
            else -> 650L
        }
        val frameDelay = (baseIntervalMs / playbackSpeed).toLong().coerceAtLeast(180L)
        while (isPlaying) {
            delay(frameDelay)
            currentFrameIndex = (currentFrameIndex + 1) % activePlaylist.size
            val frame = activePlaylist[currentFrameIndex]
            selectedWeekStage = (frame.weekNumber - 1).coerceIn(0, 4)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            userPreferences.saveDailyPhoto(uploadDayTarget, uri.toString())
            dailyPhotos = userPreferences.getAllDailyPhotos()
            SystemFeedbackHelper.triggerHaptic(context)
            SystemFeedbackHelper.playSuccessFanfare()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF205070B))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                borderColor = SystemCyan,
                borderWidth = 1.5.dp,
                backgroundColor = SystemAmoledBg
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.MovieFilter, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MEMORY LANE • TIME-LAPSE STUDIO",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = SystemCyan
                                    )
                                )
                                Text(
                                    text = "AUTO-ANIMATED WEEKLY & 30-DAY TRANSFORMATION REEL",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = RankGold
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode Switcher: Time-Lapse Animation vs Interactive Before/After Split Wipe
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CutCornerShape(4.dp))
                                .background(if (!isSplitCompareMode) SystemCyan else SystemSurfaceElevated)
                                .clickable {
                                    SystemFeedbackHelper.triggerHaptic(context)
                                    isSplitCompareMode = false
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (!isSplitCompareMode) Color.Black else SystemCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AUTO TIME-LAPSE REEL",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = if (!isSplitCompareMode) Color.Black else TextPrimary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CutCornerShape(4.dp))
                                .background(if (isSplitCompareMode) RankGold else SystemSurfaceElevated)
                                .clickable {
                                    SystemFeedbackHelper.triggerHaptic(context)
                                    isSplitCompareMode = true
                                    isPlaying = false
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Compare,
                                    contentDescription = null,
                                    tint = if (isSplitCompareMode) Color.Black else RankGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "DAY 1 VS 30 SPLIT WIPE",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSplitCompareMode) Color.Black else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isSplitCompareMode) {
                        // ===================================================================
                        // 1. MEMORY LANE AUTO TIME-LAPSE ANIMATION STAGE
                        // ===================================================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            MemoryLaneFilterMode.entries.forEach { mode ->
                                val isSel = filterMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSel) MonarchPurple.copy(alpha = 0.3f) else SystemSurface)
                                        .border(1.dp, if (isSel) MonarchPurple else SystemSurfaceBorder, RoundedCornerShape(4.dp))
                                        .clickable {
                                            SystemFeedbackHelper.triggerHaptic(context)
                                            filterMode = mode
                                            currentFrameIndex = 0
                                        }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.label,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) SystemCyan else TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Animated Time-Lapse Viewport
                        val animatedMorph by animateFloatAsState(
                            targetValue = currentFrame.morphProgress,
                            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                            label = "morph_progress"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(CutCornerShape(8.dp))
                                .background(Color(0xFF070A12))
                                .border(1.5.dp, if (currentFrame.dayNumber == 30) RankGold else SystemCyan, CutCornerShape(8.dp))
                                .testTag("memory_lane_timelapse_viewport")
                        ) {
                            // Crossfading Daily Progress Photo (if uploaded)
                            Crossfade(
                                targetState = currentFrame.photoUri,
                                animationSpec = tween(350),
                                label = "photo_crossfade"
                            ) { uriString ->
                                if (!uriString.isNullOrBlank()) {
                                    AsyncImage(
                                        model = Uri.parse(uriString),
                                        contentDescription = "Day ${currentFrame.dayNumber} Progress Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            // Dynamic Holographic V-Taper Morphing Silhouette & Cyberpunk Biometric Grid Overlay
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Subtle dark gradient for HUD legibility
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xB305070B),
                                            Color(0x4005070B),
                                            Color(0xCC05070B)
                                        )
                                    )
                                )

                                // Biometric Grid Lines
                                val gridCols = 6
                                val gridRows = 5
                                for (c in 1 until gridCols) {
                                    val x = (w / gridCols) * c
                                    drawLine(
                                        color = SystemCyan.copy(alpha = 0.12f),
                                        start = Offset(x, 0f),
                                        end = Offset(x, h),
                                        strokeWidth = 1f
                                    )
                                }
                                for (r in 1 until gridRows) {
                                    val y = (h / gridRows) * r
                                    drawLine(
                                        color = SystemCyan.copy(alpha = 0.12f),
                                        start = Offset(0f, y),
                                        end = Offset(w, y),
                                        strokeWidth = 1f
                                    )
                                }

                                // Animated V-Taper Anatomical Silhouette that widens shoulders/lats & tapers waist from Day 1 -> Day 30
                                val centerX = w * 0.5f
                                val shoulderHalfWidth = (w * 0.18f) + (w * 0.08f * animatedMorph)
                                val latHalfWidth = (w * 0.15f) + (w * 0.07f * animatedMorph)
                                val waistHalfWidth = (w * 0.13f) - (w * 0.035f * animatedMorph)
                                val hipHalfWidth = (w * 0.14f) - (w * 0.015f * animatedMorph)

                                val neckY = h * 0.24f
                                val shoulderY = h * 0.30f
                                val latY = h * 0.46f
                                val waistY = h * 0.64f
                                val hipY = h * 0.78f

                                val silhouetteColor = if (currentFrame.dayNumber == 30) RankGold else SystemCyan
                                val hasPhoto = !currentFrame.photoUri.isNullOrBlank()
                                val fillAlpha = if (hasPhoto) 0.14f else 0.28f

                                val torsoPath = Path().apply {
                                    moveTo(centerX - (w * 0.045f), neckY)
                                    lineTo(centerX + (w * 0.045f), neckY)
                                    lineTo(centerX + shoulderHalfWidth, shoulderY)
                                    lineTo(centerX + latHalfWidth, latY)
                                    lineTo(centerX + waistHalfWidth, waistY)
                                    lineTo(centerX + hipHalfWidth, hipY)
                                    lineTo(centerX - hipHalfWidth, hipY)
                                    lineTo(centerX - waistHalfWidth, waistY)
                                    lineTo(centerX - latHalfWidth, latY)
                                    lineTo(centerX - shoulderHalfWidth, shoulderY)
                                    close()
                                }

                                drawPath(
                                    path = torsoPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            silhouetteColor.copy(alpha = fillAlpha),
                                            MonarchPurple.copy(alpha = fillAlpha * 0.8f)
                                        )
                                    )
                                )
                                drawPath(
                                    path = torsoPath,
                                    color = silhouetteColor.copy(alpha = if (hasPhoto) 0.65f else 0.9f),
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                // Clavicle Horizontal Vector Line
                                drawLine(
                                    color = silhouetteColor,
                                    start = Offset(centerX - shoulderHalfWidth, shoulderY),
                                    end = Offset(centerX + shoulderHalfWidth, shoulderY),
                                    strokeWidth = 1.8.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                                )

                                // Waist Taper Vector Line
                                drawLine(
                                    color = NeonGreen,
                                    start = Offset(centerX - waistHalfWidth, waistY),
                                    end = Offset(centerX + waistHalfWidth, waistY),
                                    strokeWidth = 1.8.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                                )
                            }

                            // Top Overlay: Day Badge + Phase Title
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CutCornerShape(4.dp))
                                        .background(if (currentFrame.dayNumber == 30) RankGold else SystemCyan)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "DAY ${String.format("%02d", currentFrame.dayNumber)} / 30",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .border(1.dp, MonarchPurple, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = currentFrame.phaseTitle,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = RankGold
                                    )
                                }
                            }

                            // Bottom Overlay: Real-Time Evolving Biometric Telemetry Strip
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.82f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = currentFrame.frameHighlight,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("BF: ${currentFrame.estimatedBodyFat}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = NeonGreen)
                                    Text("V-TAPER: ${currentFrame.vTaperRatio}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = SystemCyan)
                                    Text("LEAN: ${currentFrame.muscleDeltaKg}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = RankGold)
                                    Text("WAIST: ${currentFrame.waistDeltaCm}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = MonarchPurple)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Time-Lapse Playback Controls Deck (Play/Pause, Scrubber, Speed, Replay)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(SystemSurfaceElevated)
                                .border(1.dp, SystemSurfaceBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Play / Pause Button
                                    Button(
                                        onClick = {
                                            SystemFeedbackHelper.triggerHaptic(context)
                                            isPlaying = !isPlaying
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isPlaying) CrimsonPenaltyRed.copy(alpha = 0.25f) else SystemCyan,
                                            contentColor = if (isPlaying) CrimsonPenaltyRed else Color.Black
                                        ),
                                        shape = CutCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("memory_lane_play_pause_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isPlaying) "PAUSE REEL" else "PLAY REEL",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    // Speed Selector (0.5x, 1x, 2x)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(0.5f to "0.5x", 1f to "1x", 2f to "2x").forEach { (spd, label) ->
                                            val isSpd = playbackSpeed == spd
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isSpd) SystemCyan else SystemSurface)
                                                    .clickable {
                                                        SystemFeedbackHelper.triggerHaptic(context)
                                                        playbackSpeed = spd
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 9.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isSpd) Color.Black else TextSecondary
                                                )
                                            }
                                        }
                                    }

                                    // Restart Reel Button
                                    IconButton(
                                        onClick = {
                                            SystemFeedbackHelper.triggerHaptic(context)
                                            currentFrameIndex = 0
                                            isPlaying = true
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Replay, contentDescription = "Restart Time-Lapse", tint = SystemCyan, modifier = Modifier.size(16.dp))
                                    }
                                }

                                // Interactive Day Scrubber Slider
                                if (activePlaylist.size > 1) {
                                    Slider(
                                        value = safeIndex.toFloat(),
                                        onValueChange = { newVal ->
                                            isPlaying = false
                                            currentFrameIndex = newVal.toInt().coerceIn(0, activePlaylist.size - 1)
                                        },
                                        valueRange = 0f..(activePlaylist.size - 1).toFloat(),
                                        colors = SliderDefaults.colors(
                                            thumbColor = SystemCyan,
                                            activeTrackColor = SystemCyan,
                                            inactiveTrackColor = SystemSurfaceBorder
                                        ),
                                        modifier = Modifier.fillMaxWidth().height(26.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // ===================================================================
                        // 2. INTERACTIVE DAY 1 VS DAY 30 SPLIT COMPARISON WIPE
                        // ===================================================================
                        val day1Uri = dailyPhotos[1] ?: userPreferences.day1PhotoUri
                        val day30Uri = dailyPhotos[30] ?: userPreferences.day30PhotoUri

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(CutCornerShape(8.dp))
                                .background(SystemSurfaceElevated)
                                .border(1.5.dp, RankGold, CutCornerShape(8.dp))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                // Left side: Day 1 Baseline (Width controlled by splitSliderFraction)
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(splitSliderFraction.coerceIn(0.15f, 0.85f))
                                        .background(SystemSurface)
                                        .clickable {
                                            uploadDayTarget = 1
                                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day1Uri.isNotBlank()) {
                                        AsyncImage(
                                            model = Uri.parse(day1Uri),
                                            contentDescription = "Day 1",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(26.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("DAY 1 BASELINE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = SystemCyan)
                                            Text("BF: 16.4% • V-Taper 1.34", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black.copy(alpha = 0.75f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("DAY 01 BEFORE", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = SystemCyan)
                                    }
                                }

                                // Divider Handle
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .fillMaxHeight()
                                        .background(RankGold)
                                )

                                // Right side: Day 30 Finale
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight((1f - splitSliderFraction).coerceIn(0.15f, 0.85f))
                                        .background(Color(0xFF0A101D))
                                        .clickable {
                                            uploadDayTarget = 30
                                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day30Uri.isNotBlank()) {
                                        AsyncImage(
                                            model = Uri.parse(day30Uri),
                                            contentDescription = "Day 30",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = RankGold, modifier = Modifier.size(26.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("DAY 30 SOVEREIGN", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = RankGold)
                                            Text("BF: 11.0% • V-Taper 1.62", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = NeonGreen)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black.copy(alpha = 0.75f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("DAY 30 AFTER", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = NeonGreen)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "DRAG SLIDER TO WIPE BETWEEN DAY 1 BASELINE & DAY 30 FINALE",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = RankGold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Slider(
                            value = splitSliderFraction,
                            onValueChange = { splitSliderFraction = it },
                            valueRange = 0.15f..0.85f,
                            colors = SliderDefaults.colors(
                                thumbColor = RankGold,
                                activeTrackColor = SystemCyan,
                                inactiveTrackColor = NeonGreen
                            ),
                            modifier = Modifier.fillMaxWidth().height(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ===================================================================
                    // 3. 30-DAY DAILY PROGRESS PHOTO FILMSTRIP & CAMERA CAPTURE
                    // ===================================================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY PROGRESS FILMSTRIP (${dailyPhotos.size}/30 PHOTOS)",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = SystemCyan
                        )
                        Text(
                            text = "SELECTED: DAY $uploadDayTarget",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RankGold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items((1..30).toList()) { dayNum ->
                            val hasPhotoForDay = !dailyPhotos[dayNum].isNullOrBlank()
                            val isSelectedDay = uploadDayTarget == dayNum
                            Box(
                                modifier = Modifier
                                    .size(width = 48.dp, height = 56.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isSelectedDay -> SystemCyan.copy(alpha = 0.25f)
                                            hasPhotoForDay -> NeonGreen.copy(alpha = 0.15f)
                                            else -> SystemSurfaceElevated
                                        }
                                    )
                                    .border(
                                        width = if (isSelectedDay) 1.5.dp else 1.dp,
                                        color = when {
                                            isSelectedDay -> SystemCyan
                                            hasPhotoForDay -> NeonGreen
                                            else -> SystemSurfaceBorder
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        uploadDayTarget = dayNum
                                        val idx = activePlaylist.indexOfFirst { it.dayNumber == dayNum }
                                        if (idx >= 0) {
                                            isPlaying = false
                                            currentFrameIndex = idx
                                        }
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "D$dayNum",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = if (hasPhotoForDay) NeonGreen else if (isSelectedDay) SystemCyan else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Icon(
                                        imageVector = if (hasPhotoForDay) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        tint = if (hasPhotoForDay) NeonGreen else TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dual Capture Buttons for Selected Day: Live Camera or Gallery
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                showLiveCameraForDay = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SNAP DAY $uploadDayTarget LIVE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SystemSurfaceElevated, contentColor = SystemCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SystemCyan),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GALLERY DAY $uploadDayTarget", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ===================================================================
                    // 4. GEMINI LIVE AI TRANSFORMATION COMMENTARY & VOICE DIRECTOR
                    // ===================================================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MonarchPurple.copy(alpha = 0.12f))
                            .border(1.dp, MonarchPurple, RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RankGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "GEMINI LIVE TRANSFORMATION DIRECTOR",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = RankGold
                                    )
                                }
                                if (aiCommentary != null) {
                                    IconButton(
                                        onClick = {
                                            ttsEngine?.speak(aiCommentary, TextToSpeech.QUEUE_FLUSH, null, "memory_lane_tts")
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak AI Analysis", tint = SystemCyan, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (isGeneratingAiCommentary) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(color = SystemCyan, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gemini 2.5 Flash analyzing 30-day time-lapse progression...", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = SystemCyan)
                                }
                            } else if (aiCommentary != null) {
                                Text(
                                    text = aiCommentary!!,
                                    fontSize = 10.sp,
                                    color = TextPrimary,
                                    lineHeight = 14.sp
                                )
                            } else {
                                Text(
                                    text = "Generate a personalized Gemini Live voice & text analysis of your weekly morphological progression from Day 1 to Day 30.",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    lineHeight = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    SystemFeedbackHelper.triggerHaptic(context)
                                    isGeneratingAiCommentary = true
                                    coroutineScope.launch {
                                        val commentary = GeminiVisionService.generateAiMemoryLaneCommentary(
                                            character = character,
                                            loggedPhotosCount = dailyPhotos.size,
                                            completedDaysCount = completedDaysCount
                                        )
                                        aiCommentary = commentary
                                        isGeneratingAiCommentary = false
                                        ttsEngine?.speak(commentary, TextToSpeech.QUEUE_FLUSH, null, "memory_lane_auto_tts")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MonarchPurple, contentColor = Color.White),
                                shape = CutCornerShape(4.dp),
                                modifier = Modifier.fillMaxWidth().height(34.dp).testTag("gemini_memory_lane_commentary_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ANALYZE TIME-LAPSE WITH GEMINI LIVE VOICE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Biometric Improvement Metrics Grid
                    Text("MULTIMODAL ANATOMICAL CHANGES (DAY 1 ➔ 30)", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = SystemCyan)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = "BODY FAT LOST",
                            value = memoryReport.bodyFatLost,
                            icon = Icons.AutoMirrored.Filled.TrendingDown,
                            color = NeonGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "MUSCLE GAINED",
                            value = memoryReport.muscleGained,
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            color = RankGold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = "WAIST REDUCTION",
                            value = memoryReport.waistReductionCm,
                            icon = Icons.AutoMirrored.Filled.TrendingDown,
                            color = SystemCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "V-TAPER RATIO",
                            value = memoryReport.vTaperRatioProgress,
                            icon = Icons.Default.AutoAwesome,
                            color = MonarchPurple,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weekly Memory Reel Selector (Week 1 to 5)
                    Text("WEEKLY TRANSFORMATION CHAPTERS (TAP TO JUMP)", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        memoryReport.weeklyStages.forEachIndexed { index, stage ->
                            val isSel = selectedWeekStage == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CutCornerShape(4.dp))
                                    .background(if (isSel) SystemCyan else SystemSurfaceElevated)
                                    .clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        selectedWeekStage = index
                                        val targetDay = when (stage.weekNumber) {
                                            1 -> 1
                                            2 -> 14
                                            3 -> 21
                                            4 -> 28
                                            else -> 30
                                        }
                                        uploadDayTarget = targetDay
                                        val frameIdx = activePlaylist.indexOfFirst { it.dayNumber >= targetDay }
                                        if (frameIdx >= 0) {
                                            isPlaying = false
                                            currentFrameIndex = frameIdx
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (stage.weekNumber == 5) "FINALE" else "WK ${stage.weekNumber}",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSel) Color.Black else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selected Stage Card
                    val stage = memoryReport.weeklyStages[selectedWeekStage.coerceIn(0, memoryReport.weeklyStages.size - 1)]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(SystemSurface)
                            .border(1.dp, SystemSurfaceBorder, RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stage.title, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = SystemCyan)
                                Text(stage.dayRange, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(stage.stageTitle, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RankGold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(stage.biometricNotes, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sovereign Awakening Claim Section (Only if 30 days complete)
                    if (isDay30Reached) {
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                userPreferences.addEarnedBadge(character.sovereignBadge.title)
                                userPreferences.addXp(2000)
                                userPreferences.resetContractForNewMonth()
                                onAwakeningComplete()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RankGold, contentColor = Color.Black),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("claim_sovereign_awakening_memory_button")
                        ) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AWAKEN AS SOVEREIGN & EQUIP BADGE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CutCornerShape(6.dp))
                                .background(SystemSurfaceBorder.copy(alpha = 0.5f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SOVEREIGN BADGE CLAIM LOCKED (${completedDaysCount}/30 DAYS COMPLETED)",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Live Camera Viewfinder Modal to snap a daily progress photo directly into Memory Lane
    if (showLiveCameraForDay) {
        RealTimeCameraBodyScannerModal(
            title = "MEMORY LANE • DAY $uploadDayTarget SNAPSHOT",
            subtitle = "Capture Day $uploadDayTarget progress photo for your 30-day time-lapse",
            isCapturingOrAnalyzing = false,
            onPhotoCapturedBase64 = { _, bitmap ->
                try {
                    val file = File(context.cacheDir, "memory_lane_day_${uploadDayTarget}_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    val savedUri = Uri.fromFile(file).toString()
                    userPreferences.saveDailyPhoto(uploadDayTarget, savedUri)
                    dailyPhotos = userPreferences.getAllDailyPhotos()
                    SystemFeedbackHelper.playSuccessFanfare()
                } catch (ignored: Exception) {}
                showLiveCameraForDay = false
            },
            onGalleryPickRequested = {
                showLiveCameraForDay = false
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onDismiss = { showLiveCameraForDay = false }
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SystemSurfaceElevated)
            .border(1.dp, SystemSurfaceBorder, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = TextMuted, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = color)
        }
    }
}
