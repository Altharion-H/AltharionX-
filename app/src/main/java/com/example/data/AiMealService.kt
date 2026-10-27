package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class MealItem(
    val mealName: String, // e.g. "Breakfast: High-Protein Paneer Besan Chilla & Sattu Shake"
    val timing: String,
    val ingredientsWithGrams: List<String>,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatsG: Int,
    val coachNotes: String,
    val isGeneratedByLiveAi: Boolean = true
)

data class GeneratedDailyMealPlan(
    val characterName: String,
    val country: String,
    val dietMode: String,
    val totalCalories: Int,
    val totalProteinG: Int,
    val totalCarbsG: Int,
    val totalFatsG: Int,
    val meals: List<MealItem>,
    val isLiveAiGenerated: Boolean = true
)

data class FoodLogResult(
    val foodDescription: String,
    val estimatedCalories: Int,
    val estimatedProteinG: Int,
    val estimatedCarbsG: Int,
    val estimatedFatsG: Int,
    val feedback: String,
    val isLiveAiGenerated: Boolean = true
)

data class NutritionChatResponse(
    val replyText: String,
    val suggestedMealsUpdate: List<MealItem>? = null,
    val deltaCalories: Int = 0,
    val deltaProteinG: Int = 0,
    val deltaCarbsG: Int = 0,
    val deltaFatsG: Int = 0,
    val actionLabel: String? = null
)

