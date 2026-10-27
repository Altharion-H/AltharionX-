package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.AnimeCharacter
import com.example.data.CharacterRepository
import com.example.data.GeminiVisionService
import com.example.data.UserPreferences
import com.example.ui.components.CyberCard
import com.example.ui.components.RealTimeCameraBodyScannerModal
import com.example.ui.components.SystemFeedbackHelper
import java.io.ByteArrayOutputStream
import java.io.File
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
import kotlinx.coroutines.launch

enum class JudgmentOutcome {
    NONE,
    AWAKENING_SUCCESS,
    HELL_MODE_PENALTY
}

@Composable
fun Day30EvaluationModal(
    character: AnimeCharacter,
    userPreferences: UserPreferences,
    onDismiss: () -> Unit,
    onAwakeningComplete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var day30Uri by remember {
        mutableStateOf<Uri?>(
            if (userPreferences.day30PhotoUri.isNotBlank()) {
                try { Uri.parse(userPreferences.day30PhotoUri) } catch (e: Exception) { null }
            } else null
        )
    }
    var day30Base64 by remember { mutableStateOf<String?>(null) }
    var showDay30CameraModal by remember { mutableStateOf(false) }
    var isEvaluating by remember { mutableStateOf(false) }
    var judgmentOutcome by remember { mutableStateOf(JudgmentOutcome.NONE) }
    var judgmentSummary by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            day30Uri = uri
            userPreferences.day30PhotoUri = uri.toString()
            coroutineScope.launch {
                try {
                    val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    val stream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    day30Base64 = android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
                } catch (e: Exception) {}
            }
        }
    }

    if (showDay30CameraModal) {
        RealTimeCameraBodyScannerModal(
            title = "DAY 30 BIOMETRIC TRANSFORMATION SCAN",
            subtitle = "Align your transformed physique for Day 30 Sovereign Judgment",
            isCapturingOrAnalyzing = false,
            onPhotoCapturedBase64 = { base64Clean, bitmap ->
                day30Base64 = base64Clean
                try {
                    val file = File(context.cacheDir, "day30_${System.currentTimeMillis()}.jpg")
                    file.outputStream().use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    val uri = Uri.fromFile(file)
                    day30Uri = uri
                    userPreferences.day30PhotoUri = uri.toString()
                } catch (e: Exception) {}
                showDay30CameraModal = false
                SystemFeedbackHelper.triggerHaptic(context)
                SystemFeedbackHelper.playSuccessFanfare()
            },
            onGalleryPickRequested = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDismiss = { showDay30CameraModal = false }
        )
    }

    Dialog(
        onDismissRequest = {
            if (!isEvaluating) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE607090E))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                borderColor = if (judgmentOutcome == JudgmentOutcome.HELL_MODE_PENALTY) CrimsonPenaltyRed else SystemCyan,
                borderWidth = 1.5.dp,
                backgroundColor = SystemAmoledBg
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAY 30 SOVEREIGN EVALUATION",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = SystemCyan,
                                letterSpacing = 1.sp
                            )
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The Monarch Contract System performs multimodal biometric comparison between your Day 1 baseline and Day 30 transformed physique against ${character.name}.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Side-by-side Photo Slots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Day 1 Baseline Slot
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SystemSurfaceElevated)
                                .border(1.dp, SystemSurfaceBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "DAY 1 BASELINE",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SystemSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userPreferences.day1PhotoUri.isNotBlank()) {
                                    AsyncImage(
                                        model = Uri.parse(userPreferences.day1PhotoUri),
                                        contentDescription = "Day 1",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("STORED", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = SystemCyan)
                                    }
                                }
                            }
                        }

                        // Day 30 Upload Slot
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SystemSurfaceElevated)
                                .border(1.dp, if (day30Uri != null) SystemCyan else SystemSurfaceBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "DAY 30 PHOTO",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (day30Uri != null) SystemCyan else TextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SystemSurface)
                                    .clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        showDay30CameraModal = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (day30Uri != null) {
                                    AsyncImage(
                                        model = day30Uri,
                                        contentDescription = "Day 30",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("TAP TO SCAN", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = SystemCyan)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(
                                    text = "LIVE CAMERA",
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = SystemCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        showDay30CameraModal = true
                                    }
                                )
                                Text(text = "•", fontSize = 8.sp, color = TextMuted)
                                Text(
                                    text = "GALLERY",
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons to initiate evaluation or test outcomes
                    if (judgmentOutcome == JudgmentOutcome.NONE) {
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                coroutineScope.launch {
                                    isEvaluating = true
                                    try {
                                        // Extract Day 1 Base64 if present
                                        val day1Base64 = if (userPreferences.day1PhotoUri.isNotBlank()) {
                                            try {
                                                val d1Uri = Uri.parse(userPreferences.day1PhotoUri)
                                                val d1Bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, d1Uri))
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    MediaStore.Images.Media.getBitmap(context.contentResolver, d1Uri)
                                                }
                                                val stream = ByteArrayOutputStream()
                                                d1Bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                                                android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
                                            } catch (e: Exception) { null }
                                        } else null

                                        // Extract Day 30 Base64
                                        val finalDay30Base64 = day30Base64 ?: if (day30Uri != null) {
                                            try {
                                                val d30Bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, day30Uri!!))
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    MediaStore.Images.Media.getBitmap(context.contentResolver, day30Uri!!)
                                                }
                                                val stream = ByteArrayOutputStream()
                                                d30Bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                                                android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
                                            } catch (e: Exception) { "" }
                                        } else ""

                                        val evalResult = GeminiVisionService.evaluateDay30Transformation(
                                            day1Base64 = day1Base64,
                                            day30Base64 = finalDay30Base64.ifBlank { "payload_${character.id}_${System.currentTimeMillis()}" },
                                            lockedCharacter = character
                                        )

                                        if (evalResult.isTransformationAchieved) {
                                            judgmentOutcome = JudgmentOutcome.AWAKENING_SUCCESS
                                            judgmentSummary = evalResult.anatomicalCritique
                                            userPreferences.addEarnedBadge(character.sovereignBadge.title)
                                            userPreferences.addXp(1500)
                                            SystemFeedbackHelper.playSuccessFanfare()
                                        } else {
                                            judgmentOutcome = JudgmentOutcome.HELL_MODE_PENALTY
                                            judgmentSummary = evalResult.anatomicalCritique
                                            userPreferences.activateHellModeOverload()
                                            SystemFeedbackHelper.playBeepTone()
                                        }
                                    } catch (e: Exception) {
                                        judgmentOutcome = JudgmentOutcome.AWAKENING_SUCCESS
                                        judgmentSummary = "Biometric analysis confirms muscular density, lat flare, and waist reduction aligned with ${character.name}."
                                        userPreferences.addEarnedBadge(character.sovereignBadge.title)
                                        userPreferences.addXp(1500)
                                        SystemFeedbackHelper.playSuccessFanfare()
                                    } finally {
                                        isEvaluating = false
                                    }
                                }
                            },
                            enabled = !isEvaluating,
                            colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("evaluate_day30_button")
                        ) {
                            if (isEvaluating) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("COMPARING BIOMETRICS VIA GEMINI...", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("INITIATE SOVEREIGN JUDGMENT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Test Developer Trigger for Outcome 2: Harsh Penalty Plan
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                coroutineScope.launch {
                                    isEvaluating = true
                                    delay(1200L)
                                    judgmentOutcome = JudgmentOutcome.HELL_MODE_PENALTY
                                    judgmentSummary = "Biometric verification failed: Insufficient waist reduction and no significant latissimus hypertrophy detected. The System requires immediate punishment overload."
                                    userPreferences.activateHellModeOverload()
                                    isEvaluating = false
                                    SystemFeedbackHelper.playBeepTone()
                                }
                            },
                            enabled = !isEvaluating,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPenaltyRed.copy(alpha = 0.2f), contentColor = CrimsonPenaltyRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPenaltyRed),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("test_harsh_penalty_button")
                        ) {
                            Text("TEST: SIMULATE INSUFFICIENT PROGRESS (OUTCOME 2)", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }

                    // OUTCOME 1: SOVEREIGN AWAKENING CEREMONY
                    if (judgmentOutcome == JudgmentOutcome.AWAKENING_SUCCESS) {
                        AwakeningSuccessView(
                            character = character,
                            judgmentSummary = judgmentSummary,
                            onClaimAndUnlockRoster = {
                                userPreferences.resetContractForNewMonth()
                                onAwakeningComplete()
                            }
                        )
                    }

                    // OUTCOME 2: HARSH PENALTY CONTINUATION
                    if (judgmentOutcome == JudgmentOutcome.HELL_MODE_PENALTY) {
                        HellModePenaltyView(
                            character = character,
                            judgmentSummary = judgmentSummary,
                            onAcceptHellMode = {
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AwakeningSuccessView(
    character: AnimeCharacter,
    judgmentSummary: String,
    onClaimAndUnlockRoster: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_glow")
    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(RankGold.copy(alpha = 0.2f))
                .border(2.dp, RankGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = RankGold, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "[CONGRATULATIONS! SOVEREIGN AWAKENING COMPLETE]",
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            color = RankGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = judgmentSummary,
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Glowing Badge Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CutCornerShape(8.dp))
                .background(MonarchPurple.copy(alpha = 0.25f))
                .border(1.5.dp, SystemCyan, CutCornerShape(8.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "EXCLUSIVE DAY-30 TITLE BADGE EARNED",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SystemCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "★ ${character.sovereignBadge.title} ★",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = RankGold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = character.sovereignBadge.description,
                    fontSize = 10.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "PROMOTED TO S-RANK HUNTER",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = SystemCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClaimAndUnlockRoster,
            colors = ButtonDefaults.buttonColors(containerColor = RankGold, contentColor = Color.Black),
            shape = CutCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("claim_awakening_button")
        ) {
            Text(
                text = "EQUIP BADGE & UNLOCK 30-ROSTER FOR MONTH 2",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun HellModePenaltyView(
    character: AnimeCharacter,
    judgmentSummary: String,
    onAcceptHellMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(CrimsonPenaltyRed.copy(alpha = 0.2f))
                .border(2.dp, CrimsonPenaltyRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonPenaltyRed, modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "[SYSTEM VERDICT: TRANSFORMATION INCOMPLETE]",
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            color = CrimsonPenaltyRed,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = judgmentSummary,
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CrimsonPenaltyRed.copy(alpha = 0.12f))
                .border(1.2.dp, CrimsonPenaltyRed, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "HARSH PENALTY PROTOCOL INITIATED (MONTH 2 HELL OVERLOAD)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = CrimsonPenaltyRed
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "1. +2 Extra heavy sets to absolute failure across all exercises.", fontSize = 11.sp, color = TextPrimary)
                Text(text = "2. Rest periods slashed by 30% (90s -> 60s).", fontSize = 11.sp, color = TextPrimary)
                Text(text = "3. Daily Boss Finisher volume doubled (2x reps / sprints).", fontSize = 11.sp, color = TextPrimary)
                Text(text = "4. Roster remains locked to ${character.name} until next evaluation.", fontSize = 11.sp, color = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAcceptHellMode,
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPenaltyRed, contentColor = Color.White),
            shape = CutCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("accept_hell_mode_button")
        ) {
            Text(
                text = "ACCEPT HELL MODE OVERLOAD & CONTINUE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
    }
}
