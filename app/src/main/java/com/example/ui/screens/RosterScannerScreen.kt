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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.AnimeCharacter
import com.example.data.CharacterCategory
import com.example.data.CharacterRepository
import com.example.data.GeminiVisionService
import com.example.data.SystemScanReport
import com.example.data.UserPreferences
import com.example.ui.components.CyberCard
import com.example.ui.components.RealTimeCameraBodyScannerModal
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RosterScannerScreen(
    userPreferences: UserPreferences,
    onContractSigned: (AnimeCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CharacterCategory?>(null) }
    var activeCharId by remember { mutableStateOf(userPreferences.activeCharacterId) }
    var isContractSigned by remember { mutableStateOf(userPreferences.isContractSigned) }

    // Dossier Modal state
    var selectedDossierCharacter by remember { mutableStateOf<AnimeCharacter?>(null) }
    var lockedAlertCharacterName by remember { mutableStateOf<String?>(null) }

    // AI Vision Scanner state
    var isScanning by remember { mutableStateOf(false) }
    var scanReport by remember { mutableStateOf<SystemScanReport?>(null) }
    var showScanReportDialog by remember { mutableStateOf(false) }
    var showCameraScannerModal by remember { mutableStateOf(false) }
    var showTransformationMemoryModal by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isScanning = true
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    val stream = java.io.ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val base64Clean = android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
                    val report = GeminiVisionService.scanPhysiqueImageBase64(base64Clean)
                    scanReport = report
                    showScanReportDialog = true
                    if (report.isValidPhysique) {
                        SystemFeedbackHelper.playSuccessFanfare()
                    } else {
                        SystemFeedbackHelper.playBeepTone()
                    }
                } catch (e: Exception) {
                    // ignore
                } finally {
                    isScanning = false
                }
            }
        }
    }

    val filteredCharacters = remember(searchQuery, selectedCategory) {
        CharacterRepository.characters.filter { char ->
            val matchesCategory = selectedCategory == null || char.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                char.name.contains(searchQuery, ignoreCase = true) ||
                char.animeTitle.contains(searchQuery, ignoreCase = true) ||
                char.physiqueClass.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
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
                title = "MONARCH CONTRACT ROSTER",
                subtitle = if (isContractSigned && activeCharId.isNotBlank())
                    "CONTRACT BOUND TO: ${CharacterRepository.getCharacterById(activeCharId).name.uppercase()}"
                else "STATUS: UNCONTRACTED E-RANK HUNTER (NO BLUEPRINT ACTIVE)"
            )
        }

        // Uncontracted Hunter Status Banner (Zero-Progress Initial State)
        if (!isContractSigned || activeCharId.isBlank()) {
            item {
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    borderColor = SystemCyan,
                    backgroundColor = SystemSurfaceElevated
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ZERO-PROGRESS INITIAL STATE",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = SystemCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You are currently an Uncontracted Hunter (0 / 30 Days Cleared, 0 XP). Browse the 30 characters below, tap 'VIEW BLUEPRINT DOSSIER', and sign your 30-Day Blood Oath Contract to unlock Day 1.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        } else {
            // Contract Active Lock Banner
            val boundChar = CharacterRepository.getCharacterById(activeCharId)
            item {
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    borderColor = MonarchPurple,
                    backgroundColor = MonarchPurple.copy(alpha = 0.12f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MonarchPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "30-DAY IRREVOCABLE CONTRACT ACTIVE",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Permanently bound to ${boundChar.name} (${boundChar.physiqueClass}). All other 29 blueprints are padlocked until Day 30 Judgment.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // AI Vision Scanner Hero Card
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                backgroundColor = SystemSurface,
                borderColor = SystemCyan
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "REAL-TIME AI BODY SCANNER",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = SystemCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Live HUD viewfinder with Gemini 2.5 Flash skeletal & V-taper classification",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Live Camera Scanner Button
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                showCameraScannerModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(40.dp)
                                .testTag("open_live_camera_scanner_button")
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LIVE SCANNER", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                            }
                        }

                        // Upload from Gallery Button
                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SystemSurfaceElevated, contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SystemSurfaceBorder),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("upload_gallery_scanner_button")
                        ) {
                            Text("GALLERY", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Google Photos-Style Transformation Memory Hero Card
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                backgroundColor = SystemSurfaceElevated,
                borderColor = MonarchPurple
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MonarchPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TRANSFORMATION MEMORY", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Weekly progressive photo reel • Before/After milestone memory", fontSize = 10.sp, color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            showTransformationMemoryModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MonarchPurple, contentColor = Color.White),
                        shape = CutCornerShape(4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("VIEW REEL", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("character_search_input"),
                placeholder = {
                    Text(
                        text = "Search by character, anime, or class...",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SystemCyan)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SystemCyan,
                    unfocusedBorderColor = SystemSurfaceBorder,
                    focusedContainerColor = SystemSurface,
                    unfocusedContainerColor = SystemSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }

        // 4 Official Filter Tabs:
        // [New-Gen & Webtoon], [Absolute Giants], [Peak Aesthetics & Functional], [Comically Majestic & Ripped]
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip(
                        title = "ALL (30)",
                        isSelected = selectedCategory == null,
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            selectedCategory = null
                        }
                    )
                }
                items(CharacterCategory.values()) { category ->
                    CategoryChip(
                        title = category.displayName,
                        isSelected = selectedCategory == category,
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            selectedCategory = if (selectedCategory == category) null else category
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AVAILABLE CHARACTER BLUEPRINTS (${filteredCharacters.size})",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // 30 Characters List / Cards
        items(filteredCharacters, key = { it.id }) { character ->
            val isBound = isContractSigned && activeCharId == character.id
            val isLockedOut = isContractSigned && activeCharId.isNotBlank() && activeCharId != character.id

            CharacterRosterCard(
                character = character,
                isBound = isBound,
                isLockedOut = isLockedOut,
                onCardClick = {
                    SystemFeedbackHelper.triggerHaptic(context)
                    if (isLockedOut) {
                        val boundName = CharacterRepository.getCharacterById(activeCharId).name
                        lockedAlertCharacterName = boundName
                        SystemFeedbackHelper.playBeepTone()
                    } else {
                        selectedDossierCharacter = character
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // SYSTEM LOCK ALERT DIALOG (Crimson)
    if (lockedAlertCharacterName != null) {
        val boundName = lockedAlertCharacterName!!
        Dialog(onDismissRequest = { lockedAlertCharacterName = null }) {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                borderColor = CrimsonPenaltyRed,
                borderWidth = 1.5.dp,
                backgroundColor = SystemAmoledBg
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonPenaltyRed, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[SYSTEM LOCK ACTIVE]",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = CrimsonPenaltyRed
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your blueprint is permanently bound to $boundName for 30 days. You cannot change characters until Day 30 Judgment is complete.",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { lockedAlertCharacterName = null },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPenaltyRed, contentColor = Color.White),
                        shape = CutCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("ACKNOWLEDGE LOCK", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // BLUEPRINT DOSSIER MODAL ("WHAT THIS CHARACTER OFFERS" & BLOOD OATH CONTRACT)
    if (selectedDossierCharacter != null) {
        val character = selectedDossierCharacter!!
        CharacterDossierModal(
            character = character,
            userPreferences = userPreferences,
            onDismiss = { selectedDossierCharacter = null },
            onContractConfirmed = { weight, height, gymMode, photoUri ->
                userPreferences.signContract(character.id, weight, height, gymMode, photoUri)
                isContractSigned = true
                activeCharId = character.id
                selectedDossierCharacter = null
                onContractSigned(character)
            }
        )
    }

    // Real-Time Camera Scanner Modal
    if (showCameraScannerModal) {
        RealTimeCameraBodyScannerModal(
            title = "REAL-TIME BIOMETRIC BODY SCANNER",
            subtitle = "Align clavicles to top guide • waist to lower guide",
            isCapturingOrAnalyzing = isScanning,
            onPhotoCapturedBase64 = { base64Clean, _ ->
                coroutineScope.launch {
                    isScanning = true
                    try {
                        val report = GeminiVisionService.scanPhysiqueImageBase64(base64Clean)
                        scanReport = report
                        showCameraScannerModal = false
                        showScanReportDialog = true
                        if (report.isValidPhysique) {
                            SystemFeedbackHelper.playSuccessFanfare()
                        } else {
                            SystemFeedbackHelper.playBeepTone()
                        }
                    } catch (e: Exception) {
                        // ignore
                    } finally {
                        isScanning = false
                    }
                }
            },
            onGalleryPickRequested = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDismiss = { showCameraScannerModal = false }
        )
    }

    // AI Vision Scan Report Dialog
    if (showScanReportDialog && scanReport != null) {
        val report = scanReport!!
        Dialog(onDismissRequest = { showScanReportDialog = false }) {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                borderColor = if (report.isValidPhysique) SystemCyan else CrimsonPenaltyRed,
                borderWidth = 1.5.dp,
                backgroundColor = SystemAmoledBg
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (report.isValidPhysique) "[SYSTEM SCAN REPORT: SUCCESS]" else "[SYSTEM SCAN REPORT: FAILED]",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = if (report.isValidPhysique) SystemCyan else CrimsonPenaltyRed
                        )
                        IconButton(onClick = { showScanReportDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!report.isValidPhysique) {
                        // Failure banner: NO PERSON / TORSO DETECTED
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CrimsonPenaltyRed.copy(alpha = 0.15f))
                                .border(1.dp, CrimsonPenaltyRed, RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonPenaltyRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "NO CLEAR TORSO / PHYSIQUE DETECTED",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = CrimsonPenaltyRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = report.failureReason ?: "[SCAN FAILED: NO CLEAR TORSO/PHYSIQUE DETECTED. PLEASE ALIGN BODY WITHIN THE CAMERA GRID AND RE-SCAN]",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                showScanReportDialog = false
                                showCameraScannerModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPenaltyRed, contentColor = Color.White),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RE-OPEN LIVE CAMERA SCANNER", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    } else {
                        // Valid Physique Classification
                        Text(
                            text = "CURRENT ARCHETYPE: ${report.currentClosestAnimeMatch.uppercase()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "RECOMMENDED BLUEPRINT: ${report.recommendedBlueprintMatch.uppercase()}",
                            fontSize = 11.sp,
                            color = MonarchPurple,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CutCornerShape(4.dp))
                                    .background(SystemSurfaceElevated)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("BODY TYPE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                                    Text(report.detectedBodyType, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CutCornerShape(4.dp))
                                    .background(SystemSurfaceElevated)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("TARGET BF", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                                    Text(report.estimatedBodyFatRange, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = SystemCyan)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "RATIO: ${report.shoulderToWaistRatioAnalysis}",
                            fontSize = 10.sp,
                            color = SystemCyan,
                            fontFamily = FontFamily.Monospace
                        )

                        if (report.weakPointMusclesToTarget.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "PRIORITY OVERLOAD MUSCLES:",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                report.weakPointMusclesToTarget.forEach { muscle ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(SystemSurfaceElevated)
                                            .border(1.dp, SystemSurfaceBorder, RoundedCornerShape(3.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(muscle, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = report.anatomicalCritique,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        // Mixed Multi-Character Hybrid Exercises
                        if (report.hybridExercises.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "⚡ HYBRID MULTI-CHARACTER ROUTINE (MIXED PER NECESSITY):",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = RankGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            report.hybridExercises.forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SystemSurfaceElevated)
                                        .border(0.8.dp, SystemSurfaceBorder, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(hex.exerciseName, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = TextPrimary)
                                            Text("From ${hex.characterSource}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = SystemCyan)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${hex.targetMuscle} • ${hex.setsReps}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = RankGold)
                                        Text("Reason: ${hex.reasonWhyNeeded}", fontSize = 9.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val match = CharacterRepository.characters.find {
                                    it.name.contains(report.recommendedBlueprintMatch, ignoreCase = true) ||
                                        it.name.contains(report.currentClosestAnimeMatch, ignoreCase = true)
                                } ?: CharacterRepository.characters.first()
                                showScanReportDialog = false
                                selectedDossierCharacter = match
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                            shape = CutCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text(
                                text = "ACTIVATE ${report.recommendedBlueprintMatch.uppercase()} BLUEPRINT",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Google Photos-Style Transformation Memory Modal
    if (showTransformationMemoryModal) {
        val boundChar = if (activeCharId.isNotBlank()) CharacterRepository.getCharacterById(activeCharId) else CharacterRepository.characters.first()
        TransformationMemoryModal(
            character = boundChar,
            userPreferences = userPreferences,
            onDismiss = { showTransformationMemoryModal = false },
            onAwakeningComplete = {
                showTransformationMemoryModal = false
            }
        )
    }
}

@Composable
private fun CharacterRosterCard(
    character: AnimeCharacter,
    isBound: Boolean,
    isLockedOut: Boolean,
    onCardClick: () -> Unit
) {
    val borderColor = when {
        isBound -> SystemCyan
        isLockedOut -> SystemSurfaceBorder.copy(alpha = 0.4f)
        else -> SystemSurfaceBorder
    }
    val bgColor = when {
        isBound -> SystemSurfaceElevated
        isLockedOut -> SystemSurface.copy(alpha = 0.5f)
        else -> SystemSurface
    }

    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("character_card_${character.id}"),
        borderColor = borderColor,
        borderWidth = if (isBound) 1.5.dp else 1.dp,
        backgroundColor = bgColor
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = character.name.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isLockedOut) TextMuted else TextPrimary
                        )
                        if (isLockedOut) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = CrimsonPenaltyRed, modifier = Modifier.size(14.dp))
                        }
                        if (isBound) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Bound", tint = SystemCyan, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(
                        text = character.animeTitle,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isLockedOut) TextMuted else MonarchPurple
                    )
                }

                // Body Fat Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isLockedOut) SystemSurfaceBorder else CrimsonPenaltyRed.copy(alpha = 0.15f))
                        .border(1.dp, if (isLockedOut) SystemSurfaceBorder else CrimsonPenaltyRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "BF: ${character.targetBodyFat}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = if (isLockedOut) TextMuted else CrimsonPenaltyRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Class Badge
            Text(
                text = "CLASS: ${character.physiqueClass.uppercase()}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isLockedOut) TextMuted else SystemCyan
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = character.focusDescription,
                fontSize = 11.sp,
                color = if (isLockedOut) TextMuted else TextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action / Tap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REWARD: ★ ${character.sovereignBadge.title} ★",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isLockedOut) TextMuted else RankGold
                )
                Text(
                    text = if (isBound) "ACTIVE BOUND [VIEW]" else if (isLockedOut) "[LOCKED]" else "VIEW DOSSIER →",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = if (isBound) SystemCyan else if (isLockedOut) CrimsonPenaltyRed else SystemCyan
                )
            }
        }
    }
}

@Composable
private fun CharacterDossierModal(
    character: AnimeCharacter,
    userPreferences: UserPreferences,
    onDismiss: () -> Unit,
    onContractConfirmed: (Float, Float, Boolean, String) -> Unit
) {
    val context = LocalContext.current
    var weightInput by remember { mutableStateOf(userPreferences.userWeightKg.toString()) }
    var heightInput by remember { mutableStateOf(userPreferences.userHeightCm.toString()) }
    var isGymAccess by remember { mutableStateOf(userPreferences.isGymMode) }
    var day1PhotoUri by remember { mutableStateOf(userPreferences.day1PhotoUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            day1PhotoUri = uri.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
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
                borderColor = MonarchPurple,
                borderWidth = 1.5.dp,
                backgroundColor = SystemAmoledBg
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BLUEPRINT DOSSIER",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SystemCyan,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = character.name.uppercase(),
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Complete Physique Breakdown
                    SectionHeader("1. PHYSIQUE BREAKDOWN")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Aesthetic Class: ${character.physiqueClass}", fontSize = 11.sp, color = SystemCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(text = "Target Body Fat: ${character.targetBodyFat}", fontSize = 11.sp, color = CrimsonPenaltyRed, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(text = "Target Muscle Priorities: ${character.dominantMuscles.joinToString(", ")}", fontSize = 11.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Training Regimen Overview
                    SectionHeader("2. TRAINING REGIMEN OVERVIEW")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Weekly Split: ${character.weeklySplit}", fontSize = 11.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                    Text(text = "Exercise Style: ${character.exerciseStyle}", fontSize = 11.sp, color = TextSecondary)
                    Text(text = "Extreme Finisher Preview: ${character.extremeFinisherPreview}", fontSize = 11.sp, color = RankGold, fontFamily = FontFamily.Monospace)

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Nutritional Protocol Included
                    SectionHeader("3. NUTRITIONAL PROTOCOL")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Strategy: ${character.caloricStrategy}", fontSize = 11.sp, color = SystemCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(text = "Target Protein: ${character.proteinPerKg}g per kg of bodyweight", fontSize = 11.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                    Text(text = "Hydration Goal: ${character.hydrationGoalLiters} Liters / day", fontSize = 11.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Exclusive Day-30 Sovereign Badge Reward
                    SectionHeader("4. DAY-30 SOVEREIGN BADGE REWARD")
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CutCornerShape(6.dp))
                            .background(MonarchPurple.copy(alpha = 0.2f))
                            .border(1.dp, RankGold, CutCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = RankGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "★ ${character.sovereignBadge.title} ★",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = RankGold
                                )
                                Text(
                                    text = character.sovereignBadge.description,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // BLOOD OATH CONTRACT SIGNING FORM
                    SectionHeader("SIGN 30-DAY BLOOD OATH CONTRACT")
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (kg)", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SystemCyan,
                                unfocusedBorderColor = SystemSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = { Text("Height (cm)", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SystemCyan,
                                unfocusedBorderColor = SystemSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Equipment Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CutCornerShape(4.dp))
                                .background(if (isGymAccess) SystemCyan else SystemSurfaceElevated)
                                .clickable { isGymAccess = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("FULL GYM", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isGymAccess) Color.Black else TextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CutCornerShape(4.dp))
                                .background(if (!isGymAccess) SystemCyan else SystemSurfaceElevated)
                                .clickable { isGymAccess = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("HOME / CALISTHENICS", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (!isGymAccess) Color.Black else TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day 1 Starting Physique Photo Slot
                    Button(
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SystemSurfaceElevated, contentColor = SystemCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (day1PhotoUri.isNotBlank()) SystemCyan else SystemSurfaceBorder),
                        shape = CutCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (day1PhotoUri.isNotBlank()) "DAY 1 PHOTO ATTACHED ✓" else "UPLOAD DAY 1 BASELINE PHOTO",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Final Irrevocable Lock Button
                    Button(
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            SystemFeedbackHelper.playSuccessFanfare()
                            val w = weightInput.toFloatOrNull() ?: 75f
                            val h = heightInput.toFloatOrNull() ?: 178f
                            onContractConfirmed(w, h, isGymAccess, day1PhotoUri)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MonarchPurple, contentColor = Color.White),
                        shape = CutCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("lock_blueprint_30_days_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOCK BLUEPRINT FOR 30 DAYS (SIGN CONTRACT)",
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
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        color = TextMuted,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun CategoryChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CutCornerShape(4.dp))
            .background(if (isSelected) SystemCyan else SystemSurfaceElevated)
            .border(1.dp, if (isSelected) SystemCyan else SystemSurfaceBorder, CutCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            color = if (isSelected) Color.Black else TextSecondary
        )
    }
}
