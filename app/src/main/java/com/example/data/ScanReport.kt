package com.example.data

data class HybridExercise(
    val exerciseName: String,
    val characterSource: String, // e.g. "Yujiro Hanma" or "Broly"
    val targetMuscle: String,
    val setsReps: String,
    val reasonWhyNeeded: String
)

data class SystemScanReport(
    val isValidPhysique: Boolean,
    val detectedBodyType: String, // "Ectomorph", "Mesomorph", "Endomorph", "Anime Character Scan"
    val estimatedBodyFatRange: String, // e.g. "12-14%"
    val shoulderToWaistRatioAnalysis: String,
    val weakPointMusclesToTarget: List<String>, // exactly 3 specific muscle groups
    val currentClosestAnimeMatch: String, // one of the 30 characters
    val recommendedBlueprintMatch: String, // recommended character from 30 roster
    val anatomicalCritique: String,
    val failureReason: String? = null,
    val hybridExercises: List<HybridExercise> = emptyList()
) {
    // Backward-compatible accessors
    val characterMatch: String get() = currentClosestAnimeMatch
    val animeOrArchetype: String get() = detectedBodyType
    val shoulderToWaistRatio: String get() = shoulderToWaistRatioAnalysis
    val estimatedBodyFat: String get() = estimatedBodyFatRange
    val anatomicalBreakdown: String get() = anatomicalCritique
}
