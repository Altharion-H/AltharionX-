package com.example.data

enum class CharacterCategory(val displayName: String) {
    MODERN_WEBTOON("New-Gen & Webtoon"),
    ABSOLUTE_GIANTS("Absolute Giants"),
    PEAK_AESTHETICS("Peak Aesthetics & Functional"),
    COMICALLY_MAJESTIC("Comically Majestic & Ripped")
}

data class SovereignBadge(
    val title: String,
    val description: String,
    val auraColorHex: Long = 0xFF00F0FF
)

data class AnimeCharacter(
    val id: String,
    val name: String,
    val animeTitle: String,
    val category: CharacterCategory,
    val physiqueClass: String,
    val targetBodyFat: String,
    val dominantMuscles: List<String>,
    val focusDescription: String,
    val signatureMove: String,
    val dailyCalisthenicsProtocol: String,
    // Dossier fields
    val weeklySplit: String,
    val exerciseStyle: String,
    val caloricStrategy: String, // e.g. "Lean Recomp (-200 kcal)", "Monster Hypertrophy Bulk (+450 kcal)"
    val recommendedCalories: String,
    val proteinPerKg: Double,
    val carbsPerKg: Double = 3.5,
    val fatsPerKg: Double = 0.9,
    val hydrationGoalLiters: Double,
    val extremeFinisherPreview: String,
    val sovereignBadge: SovereignBadge,
    val auraColorHex: Long = 0xFF00F0FF
)
