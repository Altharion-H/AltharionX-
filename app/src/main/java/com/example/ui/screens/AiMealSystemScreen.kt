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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.AiMealService
import com.example.data.CharacterRepository
import com.example.data.FoodLogResult
import com.example.data.GeneratedDailyMealPlan
import com.example.data.MealItem
import com.example.data.NutritionChatResponse
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AiMealSystemScreen(
    userPreferences: UserPreferences,
    onNavigateToRoster: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val hasContract = userPreferences.isContractSigned && userPreferences.activeCharacterId.isNotBlank()
    val activeCharacter = remember(userPreferences.activeCharacterId) {
        if (hasContract) CharacterRepository.getCharacterById(userPreferences.activeCharacterId)
        else CharacterRepository.characters.first()
    }

    // Macro targets calculated from user weight & character blueprint:
    val targetCalories = remember(activeCharacter, userPreferences.userWeightKg) {
        val baseCal = activeCharacter.recommendedCalories.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 2700
        val weightAdj = ((userPreferences.userWeightKg - 75f) * 25).toInt()
        baseCal + weightAdj
    }
    val targetProteinG = (userPreferences.userWeightKg * activeCharacter.proteinPerKg).toInt()
    val targetCarbsG = (userPreferences.userWeightKg * activeCharacter.carbsPerKg).toInt()
    val targetFatsG = (userPreferences.userWeightKg * activeCharacter.fatsPerKg).toInt()
    val waterGoalMl = (activeCharacter.hydrationGoalLiters * 1000).toInt()

    var caloriesLogged by remember { mutableIntStateOf(userPreferences.caloriesLoggedToday) }
    var proteinLogged by remember { mutableIntStateOf(userPreferences.proteinLoggedToday) }
    var carbsLogged by remember { mutableIntStateOf(userPreferences.carbsLoggedToday) }
    var fatsLogged by remember { mutableIntStateOf(userPreferences.fatsLoggedToday) }
    var waterLogged by remember { mutableIntStateOf(userPreferences.waterMlToday) }

    // Country / Cuisine State
    val countryOptions = remember {
        listOf(
            "India - North/South/East/West",
            "Japan",
            "USA",
            "United Kingdom",
            "Middle East",
            "South Korea",
            "Brazil",
            "Other (Custom Region)"
        )
    }
    var selectedCountry by remember { mutableStateOf("India - North/South/East/West") }
    var customCountryText by remember { mutableStateOf("") }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }

    // Strict Diet Mode Selector
    val dietOptions = remember {
        listOf(
            "Pure Vegetarian (Strict: NO Meat, NO Fish, NO Eggs, NO Beef)",
            "Eggetarian (Veg + Eggs ONLY: NO Meat, NO Fish, NO Beef)",
            "Vegan (100% Plant-Based)",
            "Indian Non-Veg (Chicken, Fish, Eggs, Mutton — STRICTLY NO BEEF/PORK)",
            "Standard Global Omnivore"
        )
    }
    var selectedDietMode by remember {
        mutableStateOf("Pure Vegetarian (Strict: NO Meat, NO Fish, NO Eggs, NO Beef)")
    }

    // "What's in My Kitchen / Hostels / Mess?" Text Input
    var kitchenIngredients by remember {
        mutableStateOf("Paneer, sattu, soya chunks, moong dal, kala chana, dahi, milk, oats, roti, rice, bananas, peanuts")
    }

    // AI Generation Status & Result
    var isGeneratingPlan by remember { mutableStateOf(false) }
    var generatedMealPlan by remember { mutableStateOf<GeneratedDailyMealPlan?>(null) }

    // Interactive Nutrition Chat State
    var chatInput by remember { mutableStateOf("") }
    var isChatting by remember { mutableStateOf(false) }
    var lastChatResponse by remember { mutableStateOf<NutritionChatResponse?>(null) }

    // Food Photo Scanner State
    var foodTextDescription by remember { mutableStateOf("") }
    var isAnalyzingFood by remember { mutableStateOf(false) }
    var lastLoggedFoodResult by remember { mutableStateOf<FoodLogResult?>(null) }

    // Instant Camera Photo Launcher for Meals
    val foodCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            coroutineScope.launch {
                isAnalyzingFood = true
                try {
                    val result = AiMealService.analyzeFoodInput(null, bitmap)
                    lastLoggedFoodResult = result

                    caloriesLogged += result.estimatedCalories
                    proteinLogged += result.estimatedProteinG
                    carbsLogged += result.estimatedCarbsG
                    fatsLogged += result.estimatedFatsG

                    userPreferences.caloriesLoggedToday = caloriesLogged
                    userPreferences.proteinLoggedToday = proteinLogged
                    userPreferences.carbsLoggedToday = carbsLogged
                    userPreferences.fatsLoggedToday = fatsLogged
                    userPreferences.addXp(30)
                    SystemFeedbackHelper.triggerHaptic(context)
                    SystemFeedbackHelper.playSuccessFanfare()
                } catch (e: Exception) {
                    // handled inside service
                } finally {
                    isAnalyzingFood = false
                }
            }
        }
    }

    // Gallery Photo Launcher for Meals
    val foodGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isAnalyzingFood = true
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    val result = AiMealService.analyzeFoodInput(null, bitmap)
                    lastLoggedFoodResult = result

                    caloriesLogged += result.estimatedCalories
                    proteinLogged += result.estimatedProteinG
                    carbsLogged += result.estimatedCarbsG
                    fatsLogged += result.estimatedFatsG

                    userPreferences.caloriesLoggedToday = caloriesLogged
                    userPreferences.proteinLoggedToday = proteinLogged
                    userPreferences.carbsLoggedToday = carbsLogged
                    userPreferences.fatsLoggedToday = fatsLogged
                    userPreferences.addXp(30)
                    SystemFeedbackHelper.triggerHaptic(context)
                    SystemFeedbackHelper.playSuccessFanfare()
                } catch (e: Exception) {
                    // handled inside service
                } finally {
                    isAnalyzingFood = false
                }
            }
        }
    }

    val activeCountry = if (selectedCountry == "Other (Custom Region)" && customCountryText.isNotBlank()) {
        customCountryText
    } else {
        selectedCountry
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
                title = "AI MEAL & NUTRITION SYSTEM",
                subtitle = if (hasContract) "LIVE GEMINI AI 2.5-FLASH • BOUND TO ${activeCharacter.name.uppercase()}" else "AWAITING MONARCH CONTRACT SIGNATURE"
            )
        }

        if (!hasContract) {
            item {
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    borderColor = CrimsonPenaltyRed,
                    backgroundColor = CrimsonPenaltyRed.copy(alpha = 0.08f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "[SYSTEM WARNING: NO ACTIVE CONTRACT]",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = CrimsonPenaltyRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You must browse the 30-Character Roster and sign a 30-Day Contract to calibrate your biometric nutrition targets.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToRoster,
                            colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("GO TO ROSTER & SIGN CONTRACT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Section 1: Auto-Calibrated Macro Dashboard
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                borderColor = SystemCyan,
                borderWidth = 1.dp,
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
                                text = "CALORIC TARGET",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$caloriesLogged / $targetCalories kcal",
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = SystemCyan
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
                                text = activeCharacter.caloricStrategy,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calorie Progress
                    val calFraction = (caloriesLogged.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SystemSurfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(calFraction)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SystemCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sub-Macros (Protein, Carbs, Fats)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroColumn(
                            title = "PROTEIN",
                            logged = proteinLogged,
                            target = targetProteinG,
                            unit = "g",
                            barColor = RankGold,
                            modifier = Modifier.weight(1f)
                        )
                        MacroColumn(
                            title = "CARBS",
                            logged = carbsLogged,
                            target = targetCarbsG,
                            unit = "g",
                            barColor = Color(0xFF00FF88),
                            modifier = Modifier.weight(1f)
                        )
                        MacroColumn(
                            title = "FATS",
                            logged = fatsLogged,
                            target = targetFatsG,
                            unit = "g",
                            barColor = Color(0xFFFF7043),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Hydration Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalDrink, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HYDRATION: $waterLogged / $waterGoalMl ml",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SystemSurface)
                                    .clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        waterLogged += 250
                                        userPreferences.waterMlToday = waterLogged
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("+250ml", fontSize = 10.sp, color = SystemCyan, fontFamily = FontFamily.Monospace)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SystemSurface)
                                    .clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        waterLogged += 500
                                        userPreferences.waterMlToday = waterLogged
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("+500ml", fontSize = 10.sp, color = SystemCyan, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: LIVE GEMINI AI MEAL GENERATOR (COUNTRY + DIET + INGREDIENTS)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = RankGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE GEMINI AI MEAL GENERATOR",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "100% powered in real time by Gemini 2.5 Flash. Calibrated specifically for your country, cultural diet, and exact ingredients available in your kitchen, hostel, or mess.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Country / Regional Cuisine Selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("1. SELECT COUNTRY / REGIONAL CUISINE", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = SystemCyan)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = isCountryDropdownExpanded,
                        onExpandedChange = { isCountryDropdownExpanded = !isCountryDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCountry,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCountryDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SystemCyan,
                                unfocusedBorderColor = SystemSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                        ExposedDropdownMenu(
                            expanded = isCountryDropdownExpanded,
                            onDismissRequest = { isCountryDropdownExpanded = false },
                            modifier = Modifier.background(SystemSurfaceElevated)
                        ) {
                            countryOptions.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text(country, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                                    onClick = {
                                        selectedCountry = country
                                        isCountryDropdownExpanded = false
                                        SystemFeedbackHelper.triggerHaptic(context)
                                    }
                                )
                            }
                        }
                    }

                    if (selectedCountry == "Other (Custom Region)") {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customCountryText,
                            onValueChange = { customCountryText = it },
                            placeholder = { Text("e.g. Mexico, Italy, Thailand, Nigeria", fontSize = 11.sp, color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SystemCyan,
                                unfocusedBorderColor = SystemSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Strict Diet Mode Selector
                    Text("2. STRICT DIETARY RESTRICTION PROTOCOL", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = SystemCyan)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        dietOptions.forEach { diet ->
                            val isSel = selectedDietMode == diet
                            val isPureVeg = diet.startsWith("Pure Vegetarian")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CutCornerShape(4.dp))
                                    .background(if (isSel) SystemCyan.copy(alpha = 0.15f) else SystemSurfaceElevated)
                                    .border(1.dp, if (isSel) SystemCyan else SystemSurfaceBorder, CutCornerShape(4.dp))
                                    .clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        selectedDietMode = diet
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSel) Icons.Default.CheckCircle else Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = if (isSel) SystemCyan else TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = diet,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSel) FontWeight.Black else FontWeight.Normal,
                                        color = if (isSel) SystemCyan else TextSecondary,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. What's in My Kitchen / Hostels / Mess?
                    Text("3. WHAT'S IN MY KITCHEN / HOSTEL / MESS?", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = SystemCyan)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = kitchenIngredients,
                        onValueChange = { kitchenIngredients = it },
                        modifier = Modifier.fillMaxWidth().testTag("kitchen_ingredients_input"),
                        placeholder = { Text("e.g. Paneer, sattu, soya chunks, moong dal, curd, oats, roti, rice", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SystemCyan,
                            unfocusedBorderColor = SystemSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Status Box while generating
                    if (isGeneratingPlan) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CutCornerShape(6.dp))
                                .background(MonarchPurple.copy(alpha = 0.35f))
                                .border(1.5.dp, SystemCyan, CutCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = SystemCyan, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "[GEMINI AI ENGINE ACTIVE: CALCULATING CUSTOM $activeCountry $selectedDietMode MACROS FOR ${activeCharacter.name}...]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            coroutineScope.launch {
                                isGeneratingPlan = true
                                val plan = AiMealService.generate4MealPlan(
                                    character = activeCharacter,
                                    targetCalories = targetCalories,
                                    targetProtein = targetProteinG,
                                    targetCarbs = targetCarbsG,
                                    targetFats = targetFatsG,
                                    country = activeCountry,
                                    dietaryMode = selectedDietMode,
                                    availableIngredients = kitchenIngredients
                                )
                                generatedMealPlan = plan
                                isGeneratingPlan = false
                                SystemFeedbackHelper.playSuccessFanfare()
                            }
                        },
                        enabled = !isGeneratingPlan,
                        colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                        shape = CutCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("ask_gemini_generate_meal_plan_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ASK GEMINI TO GENERATE MEAL PLAN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }

        // Section 2.1: Display Generated 4-Meal Plan with "⚡ Generated Live by Gemini AI" badge
        if (generatedMealPlan != null) {
            val plan = generatedMealPlan!!
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[CALIBRATED 4-MEAL PROTOCOL]",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = SystemCyan
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00FF88).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF00FF88), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡ Generated Live by Gemini AI",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FF88)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(plan.meals) { meal ->
                CyberCard(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    backgroundColor = SystemSurfaceElevated,
                    borderColor = SystemSurfaceBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = meal.mealName.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${meal.calories} kcal • ${meal.proteinG}g P",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = RankGold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Timing: ${meal.timing}",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                            Text(
                                text = "⚡ Live Gemini AI",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SystemCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        meal.ingredientsWithGrams.forEach { ing ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(4.dp).background(SystemCyan, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = ing, fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "COACH CUE: ${meal.coachNotes}", fontSize = 10.sp, color = TextMuted, lineHeight = 13.sp)
                    }
                }
            }
        }

        // Section 3: INTERACTIVE "TALK TO GEMINI NUTRITION AI" LIVE CHAT BOX
        item {
            Spacer(modifier = Modifier.height(14.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = MonarchPurple,
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = MonarchPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TALK TO GEMINI NUTRITION AI",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ask any diet customization question or log consumed meals in natural language. Gemini computes exact macros and can directly apply updates to today's tracker.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Prompt Suggestion Chips
                    Text("QUICK PROMPTS (TAP TO LOAD):", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))

                    val quickPrompts = listOf(
                        "Indian pure veg 140g protein without whey using sattu, paneer & soya",
                        "Replace my lunch with something I can easily eat in hostel/mess",
                        "I just ate 3 rotis, 1 bowl dal, and 100g paneer — log macros"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        quickPrompts.forEach { prompt ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CutCornerShape(4.dp))
                                    .background(SystemSurfaceElevated)
                                    .clickable {
                                        SystemFeedbackHelper.triggerHaptic(context)
                                        chatInput = prompt
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "💬 \"$prompt\"",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chat Input Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Ask Gemini Nutrition Coach...", fontSize = 11.sp, color = TextMuted) },
                            modifier = Modifier.weight(1f).testTag("chat_input_field"),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MonarchPurple,
                                unfocusedBorderColor = SystemSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Button(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    val msg = chatInput
                                    chatInput = ""
                                    SystemFeedbackHelper.triggerHaptic(context)
                                    coroutineScope.launch {
                                        isChatting = true
                                        val resp = AiMealService.chatWithNutritionCoach(
                                            userMessage = msg,
                                            character = activeCharacter,
                                            country = activeCountry,
                                            dietaryMode = selectedDietMode,
                                            currentCalories = caloriesLogged,
                                            targetCalories = targetCalories,
                                            currentProtein = proteinLogged,
                                            targetProtein = targetProteinG
                                        )
                                        lastChatResponse = resp
                                        isChatting = false
                                        SystemFeedbackHelper.playSuccessFanfare()
                                    }
                                }
                            },
                            enabled = !isChatting && chatInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = MonarchPurple, contentColor = Color.White),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier.height(50.dp).testTag("send_chat_button")
                        ) {
                            if (isChatting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Chat Response Card with Action Button
                    if (lastChatResponse != null) {
                        val resp = lastChatResponse!!
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MonarchPurple.copy(alpha = 0.15f))
                                .border(1.dp, MonarchPurple, RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ GEMINI COACH RESPONSE",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = SystemCyan
                                    )
                                    if (resp.deltaCalories > 0) {
                                        Text(
                                            text = "+${resp.deltaCalories} kcal • +${resp.deltaProteinG}g P",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = RankGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = resp.replyText,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    lineHeight = 15.sp
                                )

                                // Action Buttons (Apply Changes or Log Macros)
                                if (resp.deltaCalories > 0 || resp.actionLabel == "LOG MACROS TO TODAY") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            SystemFeedbackHelper.triggerHaptic(context)
                                            caloriesLogged += resp.deltaCalories
                                            proteinLogged += resp.deltaProteinG
                                            carbsLogged += resp.deltaCarbsG
                                            fatsLogged += resp.deltaFatsG

                                            userPreferences.caloriesLoggedToday = caloriesLogged
                                            userPreferences.proteinLoggedToday = proteinLogged
                                            userPreferences.carbsLoggedToday = carbsLogged
                                            userPreferences.fatsLoggedToday = fatsLogged
                                            userPreferences.addXp(35)
                                            lastChatResponse = resp.copy(actionLabel = "LOGGED SUCCESSFULLY ✓")
                                            SystemFeedbackHelper.playSuccessFanfare()
                                        },
                                        enabled = resp.actionLabel != "LOGGED SUCCESSFULLY ✓",
                                        colors = ButtonDefaults.buttonColors(containerColor = RankGold, contentColor = Color.Black),
                                        shape = CutCornerShape(4.dp),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        Text(
                                            text = if (resp.actionLabel == "LOGGED SUCCESSFULLY ✓") "MACROS LOGGED TO TODAY ✓" else "LOG MACROS TO TODAY (+${resp.deltaCalories} kcal, +${resp.deltaProteinG}g P)",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        )
                                    }
                                } else if (resp.suggestedMealsUpdate != null || resp.actionLabel == "APPLY CHANGES TO TODAY'S MEAL PLAN") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            SystemFeedbackHelper.triggerHaptic(context)
                                            val currentMeals = generatedMealPlan?.meals?.toMutableList() ?: mutableListOf()
                                            resp.suggestedMealsUpdate?.forEach { newMeal ->
                                                val existingIdx = currentMeals.indexOfFirst { it.timing.contains(newMeal.timing, ignoreCase = true) || it.mealName.contains("Lunch", ignoreCase = true) }
                                                if (existingIdx >= 0) {
                                                    currentMeals[existingIdx] = newMeal
                                                } else {
                                                    currentMeals.add(newMeal)
                                                }
                                            }
                                            generatedMealPlan = GeneratedDailyMealPlan(
                                                characterName = activeCharacter.name,
                                                country = activeCountry,
                                                dietMode = selectedDietMode,
                                                totalCalories = targetCalories,
                                                totalProteinG = targetProteinG,
                                                totalCarbsG = targetCarbsG,
                                                totalFatsG = targetFatsG,
                                                meals = currentMeals,
                                                isLiveAiGenerated = true
                                            )
                                            lastChatResponse = resp.copy(actionLabel = "PLAN UPDATED SUCCESSFULLY ✓")
                                            SystemFeedbackHelper.playSuccessFanfare()
                                        },
                                        enabled = resp.actionLabel != "PLAN UPDATED SUCCESSFULLY ✓",
                                        colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                                        shape = CutCornerShape(4.dp),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        Text(
                                            text = if (resp.actionLabel == "PLAN UPDATED SUCCESSFULLY ✓") "PLAN UPDATED SUCCESSFULLY ✓" else "APPLY CHANGES TO TODAY'S MEAL PLAN",
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
            }
        }

        // Section 4: LIVE GEMINI VISION FOR FOOD PHOTO SCANNING
        item {
            Spacer(modifier = Modifier.height(14.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = SystemSurfaceBorder,
                backgroundColor = SystemSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = SystemCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE GEMINI VISION FOOD SCANNER",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Snap a photo of your plate or describe what you ate. Gemini Vision identifies authentic Indian or regional dishes, computes exact macros, and updates your daily progress automatically.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = foodTextDescription,
                            onValueChange = { foodTextDescription = it },
                            placeholder = { Text("e.g. 3 rotis, 1 bowl dal, 100g paneer", fontSize = 11.sp, color = TextMuted) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SystemCyan,
                                unfocusedBorderColor = SystemSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Button(
                            onClick = {
                                if (foodTextDescription.isNotBlank()) {
                                    SystemFeedbackHelper.triggerHaptic(context)
                                    coroutineScope.launch {
                                        isAnalyzingFood = true
                                        val res = AiMealService.analyzeFoodInput(foodTextDescription, null)
                                        lastLoggedFoodResult = res
                                        caloriesLogged += res.estimatedCalories
                                        proteinLogged += res.estimatedProteinG
                                        carbsLogged += res.estimatedCarbsG
                                        fatsLogged += res.estimatedFatsG

                                        userPreferences.caloriesLoggedToday = caloriesLogged
                                        userPreferences.proteinLoggedToday = proteinLogged
                                        userPreferences.carbsLoggedToday = carbsLogged
                                        userPreferences.fatsLoggedToday = fatsLogged
                                        userPreferences.addXp(30)
                                        foodTextDescription = ""
                                        isAnalyzingFood = false
                                        SystemFeedbackHelper.playSuccessFanfare()
                                    }
                                }
                            },
                            enabled = !isAnalyzingFood && foodTextDescription.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("LOG", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
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
                                foodCameraLauncher.launch(null)
                            },
                            enabled = !isAnalyzingFood,
                            colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("snap_meal_photo_camera_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SNAP FOOD PHOTO", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                SystemFeedbackHelper.triggerHaptic(context)
                                foodGalleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isAnalyzingFood,
                            colors = ButtonDefaults.buttonColors(containerColor = SystemSurfaceElevated, contentColor = SystemCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SystemCyan),
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("upload_food_gallery_button")
                        ) {
                            Text("GALLERY UPLOAD", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }

                    if (isAnalyzingFood) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = SystemCyan, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI MULTIMODAL VISION IS ANALYZING MEAL MACROS...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = SystemCyan
                            )
                        }
                    }

                    if (lastLoggedFoodResult != null) {
                        val res = lastLoggedFoodResult!!
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(SystemCyan.copy(alpha = 0.12f))
                                .border(1.dp, SystemCyan, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "LOGGED: ${res.foodDescription.uppercase()}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "⚡ Gemini Vision",
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = SystemCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+${res.estimatedCalories} kcal | +${res.estimatedProteinG}g Protein | +${res.estimatedCarbsG}g Carbs | +${res.estimatedFatsG}g Fats",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = SystemCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = res.feedback,
                                    fontSize = 10.sp,
                                    color = TextSecondary
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
private fun MacroColumn(
    title: String,
    logged: Int,
    target: Int,
    unit: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val fraction = if (target > 0) (logged.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SystemSurface)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
        Text("$logged/$target$unit", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SystemSurfaceBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}
