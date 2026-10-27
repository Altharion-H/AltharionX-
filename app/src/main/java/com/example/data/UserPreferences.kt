package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class StreakTier(
    val weekNumber: Int,
    val title: String,
    val flameColorHex: Long,
    val rankBadge: String,
    val auraDescription: String
)

data class HunterFriend(
    val id: String,
    val name: String,
    val rank: String,
    val streak: Int,
    val roadworkKm: Float,
    val characterBlueprint: String
)

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("arise_monarch_contract_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_CONTRACT_SIGNED = "is_contract_signed"
        private const val KEY_ACTIVE_CHAR_ID = "active_character_id"
        private const val KEY_CONTRACT_TIMESTAMP = "contract_timestamp"
        private const val KEY_COMPLETED_DAYS = "completed_days"
        private const val KEY_CHECKED_EXERCISES = "checked_exercises"
        private const val KEY_IS_GYM_MODE = "is_gym_mode"
        private const val KEY_TOTAL_XP = "total_xp"
        private const val KEY_STREAK = "streak_days"
        private const val KEY_EQUIPPED_BADGE = "equipped_badge"
        private const val KEY_EARNED_BADGES = "earned_badges"

        // Photos
        private const val KEY_DAY1_PHOTO = "day1_photo_uri"
        private const val KEY_DAY30_PHOTO = "day30_photo_uri"

        // Hell Mode Penalty (Outcome 2 from Day 30)
        private const val KEY_IS_HELL_MODE = "is_hell_mode"

        // User Stats
        private const val KEY_USER_WEIGHT = "user_weight_kg"
        private const val KEY_USER_HEIGHT = "user_height_cm"
        private const val KEY_SHOULDER_CM = "shoulder_cm"
        private const val KEY_CHEST_CM = "chest_cm"
        private const val KEY_WAIST_CM = "waist_cm"
        private const val KEY_ARMS_CM = "arms_cm"

        // Daily Combat & Nutrition
        private const val KEY_FAILURE_SETS = "failure_sets_today"
        private const val KEY_WATER_ML = "water_ml_today"
        private const val KEY_CALORIES_LOGGED = "calories_logged_today"
        private const val KEY_PROTEIN_LOGGED = "protein_logged_today"
        private const val KEY_CARBS_LOGGED = "carbs_logged_today"
        private const val KEY_FATS_LOGGED = "fats_logged_today"

        // Authentication & Profile
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_LOGIN_PROVIDER = "login_provider"
        private const val KEY_HUNTER_ID = "hunter_id_code"

        // Roadwork & Endurance
        private const val KEY_TOTAL_ROADWORK_KM = "total_roadwork_km"
        private const val KEY_TOTAL_SKIPS = "total_skips_count"
        private const val KEY_LAST_ROADWORK_PACE = "last_roadwork_pace"

        // Multi-Theme
        private const val KEY_ACTIVE_THEME = "active_theme_name"

        // Solo Leveling Penalty Lockout
        private const val KEY_IS_PENALTY_LOCK_ACTIVE = "is_penalty_lock_active"

        // Live Camera Repetition Verification
        private const val KEY_CAMERA_VERIFIED_EXERCISES = "camera_verified_exercises"
        private const val KEY_CAMERA_VERIFIED_REPS = "camera_verified_reps"
        private const val KEY_CAMERA_ENFORCEMENT_ENABLED = "camera_enforcement_enabled"
    }

    // STRICT ZERO-PROGRESS INITIAL STATE:
    // Starts as uncontracted with 0 days, 0 XP, 0 streak, no active blueprint.
    var isContractSigned: Boolean
        get() = prefs.getBoolean(KEY_IS_CONTRACT_SIGNED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_CONTRACT_SIGNED, value).apply()

    var activeCharacterId: String
        get() = prefs.getString(KEY_ACTIVE_CHAR_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ACTIVE_CHAR_ID, value).apply()

    var contractTimestamp: Long
        get() = prefs.getLong(KEY_CONTRACT_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_CONTRACT_TIMESTAMP, value).apply()

    var isGymMode: Boolean
        get() = prefs.getBoolean(KEY_IS_GYM_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_GYM_MODE, value).apply()

    var totalXp: Int
        get() = prefs.getInt(KEY_TOTAL_XP, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_XP, value).apply()

    var streakDays: Int
        get() = prefs.getInt(KEY_STREAK, 0)
        set(value) = prefs.edit().putInt(KEY_STREAK, value).apply()

    var equippedBadge: String
        get() = prefs.getString(KEY_EQUIPPED_BADGE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_EQUIPPED_BADGE, value).apply()

    var day1PhotoUri: String
        get() = prefs.getString(KEY_DAY1_PHOTO, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DAY1_PHOTO, value).apply()

    var day30PhotoUri: String
        get() = prefs.getString(KEY_DAY30_PHOTO, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DAY30_PHOTO, value).apply()

    var isHellMode: Boolean
        get() = prefs.getBoolean(KEY_IS_HELL_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_HELL_MODE, value).apply()

    var userWeightKg: Float
        get() = prefs.getFloat(KEY_USER_WEIGHT, 75.0f)
        set(value) = prefs.edit().putFloat(KEY_USER_WEIGHT, value).apply()

    var userHeightCm: Float
        get() = prefs.getFloat(KEY_USER_HEIGHT, 178.0f)
        set(value) = prefs.edit().putFloat(KEY_USER_HEIGHT, value).apply()

    var shoulderCm: Float
        get() = prefs.getFloat(KEY_SHOULDER_CM, 120.0f)
        set(value) = prefs.edit().putFloat(KEY_SHOULDER_CM, value).apply()

    var chestCm: Float
        get() = prefs.getFloat(KEY_CHEST_CM, 102.0f)
        set(value) = prefs.edit().putFloat(KEY_CHEST_CM, value).apply()

    var waistCm: Float
        get() = prefs.getFloat(KEY_WAIST_CM, 78.0f)
        set(value) = prefs.edit().putFloat(KEY_WAIST_CM, value).apply()

    var armsCm: Float
        get() = prefs.getFloat(KEY_ARMS_CM, 38.0f)
        set(value) = prefs.edit().putFloat(KEY_ARMS_CM, value).apply()

    var failureSetsToday: Int
        get() = prefs.getInt(KEY_FAILURE_SETS, 0)
        set(value) = prefs.edit().putInt(KEY_FAILURE_SETS, value).apply()

    var waterMlToday: Int
        get() = prefs.getInt(KEY_WATER_ML, 0)
        set(value) = prefs.edit().putInt(KEY_WATER_ML, value).apply()

    var caloriesLoggedToday: Int
        get() = prefs.getInt(KEY_CALORIES_LOGGED, 0)
        set(value) = prefs.edit().putInt(KEY_CALORIES_LOGGED, value).apply()

    var proteinLoggedToday: Int
        get() = prefs.getInt(KEY_PROTEIN_LOGGED, 0)
        set(value) = prefs.edit().putInt(KEY_PROTEIN_LOGGED, value).apply()

    var carbsLoggedToday: Int
        get() = prefs.getInt(KEY_CARBS_LOGGED, 0)
        set(value) = prefs.edit().putInt(KEY_CARBS_LOGGED, value).apply()

    var fatsLoggedToday: Int
        get() = prefs.getInt(KEY_FATS_LOGGED, 0)
        set(value) = prefs.edit().putInt(KEY_FATS_LOGGED, value).apply()

    // Authentication Properties
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Hunter Sung") ?: "Hunter Sung"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "hunter.shadow@monarch.system") ?: "hunter.shadow@monarch.system"
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var loginProvider: String
        get() = prefs.getString(KEY_LOGIN_PROVIDER, "offline") ?: "offline"
        set(value) = prefs.edit().putString(KEY_LOGIN_PROVIDER, value).apply()

    var hunterIdCode: String
        get() = prefs.getString(KEY_HUNTER_ID, "HUNTER#7701-JINWOO") ?: "HUNTER#7701-JINWOO"
        set(value) = prefs.edit().putString(KEY_HUNTER_ID, value).apply()

    // Roadwork Metrics
    var totalRoadworkKm: Float
        get() = prefs.getFloat(KEY_TOTAL_ROADWORK_KM, 0.0f)
        set(value) = prefs.edit().putFloat(KEY_TOTAL_ROADWORK_KM, value).apply()

    var totalSkips: Int
        get() = prefs.getInt(KEY_TOTAL_SKIPS, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_SKIPS, value).apply()

    fun addRoadworkKm(km: Float) {
        totalRoadworkKm += km
    }

    fun addSkips(skips: Int) {
        totalSkips += skips
    }

    // Active Theme
    var activeThemeName: String
        get() = prefs.getString(KEY_ACTIVE_THEME, "MONARCH_AMOLED") ?: "MONARCH_AMOLED"
        set(value) = prefs.edit().putString(KEY_ACTIVE_THEME, value).apply()

    // Solo Leveling Penalty Lockout
    var isPenaltyLockActive: Boolean
        get() = prefs.getBoolean(KEY_IS_PENALTY_LOCK_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_PENALTY_LOCK_ACTIVE, value).apply()

    // 14-Day Streak Co-Op Network Unlock
    val isCoOpUnlocked: Boolean
        get() = streakDays >= 14

    // Daily Photo Storage (Days 1, 7, 14, 21, 28, 30, etc.)
    fun saveDailyPhoto(day: Int, uriString: String) {
        prefs.edit().putString("daily_photo_$day", uriString).apply()
        if (day == 1) {
            day1PhotoUri = uriString
        } else if (day == 30) {
            day30PhotoUri = uriString
        }
    }

    fun getDailyPhoto(day: Int): String? {
        return prefs.getString("daily_photo_$day", null)?.takeIf { it.isNotBlank() }
    }

    fun getAllDailyPhotos(): Map<Int, String> {
        val result = mutableMapOf<Int, String>()
        for (day in 1..30) {
            val uri = getDailyPhoto(day)
            if (uri != null) {
                result[day] = uri
            }
        }
        return result
    }

    // Duolingo-style Streak Evolution
    fun getStreakEvolutionTier(): StreakTier {
        return when {
            streakDays >= 28 -> StreakTier(
                weekNumber = 5,
                title = "ASTRAL SOVEREIGN FLAME",
                flameColorHex = 0xFF00F0FF,
                rankBadge = "S-RANK SOVEREIGN",
                auraDescription = "Diamond Prismatic Aura • Complete Transformation Achieved"
            )
            streakDays >= 21 -> StreakTier(
                weekNumber = 4,
                title = "SHADOW MONARCH CORE",
                flameColorHex = 0xFF7B2CBF,
                rankBadge = "A-RANK MONARCH",
                auraDescription = "Royal Purple Mana Wave • Indomitable Willpower"
            )
            streakDays >= 14 -> StreakTier(
                weekNumber = 3,
                title = "INFERNAL SOLAR BLAZE",
                flameColorHex = 0xFFFFB800,
                rankBadge = "B-RANK INFERNAL",
                auraDescription = "Golden Radiant Solar Flame • Co-Op Hunter Network Unlocked!"
            )
            streakDays >= 7 -> StreakTier(
                weekNumber = 2,
                title = "ELECTRIC MANA SPARK",
                flameColorHex = 0xFF00B4D8,
                rankBadge = "D-RANK SURGING",
                auraDescription = "Azure Lightning Current • Weekly Discipline Established"
            )
            streakDays >= 1 -> StreakTier(
                weekNumber = 1,
                title = "SPARK OF AWAKENING",
                flameColorHex = 0xFF00FF88,
                rankBadge = "E-RANK IGNITED",
                auraDescription = "Verdant Awakening Glow • First Steps onto the Monarch Path"
            )
            else -> StreakTier(
                weekNumber = 0,
                title = "DORMANT FLAME",
                flameColorHex = 0xFF64748B,
                rankBadge = "UNAWAKENED",
                auraDescription = "Zero Flame • Sign a Blood Contract & Complete Day 1 to Ignite"
            )
        }
    }

    fun loginWithGoogle(name: String, email: String) {
        isLoggedIn = true
        userName = name
        userEmail = email
        loginProvider = "google"
        hunterIdCode = "GOOGLE#${Math.abs(email.hashCode()) % 10000}"
    }

    fun loginWithHunterId(name: String, hunterId: String) {
        isLoggedIn = true
        userName = name
        userEmail = "$hunterId@hunter-association.org"
        loginProvider = "hunter_id"
        hunterIdCode = hunterId
    }

    fun logout() {
        isLoggedIn = false
        loginProvider = "offline"
    }

    fun getCompletedDays(): Set<Int> {
        val stringSet = prefs.getStringSet(KEY_COMPLETED_DAYS, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun setDayCompleted(day: Int, completed: Boolean) {
        val current = getCompletedDays().toMutableSet()
        if (completed) {
            current.add(day)
            streakDays = maxOf(streakDays, current.size)
        } else {
            current.remove(day)
        }
        prefs.edit().putStringSet(KEY_COMPLETED_DAYS, current.map { it.toString() }.toSet()).apply()
    }

    fun getCheckedExercises(day: Int): Set<String> {
        return prefs.getStringSet("${KEY_CHECKED_EXERCISES}_$day", emptySet()) ?: emptySet()
    }

    fun toggleExerciseChecked(day: Int, exerciseId: String): Boolean {
        val current = getCheckedExercises(day).toMutableSet()
        val isNowChecked = if (current.contains(exerciseId)) {
            current.remove(exerciseId)
            false
        } else {
            current.add(exerciseId)
            true
        }
        prefs.edit().putStringSet("${KEY_CHECKED_EXERCISES}_$day", current).apply()
        return isNowChecked
    }

    var isCameraEnforcementEnabled: Boolean
        get() = prefs.getBoolean(KEY_CAMERA_ENFORCEMENT_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_CAMERA_ENFORCEMENT_ENABLED, value).apply()

    fun getCameraVerifiedExercises(day: Int): Set<String> {
        return prefs.getStringSet("${KEY_CAMERA_VERIFIED_EXERCISES}_$day", emptySet()) ?: emptySet()
    }

    fun isExerciseCameraVerified(day: Int, exerciseId: String): Boolean {
        return getCameraVerifiedExercises(day).contains(exerciseId)
    }

    fun markExerciseCameraVerified(day: Int, exerciseId: String, completedReps: Int) {
        val current = getCameraVerifiedExercises(day).toMutableSet()
        current.add(exerciseId)
        prefs.edit()
            .putStringSet("${KEY_CAMERA_VERIFIED_EXERCISES}_$day", current)
            .putInt("${KEY_CAMERA_VERIFIED_REPS}_${day}_$exerciseId", completedReps)
            .apply()
        // Automatically check the exercise once camera verified
        val checked = getCheckedExercises(day).toMutableSet()
        checked.add(exerciseId)
        prefs.edit().putStringSet("${KEY_CHECKED_EXERCISES}_$day", checked).apply()
    }

    fun getVerifiedReps(day: Int, exerciseId: String): Int {
        return prefs.getInt("${KEY_CAMERA_VERIFIED_REPS}_${day}_$exerciseId", 0)
    }

    fun getEarnedBadges(): Set<String> {
        return prefs.getStringSet(KEY_EARNED_BADGES, emptySet()) ?: emptySet()
    }

    fun addEarnedBadge(badgeTitle: String) {
        val current = getEarnedBadges().toMutableSet()
        current.add(badgeTitle)
        prefs.edit().putStringSet(KEY_EARNED_BADGES, current).apply()
        equippedBadge = badgeTitle
    }

    fun signContract(characterId: String, weight: Float, height: Float, gymMode: Boolean, photoUri: String) {
        prefs.edit()
            .putBoolean(KEY_IS_CONTRACT_SIGNED, true)
            .putString(KEY_ACTIVE_CHAR_ID, characterId)
            .putLong(KEY_CONTRACT_TIMESTAMP, System.currentTimeMillis())
            .putFloat(KEY_USER_WEIGHT, weight)
            .putFloat(KEY_USER_HEIGHT, height)
            .putBoolean(KEY_IS_GYM_MODE, gymMode)
            .putString(KEY_DAY1_PHOTO, photoUri)
            .putBoolean(KEY_IS_HELL_MODE, false)
            .apply()

        if (photoUri.isNotBlank()) {
            saveDailyPhoto(1, photoUri)
        }
    }

    fun resetContractForNewMonth() {
        prefs.edit()
            .putBoolean(KEY_IS_CONTRACT_SIGNED, false)
            .putString(KEY_ACTIVE_CHAR_ID, "")
            .putStringSet(KEY_COMPLETED_DAYS, emptySet())
            .putBoolean(KEY_IS_HELL_MODE, false)
            .apply()
    }

    fun activateHellModeOverload() {
        isHellMode = true
    }

    fun addXp(amount: Int): Int {
        val newXp = totalXp + amount
        totalXp = newXp
        return newXp
    }

    fun getHunterRank(): String {
        return when {
            equippedBadge.isNotBlank() -> "${equippedBadge.uppercase()} / S-RANK"
            totalXp >= 3000 -> "SOVEREIGN S-RANK HUNTER"
            totalXp >= 2000 -> "S-RANK HUNTER"
            totalXp >= 1400 -> "A-RANK HUNTER"
            totalXp >= 900 -> "B-RANK HUNTER"
            totalXp >= 500 -> "C-RANK HUNTER"
            totalXp >= 200 -> "D-RANK HUNTER"
            isContractSigned -> "E-RANK AWAKENED"
            else -> "UNCONTRACTED E-RANK"
        }
    }

    fun getRankProgressFraction(): Float {
        val (base, next) = when {
            totalXp >= 3000 -> 3000 to 5000
            totalXp >= 2000 -> 2000 to 3000
            totalXp >= 1400 -> 1400 to 2000
            totalXp >= 900 -> 900 to 1400
            totalXp >= 500 -> 500 to 900
            totalXp >= 200 -> 200 to 500
            else -> 0 to 200
        }
        val diff = (next - base).toFloat()
        return if (diff > 0) ((totalXp - base).toFloat() / diff).coerceIn(0f, 1f) else 1f
    }

    fun getMockFriends(): List<HunterFriend> {
        return listOf(
            HunterFriend("f1", "Hunter Baek Yoonho", "S-RANK", 29, 84.5f, "Broly"),
            HunterFriend("f2", "Cha Hae-In", "S-RANK", 25, 62.0f, "Toji Fushiguro"),
            HunterFriend("f3", "Choi Jong-In", "A-RANK", 18, 45.2f, "Escanor"),
            HunterFriend("f4", "Woo Jinchul", "A-RANK", 15, 38.0f, "Saitama"),
            HunterFriend("f5", "Go Gunhee", "S-RANK SOVEREIGN", 30, 110.0f, "Yujiro Hanma")
        )
    }
}