object AiMealService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") key
            else {
                try {
                    val k2 = BuildConfig.API_KEY
                    if (k2.isNotBlank() && k2 != "MY_GEMINI_API_KEY") k2
                    else System.getenv("API_KEY") ?: System.getenv("GEMINI_API_KEY") ?: ""
                } catch (e: Exception) {
                    System.getenv("API_KEY") ?: System.getenv("GEMINI_API_KEY") ?: ""
                }
            }
        } catch (e: Exception) {
            System.getenv("API_KEY") ?: System.getenv("GEMINI_API_KEY") ?: ""
        }
    }

    /**
     * Generates a 100% authentic, country + diet + ingredient specific meal plan using Gemini 2.5 Flash
     */
    suspend fun generate4MealPlan(
        character: AnimeCharacter,
        targetCalories: Int,
        targetProtein: Int,
        targetCarbs: Int,
        targetFats: Int,
        country: String,
        dietaryMode: String,
        availableIngredients: String
    ): GeneratedDailyMealPlan = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()

        val systemInstruction = """
            You are an Elite Sports Nutritionist AI. Generate a 100% authentic, country-specific daily meal plan tailored to the user's locked anime character physique goal (${character.name}).
            CRITICAL DIETARY RULE: If the user selects 'Pure Vegetarian' or 'India', you are STRICTLY FORBIDDEN from outputting beef, steak, pork, chicken, fish, or eggs. For Indian Vegetarian users, calculate exact high-protein meals using authentic Indian vegetarian sources (Paneer, Soya Chunks, Roasted Chana/Sattu, Moong/Besan Chilla, Rajma, Dal, Dahi/Curd, Milk, Peanuts, Oats, Roti, Rice). Match the exact local dishes of whatever country the user selects.
            Dietary Mode Selected: $dietaryMode
            Country / Region: $country
            User's Kitchen / Mess / Grocery Ingredients: $availableIngredients
            Physique Class: ${character.physiqueClass}
            Target Daily Macros: Calories: $targetCalories kcal, Protein: ${targetProtein}g, Carbs: ${targetCarbs}g, Fats: ${targetFats}g.
            
            Structure a 4-meal daily protocol (Meal 1: Breakfast, Meal 2: Lunch, Meal 3: Pre/Post Workout, Meal 4: Dinner).
            Include exact gram weights for all ingredients.
            Output ONLY valid JSON matching this schema:
            {
              "totalCalories": $targetCalories,
              "totalProteinG": $targetProtein,
              "totalCarbsG": $targetCarbs,
              "totalFatsG": $targetFats,
              "meals": [
                {
                  "mealName": "Exact local dish name with meal timing (e.g. Breakfast: Paneer Bhurji with 3 Multigrain Rotis)",
                  "timing": "08:00 AM",
                  "ingredientsWithGrams": ["150g Low Fat Paneer", "3 Whole Wheat Rotis (100g)", "1 Cup Low Fat Dahi (150g)"],
                  "calories": 580,
                  "proteinG": 42,
                  "carbsG": 55,
                  "fatsG": 18,
                  "coachNotes": "High leucine trigger for muscle protein synthesis aligned with ${character.name}'s hypertrophy requirements."
                }
              ]
            }
        """.trimIndent()

        if (apiKey.isBlank()) {
            return@withContext fallbackAuthenticMealPlan(
                character = character,
                targetCalories = targetCalories,
                targetProtein = targetProtein,
                targetCarbs = targetCarbs,
                targetFats = targetFats,
                country = country,
                dietaryMode = dietaryMode,
                ingredients = availableIngredients
            )
        }

        try {
            val partsArray = JSONArray().put(JSONObject().put("text", systemInstruction))
            val contents = JSONArray().put(JSONObject().put("parts", partsArray))
            val generationConfig = JSONObject()
                .put("responseMimeType", "application/json")
                .put("temperature", 0.3)

            val jsonBody = JSONObject().put("contents", contents).put("generationConfig", generationConfig)
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful || responseString.isBlank()) {
                return@withContext fallbackAuthenticMealPlan(character, targetCalories, targetProtein, targetCarbs, targetFats, country, dietaryMode, availableIngredients)
            }

            val rootJson = JSONObject(responseString)
            val text = rootJson.optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""

            val clean = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(clean)
            val mealsArray = parsed.optJSONArray("meals")
            val mealItems = mutableListOf<MealItem>()

            if (mealsArray != null) {
                for (i in 0 until mealsArray.length()) {
                    val m = mealsArray.getJSONObject(i)
                    val ingArray = m.optJSONArray("ingredientsWithGrams")
                    val ings = mutableListOf<String>()
                    if (ingArray != null) {
                        for (j in 0 until ingArray.length()) {
                            ings.add(ingArray.getString(j))
                        }
                    }
                    mealItems.add(
                        MealItem(
                            mealName = m.optString("mealName", "Meal ${i + 1}"),
                            timing = m.optString("timing", "${8 + i * 3}:00"),
                            ingredientsWithGrams = ings,
                            calories = m.optInt("calories", targetCalories / 4),
                            proteinG = m.optInt("proteinG", targetProtein / 4),
                            carbsG = m.optInt("carbsG", targetCarbs / 4),
                            fatsG = m.optInt("fatsG", targetFats / 4),
                            coachNotes = m.optString("coachNotes", "Optimized macronutrient partitioning for ${character.name}."),
                            isGeneratedByLiveAi = true
                        )
                    )
                }
            }

            GeneratedDailyMealPlan(
                characterName = character.name,
                country = country,
                dietMode = dietaryMode,
                totalCalories = parsed.optInt("totalCalories", targetCalories),
                totalProteinG = parsed.optInt("totalProteinG", targetProtein),
                totalCarbsG = parsed.optInt("totalCarbsG", targetCarbs),
                totalFatsG = parsed.optInt("totalFatsG", targetFats),
                meals = if (mealItems.isNotEmpty()) mealItems else fallbackAuthenticMealPlan(character, targetCalories, targetProtein, targetCarbs, targetFats, country, dietaryMode, availableIngredients).meals,
                isLiveAiGenerated = true
            )
        } catch (e: Exception) {
            fallbackAuthenticMealPlan(character, targetCalories, targetProtein, targetCarbs, targetFats, country, dietaryMode, availableIngredients)
        }
    }

    /**
     * Interactive Chat & Coach powered by Gemini 2.5 Flash
     */
    suspend fun chatWithNutritionCoach(
        userMessage: String,
        character: AnimeCharacter,
        country: String,
        dietaryMode: String,
        currentCalories: Int,
        targetCalories: Int,
        currentProtein: Int,
        targetProtein: Int
    ): NutritionChatResponse = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()

        val prompt = """
            You are the ARISE Elite Nutrition & Fitness Coach AI for the character blueprint: ${character.name}.
            Country / Region: $country
            Dietary Mode: $dietaryMode
            Target Calories: $targetCalories kcal (Current Logged: $currentCalories kcal)
            Target Protein: ${targetProtein}g (Current Logged: ${currentProtein}g)
            
            CRITICAL DIETARY RULE: If diet is 'Pure Vegetarian' or region is 'India', NEVER mention beef, steak, chicken, pork, fish, or eggs. Recommend authentic Indian high-protein vegetarian staples like Sattu, Paneer, Soya Chunks, Besan/Moong Chilla, Curd/Dahi, Dal, Rajma, Kala Chana, Oats, and Peanuts.
            
            User's Request: "$userMessage"
            
            Respond as an elite, motivating Monarch Nutrition AI.
            If the user is asking to calculate/log what they just ate (e.g. "I just ate 3 rotis, 1 bowl dal, 100g paneer"), calculate the exact delta macros and provide them in deltaCalories, deltaProteinG, deltaCarbsG, deltaFatsG, with actionLabel "LOG MACROS TO TODAY".
            If the user is asking to replace a meal or modify the daily plan, provide the replacement meal under suggestedMealsUpdate with actionLabel "APPLY CHANGES TO TODAY'S PLAN".
            
            Return JSON only with this schema:
            {
              "replyText": "Detailed elite coaching advice addressing user question directly",
              "deltaCalories": 0,
              "deltaProteinG": 0,
              "deltaCarbsG": 0,
              "deltaFatsG": 0,
              "actionLabel": "APPLY CHANGES TO TODAY'S MEAL PLAN" or "LOG MACROS TO TODAY" or null,
              "suggestedMeal": {
                "mealName": "Name of replacement meal if applicable",
                "timing": "Lunch / Dinner",
                "ingredientsWithGrams": ["Item 1 with grams", "Item 2 with grams"],
                "calories": 550,
                "proteinG": 40,
                "carbsG": 60,
                "fatsG": 15,
                "coachNotes": "Coach note on why this fits their hostel/mess/diet"
              }
            }
        """.trimIndent()

        if (apiKey.isBlank()) {
            return@withContext fallbackChatResponse(userMessage, country, dietaryMode, character)
        }

        try {
            val partsArray = JSONArray().put(JSONObject().put("text", prompt))
            val contents = JSONArray().put(JSONObject().put("parts", partsArray))
            val generationConfig = JSONObject()
                .put("responseMimeType", "application/json")
                .put("temperature", 0.4)

            val jsonBody = JSONObject().put("contents", contents).put("generationConfig", generationConfig)
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful || responseString.isBlank()) {
                return@withContext fallbackChatResponse(userMessage, country, dietaryMode, character)
            }

            val root = JSONObject(responseString)
            val text = root.optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""

            val clean = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(clean)

            val mealObj = parsed.optJSONObject("suggestedMeal")
            val replacementMeals = if (mealObj != null && mealObj.has("mealName")) {
                val ingList = mutableListOf<String>()
                val ingArr = mealObj.optJSONArray("ingredientsWithGrams")
                if (ingArr != null) {
                    for (i in 0 until ingArr.length()) ingList.add(ingArr.getString(i))
                }
                listOf(
                    MealItem(
                        mealName = mealObj.optString("mealName", "Customized Meal"),
                        timing = mealObj.optString("timing", "Custom Timing"),
                        ingredientsWithGrams = ingList,
                        calories = mealObj.optInt("calories", 500),
                        proteinG = mealObj.optInt("proteinG", 35),
                        carbsG = mealObj.optInt("carbsG", 55),
                        fatsG = mealObj.optInt("fatsG", 14),
                        coachNotes = mealObj.optString("coachNotes", "Tailored by Gemini Nutrition Coach."),
                        isGeneratedByLiveAi = true
                    )
                )
            } else null

            NutritionChatResponse(
                replyText = parsed.optString("replyText", "Nutrition strategy calibrated for your goal."),
                suggestedMealsUpdate = replacementMeals,
                deltaCalories = parsed.optInt("deltaCalories", 0),
                deltaProteinG = parsed.optInt("deltaProteinG", 0),
                deltaCarbsG = parsed.optInt("deltaCarbsG", 0),
                deltaFatsG = parsed.optInt("deltaFatsG", 0),
                actionLabel = parsed.optString("actionLabel").takeIf { it.isNotBlank() && it != "null" }
            )
        } catch (e: Exception) {
            fallbackChatResponse(userMessage, country, dietaryMode, character)
        }
    }

    /**
     * Gemini Vision Multimodal Food & Meal Analyzer
     */
    suspend fun analyzeFoodInput(
        textDescription: String?,
        bitmap: Bitmap?
    ): FoodLogResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()

        val prompt = """
            You are an Elite Sports Nutritionist AI with Gemini Multimodal Vision.
            Analyze this food photo (or text description: '$textDescription').
            
            Identify the exact dish on the plate. If it is Indian food (e.g. Roti, Paneer Bhurji, Dal Tadka, Soya Chunks curry, Chana, Dahi, Biryani, Dosa, Idli, Sattu, etc.) identify each item specifically.
            Estimate accurate portions, total Calories (kcal), Protein (g), Carbs (g), and Fats (g).
            
            Output JSON only:
            {
              "foodDescription": "Accurate dish name and portion breakdown",
              "estimatedCalories": 550,
              "estimatedProteinG": 38,
              "estimatedCarbsG": 55,
              "estimatedFatsG": 16,
              "feedback": "Expert sports nutrition assessment of this meal's muscle recovery value"
            }
        """.trimIndent()

        if (apiKey.isBlank()) {
            return@withContext fallbackFoodResult(textDescription)
        }

        try {
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))

            if (bitmap != null) {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                partsArray.put(
                    JSONObject().put(
                        "inlineData",
                        JSONObject().put("mimeType", "image/jpeg").put("data", base64)
                    )
                )
            }

            val contents = JSONArray().put(JSONObject().put("parts", partsArray))
            val jsonBody = JSONObject()
                .put("contents", contents)
                .put("generationConfig", JSONObject().put("responseMimeType", "application/json"))

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val resp = client.newCall(request).execute()
            val respString = resp.body?.string() ?: ""
            val root = JSONObject(respString)
            val text = root.optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""

            val clean = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(clean)

            FoodLogResult(
                foodDescription = parsed.optString("foodDescription", textDescription ?: "Logged Meal"),
                estimatedCalories = parsed.optInt("estimatedCalories", 540),
                estimatedProteinG = parsed.optInt("estimatedProteinG", 38),
                estimatedCarbsG = parsed.optInt("estimatedCarbsG", 52),
                estimatedFatsG = parsed.optInt("estimatedFatsG", 15),
                feedback = parsed.optString("feedback", "High-quality meal logged into your daily Monarch nutrition tracker."),
                isLiveAiGenerated = true
            )
        } catch (e: Exception) {
            fallbackFoodResult(textDescription)
        }
    }

    private fun fallbackAuthenticMealPlan(
        character: AnimeCharacter,
        targetCalories: Int,
        targetProtein: Int,
        targetCarbs: Int,
        targetFats: Int,
        country: String,
        dietaryMode: String,
        ingredients: String
    ): GeneratedDailyMealPlan {
        val isIndiaOrVeg = country.contains("India", ignoreCase = true) || dietaryMode.contains("Vegetarian", ignoreCase = true) || dietaryMode.contains("Vegan", ignoreCase = true) || dietaryMode.contains("Eggetarian", ignoreCase = true)

        val meals = if (isIndiaOrVeg) {
            listOf(
                MealItem(
                    mealName = "MEAL 1: Sattu High-Protein Shake & Moong Chilla",
                    timing = "08:00 AM",
                    ingredientsWithGrams = listOf(
                        "60g Roasted Chana Sattu in 300ml Buttermilk",
                        "2 Besan/Moong Dal Chilla (100g) with chopped onions & coriander",
                        "15g Roasted Peanuts"
                    ),
                    calories = (targetCalories * 0.25).toInt(),
                    proteinG = (targetProtein * 0.28).toInt(),
                    carbsG = (targetCarbs * 0.25).toInt(),
                    fatsG = (targetFats * 0.24).toInt(),
                    coachNotes = "Rich in plant-based BCAAs, leucine, and complex carbs to ignite glycogen replenishment.",
                    isGeneratedByLiveAi = false
                ),
                MealItem(
                    mealName = "MEAL 2: Paneer Bhurji & Multigrain Rotis with Dal",
                    timing = "01:00 PM",
                    ingredientsWithGrams = listOf(
                        "150g Low Fat Paneer sautéed with cumin & tomatoes",
                        "1 Bowl Thick Yellow Moong Dal (150g)",
                        "3 Multigrain Rotis (105g)",
                        "100g Cucumber & Tomato Salad"
                    ),
                    calories = (targetCalories * 0.32).toInt(),
                    proteinG = (targetProtein * 0.30).toInt(),
                    carbsG = (targetCarbs * 0.32).toInt(),
                    fatsG = (targetFats * 0.30).toInt(),
                    coachNotes = "Complete amino acid profile through synergistic legume and dairy protein synthesis.",
                    isGeneratedByLiveAi = false
                ),
                MealItem(
                    mealName = "MEAL 3: Pre-Workout Soya Chunks & Curd Power Bowl",
                    timing = "05:30 PM",
                    ingredientsWithGrams = listOf(
                        "50g Boiled & Spiced Soya Chunks (26g Pure Protein)",
                        "150g Fresh Curd/Dahi with roasted cumin",
                        "1 Medium Banana for explosive training energy"
                    ),
                    calories = (targetCalories * 0.20).toInt(),
                    proteinG = (targetProtein * 0.24).toInt(),
                    carbsG = (targetCarbs * 0.22).toInt(),
                    fatsG = (targetFats * 0.20).toInt(),
                    coachNotes = "High nitrogen retention meal providing sustained energy throughout extreme daily quests.",
                    isGeneratedByLiveAi = false
                ),
                MealItem(
                    mealName = "MEAL 4: Rajma / Kala Chana Curry with Brown Rice & Sprouted Salad",
                    timing = "08:30 PM",
                    ingredientsWithGrams = listOf(
                        "200g Cooked Rajma or Black Chana Curry",
                        "1 Cup Steamed Rice (150g)",
                        "1 Glass Warm Milk (200ml) with turmeric before bed"
                    ),
                    calories = (targetCalories * 0.23).toInt(),
                    proteinG = (targetProtein * 0.18).toInt(),
                    carbsG = (targetCarbs * 0.21).toInt(),
                    fatsG = (targetFats * 0.26).toInt(),
                    coachNotes = "Slow-digesting casein and plant fiber ensures continuous nocturnal muscle repair.",
                    isGeneratedByLiveAi = false
                )
            )
        } else {
            listOf(
                MealItem(
                    mealName = "MEAL 1: Power Scramble & Oatmeal",
                    timing = "08:00 AM",
                    ingredientsWithGrams = listOf("4 Whole Eggs (200g)", "80g Rolled Oats with 1 Banana", "15g Almonds"),
                    calories = (targetCalories * 0.26).toInt(),
                    proteinG = (targetProtein * 0.27).toInt(),
                    carbsG = (targetCarbs * 0.26).toInt(),
                    fatsG = (targetFats * 0.25).toInt(),
                    coachNotes = "Fast leucine spike and low-GI carbohydrate release.",
                    isGeneratedByLiveAi = false
                ),
                MealItem(
                    mealName = "MEAL 2: Grilled Chicken Breast with Jasmine Rice",
                    timing = "01:00 PM",
                    ingredientsWithGrams = listOf("200g Skinless Chicken Breast", "150g Jasmine Rice", "100g Steamed Broccoli"),
                    calories = (targetCalories * 0.32).toInt(),
                    proteinG = (targetProtein * 0.35).toInt(),
                    carbsG = (targetCarbs * 0.30).toInt(),
                    fatsG = (targetFats * 0.25).toInt(),
                    coachNotes = "Lean protein density prioritizing minimum fat storage.",
                    isGeneratedByLiveAi = false
                ),
                MealItem(
                    mealName = "MEAL 3: Greek Yogurt with Berries & Honey",
                    timing = "05:30 PM",
                    ingredientsWithGrams = listOf("200g Greek Yogurt", "100g Fresh Berries", "20g Peanut Butter"),
                    calories = (targetCalories * 0.20).toInt(),
                    proteinG = (targetProtein * 0.20).toInt(),
                    carbsG = (targetCarbs * 0.22).toInt(),
                    fatsG = (targetFats * 0.25).toInt(),
                    coachNotes = "Antioxidant rich recovery fuel.",
                    isGeneratedByLiveAi = false
                ),
                MealItem(
                    mealName = "MEAL 4: Salmon / White Fish with Sweet Potato",
                    timing = "08:30 PM",
                    ingredientsWithGrams = listOf("180g Baked Fish Fillet", "200g Baked Sweet Potato", "Green Asparagus Salad"),
                    calories = (targetCalories * 0.22).toInt(),
                    proteinG = (targetProtein * 0.18).toInt(),
                    carbsG = (targetCarbs * 0.22).toInt(),
                    fatsG = (targetFats * 0.25).toInt(),
                    coachNotes = "Omega-3 fatty acids for anti-inflammatory joint protection.",
                    isGeneratedByLiveAi = false
                )
            )
        }

        return GeneratedDailyMealPlan(
            characterName = character.name,
            country = country,
            dietMode = dietaryMode,
            totalCalories = targetCalories,
            totalProteinG = targetProtein,
            totalCarbsG = targetCarbs,
            totalFatsG = targetFats,
            meals = meals,
            isLiveAiGenerated = false
        )
    }

    private fun fallbackChatResponse(
        userMessage: String,
        country: String,
        dietaryMode: String,
        character: AnimeCharacter
    ): NutritionChatResponse {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("roti") || lower.contains("paneer") || lower.contains("dal") -> {
                NutritionChatResponse(
                    replyText = "3 Whole Wheat Rotis (~210 kcal, 7g P) + 1 bowl Moong Dal (~140 kcal, 9g P) + 100g Paneer (~265 kcal, 18g P) total: ~615 kcal, 34g Protein, 62g Carbs, 24g Fats. This provides a well-balanced Indian meal with sustained leucine release.",
                    deltaCalories = 615,
                    deltaProteinG = 34,
                    deltaCarbsG = 62,
                    deltaFatsG = 24,
                    actionLabel = "LOG MACROS TO TODAY"
                )
            }
            lower.contains("mess") || lower.contains("hostel") || lower.contains("lunch") -> {
                val replacement = MealItem(
                    mealName = "HOSTEL MESS SPECIAL: High-Protein Dal Chawal & Curd",
                    timing = "12:30 PM",
                    ingredientsWithGrams = listOf(
                        "2 Big Bowls of Thick Hostel Dal (250g)",
                        "1 Cup Steamed Rice or 3 Rotis",
                        "200g Fresh Amul / Local Dahi/Curd",
                        "1 Handful Roasted Chana from your room storage"
                    ),
                    calories = 620,
                    proteinG = 38,
                    carbsG = 78,
                    fatsG = 16,
                    coachNotes = "Easy to assemble in any college/school mess without cooking.",
                    isGeneratedByLiveAi = false
                )
                NutritionChatResponse(
                    replyText = "For hostel or mess dwellers: Mix 2 bowls of thick dal with 200g curd/dahi and keep a jar of roasted chana in your room. This easily hits ~38g of clean protein without cooking appliances!",
                    suggestedMealsUpdate = listOf(replacement),
                    actionLabel = "APPLY CHANGES TO TODAY'S MEAL PLAN"
                )
            }
            lower.contains("sattu") || lower.contains("140g") || lower.contains("whey") || lower.contains("vegetarian") -> {
                NutritionChatResponse(
                    replyText = "To hit 140g protein on Indian pure vegetarian without whey: 1) 60g Sattu Drink in morning = 15g P. 2) 50g Soya Chunks in lunch = 26g P. 3) 150g Paneer = 27g P. 4) 400ml Milk + 200g Curd = 20g P. 5) 2 bowls Dal + 4 Rotis = 25g P. 6) 30g Peanuts/Chana = 8g P. Total: ~141g Protein!",
                    deltaCalories = 0,
                    deltaProteinG = 0,
                    actionLabel = null
                )
            }
            else -> {
                NutritionChatResponse(
                    replyText = "As your ARISE Nutrition Coach aligned with ${character.name}, I recommend prioritizing whole-food bioavailable protein sources tailored to your $dietaryMode diet in $country.",
                    actionLabel = null
                )
            }
        }
    }

    private fun fallbackFoodResult(textDescription: String?): FoodLogResult {
        val desc = textDescription ?: "Authentic Local Meal"
        val lower = desc.lowercase()
        return if (lower.contains("paneer") || lower.contains("roti") || lower.contains("dal")) {
            FoodLogResult(
                foodDescription = desc,
                estimatedCalories = 590,
                estimatedProteinG = 32,
                estimatedCarbsG = 65,
                estimatedFatsG = 20,
                feedback = "[INDIAN MEAL DETECTED]: Excellent combination of dairy casein and lentil amino acids for muscle hypertrophy.",
                isLiveAiGenerated = false
            )
        } else {
            FoodLogResult(
                foodDescription = desc,
                estimatedCalories = 520,
                estimatedProteinG = 36,
                estimatedCarbsG = 50,
                estimatedFatsG = 14,
                feedback = "[MEAL LOGGED]: Balanced athletic macronutrient profile for recovery and glycogen replenishment.",
                isLiveAiGenerated = false
            )
        }
    }
}
