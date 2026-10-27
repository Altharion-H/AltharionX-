package com.example.data

data class WorkoutExercise(
    val id: String,
    val name: String,
    val targetMuscle: String,
    val sets: Int,
    val reps: String,
    val tempo: String,
    val restSeconds: Int,
    val rpeCue: String
)

data class DailyWorkout(
    val dayNumber: Int,
    val weekNumber: Int,
    val phaseTitle: String,
    val focusTitle: String,
    val warmupProtocol: List<String>,
    val exercisesGym: List<WorkoutExercise>,
    val exercisesHome: List<WorkoutExercise>,
    val penaltyBossQuest: String,
    val penaltyDescription: String,
    val xpReward: Int = 200,
    val isHellModeActive: Boolean = false
)

object WorkoutProtocolGenerator {

    fun generate30Days(character: AnimeCharacter, isHellMode: Boolean = false): List<DailyWorkout> {
        val days = mutableListOf<DailyWorkout>()
        for (day in 1..30) {
            val week = when {
                day <= 7 -> 1
                day <= 14 -> 2
                day <= 21 -> 3
                else -> 4
            }
            val phaseTitle = when (week) {
                1 -> if (isHellMode) "HELL OVERLOAD WEEK 1: Brutal Foundation (+2 Sets / -30% Rest)" else "WEEK 1: Foundation & Volume Conditioning"
                2 -> if (isHellMode) "HELL OVERLOAD WEEK 2: Maximum Fiber Destruction" else "WEEK 2: Hypertrophy & Weighted Overload"
                3 -> if (isHellMode) "HELL OVERLOAD WEEK 3: Unbroken Superset Agony" else "WEEK 3: Peak Intensity, Drop Sets & Supersets"
                else -> if (isHellMode) "HELL OVERLOAD WEEK 4: Sovereign Judgment Re-Trial" else "WEEK 4: Limit Breaker / Monarch Trial"
            }

            val dayInCycle = (day - 1) % 7
            val focusTitle = when (dayInCycle) {
                0 -> "${character.name} Push & Clavicle Width"
                1 -> "Demon Back & Latissimus Flare"
                2 -> "Titan Lower Body & Quad Drive"
                3 -> "Monarch Shoulder Cap & Trap Shelf"
                4 -> "Arm Armor & Serratus Core"
                5 -> "All-Out Limit Breaker Calisthenics"
                else -> "Active Recovery, Mobility & Core Shield"
            }

            val warmups = when (dayInCycle) {
                0 -> listOf(
                    "Band Shoulder Dislocates: 3 x 15 reps",
                    "Scapular Push-ups: 3 x 12 reps with 2s hold",
                    "Dead Hang Lat Stretch: 2 x 45s",
                    "Dynamic Chest Openers: 2 x 20 reps"
                )
                1 -> listOf(
                    "Dead Hang & Scapular Pull-ups: 3 x 10 reps",
                    "Cat-Cow Spine Wave: 2 x 15 reps",
                    "Thoracic Rotations: 2 x 12 per side",
                    "Band Face Pulls: 2 x 20 reps"
                )
                2 -> listOf(
                    "Deep Cossack Squats: 3 x 8 per side",
                    "Ankle Dorsiflexion Wall Rocks: 2 x 15 reps",
                    "Glute Bridge March: 2 x 20 reps",
                    "Walking Hip Openers: 2 x 10 per leg"
                )
                3 -> listOf(
                    "Cuban Rotations with Light Dumbbells: 3 x 12 reps",
                    "Arm Circles to Y-T-W: 2 x 15 reps",
                    "Band Pull-Aparts: 3 x 20 reps"
                )
                4 -> listOf(
                    "Wrist Flexion & Extension Rotations: 2 x 20 reps",
                    "Incline Scapular Shrugs: 3 x 15 reps",
                    "Pike Stretch & Hollow Body Hold: 3 x 30s"
                )
                5 -> listOf(
                    "World's Greatest Stretch: 2 x 5 per side",
                    "Jump Rope / High Knees: 3 mins",
                    "Inchworms to Push-up: 2 x 8 reps"
                )
                else -> listOf(
                    "Pigeon Pose Hip Opener: 3 x 60s per side",
                    "Thoracic Foam Rolling: 5 mins",
                    "Deep Belly Breathing Vacuum Holds: 4 x 20s"
                )
            }

            val (baseGym, baseHome) = createExerciseSets(day, week, dayInCycle, character)

            // Apply Hell Mode modifiers if active:
            val gymExercises = if (isHellMode) {
                baseGym.map { it.copy(
                    sets = it.sets + 2,
                    restSeconds = (it.restSeconds * 0.70).toInt().coerceAtLeast(35),
                    rpeCue = "[HELL OVERLOAD]: +2 Sets to absolute failure. ${it.rpeCue}"
                )}
            } else baseGym

            val homeExercises = if (isHellMode) {
                baseHome.map { it.copy(
                    sets = it.sets + 2,
                    restSeconds = (it.restSeconds * 0.70).toInt().coerceAtLeast(30),
                    rpeCue = "[HELL OVERLOAD]: +2 Sets to absolute failure. ${it.rpeCue}"
                )}
            } else baseHome

            val bossQuest = if (isHellMode) {
                when (week) {
                    1 -> "[HELL MODE 2x]: 100 Burpees + 200 Hollow Body Rocks in under 12 minutes."
                    2 -> "[HELL MODE 2x]: 10 x 5 Weighted Chins / Dips at maximum load, followed by 5 min Dead Hang."
                    3 -> "[HELL MODE 2x]: 200 Push-ups, 200 Air Squats, 100 Pull-ups with 0 rest."
                    else -> "[HELL MODE 2x]: 20 x 100m all-out sprints or 600 Hindu Squats before sunset."
                }
            } else {
                when (week) {
                    1 -> "SYSTEM TRIAL: 50 Burpees + 100 Hollow Body Rocks in under 8 minutes."
                    2 -> "WEIGHTED EXTINCTION: 5 x 5 Weighted Chins / Dips at maximum load, followed by 3 min Dead Hang."
                    3 -> "BLOODLINE TEST: 100 Push-ups, 100 Air Squats, 50 Pull-ups with 0 rest between sets."
                    else -> "MONARCH TRIAL (DAY $day): Complete 10 x 100m all-out sprints or 300 Hindu Squats before sunset."
                }
            }

            val penaltyDesc = if (isHellMode) {
                "HELL MODE PENALTY: Failure to clear all sets will reset Month 2 progress back to Day 31."
            } else {
                "Failure to complete this quest triggers the System Penalty Zone: 4-hour desert survival sprint simulation."
            }

            days.add(
                DailyWorkout(
                    dayNumber = day,
                    weekNumber = week,
                    phaseTitle = phaseTitle,
                    focusTitle = focusTitle,
                    warmupProtocol = warmups,
                    exercisesGym = gymExercises,
                    exercisesHome = homeExercises,
                    penaltyBossQuest = bossQuest,
                    penaltyDescription = penaltyDesc,
                    xpReward = if (isHellMode) 350 + (day * 20) else 200 + (day * 15),
                    isHellModeActive = isHellMode
                )
            )
        }
        return days
    }

    private fun createExerciseSets(
        day: Int,
        week: Int,
        dayInCycle: Int,
        character: AnimeCharacter
    ): Pair<List<WorkoutExercise>, List<WorkoutExercise>> {
        val volumeMultiplier = when (week) {
            1 -> 4
            2 -> 5
            3 -> 5
            else -> 6
        }

        return when (dayInCycle) {
            0 -> { // Push
                val gym = listOf(
                    WorkoutExercise("e1", "Incline Barbell Bench Press", "Upper Chest", volumeMultiplier, "6-8", "3-1-1-0", 90, "RPE 9. Explode upwards, control descent strictly."),
                    WorkoutExercise("e2", "Flat Dumbbell Press", "Mid Chest", 4, "8-10", "3-0-1-0", 75, "Full stretch at bottom. Squeeze pectorals at lock."),
                    WorkoutExercise("e3", "Standing Barbell Overhead Press", "Front Delts", volumeMultiplier, "5-7", "2-1-1-0", 90, "Lock out overhead violently, brace glutes and core."),
                    WorkoutExercise("e4", "Heavy Dumbbell Lateral Raises", "Lateral Delts", 5, "12-15", "2-0-1-1", 60, "RPE 10. Pause 1s at horizontal, slow eccentric."),
                    WorkoutExercise("e5", "Weighted Dips", "Lower Chest / Triceps", 4, "8-10", "3-1-1-0", 75, "Final set: drop bodyweight to absolute failure."),
                    WorkoutExercise("e6", "Overhead Cable Triceps Extensions", "Triceps Long Head", 4, "12-15", "3-0-1-0", 60, "Deep tricep stretch behind head, elbows tucked."),
                    WorkoutExercise("e7", "Incline Cable Flyes", "Upper Chest Shelf", 3, "15-20", "2-1-1-1", 45, "Burnout set to pump maximum blood into upper chest.")
                )
                val home = listOf(
                    WorkoutExercise("e1", "Decline Push-ups (Feet Elevated)", "Upper Chest", volumeMultiplier, "15-20", "3-1-1-0", 60, "Elevate feet on chair. Maximum upper chest activation."),
                    WorkoutExercise("e2", "Standard Tempo Push-ups", "Mid Chest", 4, "20-25", "3-0-1-0", 60, "Chest touches floor every rep. Zero momentum."),
                    WorkoutExercise("e3", "Pike Push-ups / Handstand Push-ups", "Front Delts", volumeMultiplier, "8-12", "3-1-1-0", 75, "Elevate hips high, push head through arms."),
                    WorkoutExercise("e4", "Dumbbell / Water-Jug Lateral Raises", "Lateral Delts", 5, "15-20", "2-0-1-1", 45, "Hold at apex for 2 seconds each rep."),
                    WorkoutExercise("e5", "Chair Dips with Weighted Backpack", "Triceps & Chest", 4, "12-15", "3-1-1-0", 60, "Final set drop backpack and rep to failure."),
                    WorkoutExercise("e6", "Diamond Push-ups", "Triceps", 4, "12-15", "3-0-1-0", 60, "Thumbs and index fingers touching. Strict lockout."),
                    WorkoutExercise("e7", "Floor Flye Isometric Slides", "Chest Squeeze", 3, "15-20", "2-1-1-1", 45, "Use towels on slick floor to squeeze pectorals.")
                )
                Pair(gym, home)
            }
            1 -> { // Pull / Demon Back
                val gym = listOf(
                    WorkoutExercise("e1", "Weighted Pull-ups (Wide Grip)", "Latissimus Dorsi", volumeMultiplier, "6-8", "3-1-1-0", 90, "Drive elbows into back pockets. Squeeze shoulder blades."),
                    WorkoutExercise("e2", "Barbell Bent-Over Row", "Mid Back & Rhomboids", 4, "8-10", "3-0-1-0", 75, "Torso at 45 degrees, pull bar to lower sternum."),
                    WorkoutExercise("e3", "Chest-Supported T-Bar Row", "Upper Back Density", 4, "10-12", "2-1-1-1", 60, "Zero lower-back cheating. Pure lat and trap crush."),
                    WorkoutExercise("e4", "Close-Grip Lat Pulldown", "Lower Lats", 4, "10-12", "3-1-1-0", 60, "Lean back 10 degrees, drive handles to upper chest."),
                    WorkoutExercise("e5", "Cable Face Pulls with Rope", "Rear Delts & Rotators", 4, "15-20", "2-1-1-1", 45, "Pull hands past ears, externally rotate thumbs back."),
                    WorkoutExercise("e6", "Incline Dumbbell Biceps Curls", "Biceps Long Head", 4, "10-12", "3-0-1-0", 60, "Full supination at top, 3s descent for peak stretch."),
                    WorkoutExercise("e7", "Barbell Heavy Shrugs", "Trapezius", 4, "12-15", "2-2-1-0", 60, "Hold top for 2 full seconds. Monster trap builder.")
                )
                val home = listOf(
                    WorkoutExercise("e1", "Doorframe / Tree / Bar Pull-ups", "Lats", volumeMultiplier, "8-12", "3-1-1-0", 75, "Strict pull until chin clears bar. No kipping."),
                    WorkoutExercise("e2", "Inverted Table Rows", "Mid Back", 4, "12-15", "3-0-1-1", 60, "Lie under sturdy table, pull chest up to wood edge."),
                    WorkoutExercise("e3", "Single-Arm Heavy Backpack Rows", "Lats & Rhomboids", 4, "12-15", "3-1-1-0", 60, "Pack books into backpack. Pull elbow straight back."),
                    WorkoutExercise("e4", "Towel Door Anchor Row Pulses", "Mid Back", 4, "15-20", "2-1-1-1", 45, "Hook towel over door handle, row with full bodyweight."),
                    WorkoutExercise("e5", "Prone Cobra Holds & Y-Raises", "Rear Delts & Posture", 4, "15-20", "2-2-1-1", 45, "Lie on stomach, pinch scapula and hold thumbs up."),
                    WorkoutExercise("e6", "Backpack Biceps Hammer Curls", "Brachialis & Biceps", 4, "12-15", "3-0-1-0", 60, "Grip straps tightly, curl without swinging elbows."),
                    WorkoutExercise("e7", "Dead Hangs to Failure", "Grip & Lats", 3, "Max Time", "Static", 60, "Hang from pull-up bar or ledge until fingers peel.")
                )
                Pair(gym, home)
            }
            2 -> { // Legs
                val gym = listOf(
                    WorkoutExercise("e1", "Barbell Back Squat", "Quads & Glutes", volumeMultiplier, "5-7", "3-1-1-0", 120, "Break parallel. Explode out of the hole with hip drive."),
                    WorkoutExercise("e2", "Romanian Deadlift (RDL)", "Hamstrings & Glutes", 4, "8-10", "3-1-1-0", 90, "Hinge at hips, push pelvis backward, hamstring fire."),
                    WorkoutExercise("e3", "Leg Press (High & Wide Stance)", "Quads & Adductors", 4, "10-12", "3-0-1-0", 75, "Deep range of motion, avoid locking knees at top."),
                    WorkoutExercise("e4", "Bulgarian Split Squats", "Quad Sweep & Glutes", 3, "10-12 per leg", "3-1-1-0", 60, "Brutal single-leg overload. Keep chest upright."),
                    WorkoutExercise("e5", "Lying Hamstring Leg Curls", "Hamstring Isolation", 4, "12-15", "3-0-1-1", 60, "Final set: 5 partial reps after reaching failure."),
                    WorkoutExercise("e6", "Standing Calf Raises", "Gastrocnemius", 5, "15-20", "2-2-1-1", 45, "Full 2s stretch at bottom, 2s peak contraction."),
                    WorkoutExercise("e7", "Hanging Leg Raises", "Lower Abs & Hip Flexors", 4, "12-15", "2-1-1-0", 45, "Toes touch the bar without swinging.")
                )
                val home = listOf(
                    WorkoutExercise("e1", "Hindu Squats (High Density)", "Quads & Calves", volumeMultiplier, "30-40", "2-0-1-0", 60, "Rhythmic heel elevation with breathing cadence."),
                    WorkoutExercise("e2", "Single-Leg Romanian Deadlift", "Hamstrings", 4, "12-15 per leg", "3-1-1-0", 60, "Reach opposite arm down, feel intense hamstring stretch."),
                    WorkoutExercise("e3", "Jump Squats / Explosive Tuck Jumps", "Fast-Twitch Quads", 4, "15-20", "2-0-1-0", 60, "Maximum vertical height on every single jump."),
                    WorkoutExercise("e4", "Bulgarian Split Squat on Couch", "Quad Tear", 4, "12-15 per leg", "3-1-1-0", 60, "Rear foot on sofa cushion, drop knee to floor."),
                    WorkoutExercise("e5", "Nordic Hamstring Curl Negatives", "Hamstrings", 4, "6-8", "4-0-1-0", 75, "Anchor feet under couch, lower down as slowly as possible."),
                    WorkoutExercise("e6", "Single-Leg Stair Calf Raises", "Calves", 5, "20-25 per leg", "2-1-1-1", 30, "Deep heel drop off step, rise high onto big toe."),
                    WorkoutExercise("e7", "L-Sit Holds / Hanging Knee Tucks", "Core Shield", 4, "30-45s", "Static", 45, "Press hands into floor or chairs, lift legs horizontally.")
                )
                Pair(gym, home)
            }
            3 -> { // Shoulders & Traps
                val gym = listOf(
                    WorkoutExercise("e1", "Seated Dumbbell Overhead Press", "Anterior & Lateral Delts", volumeMultiplier, "6-8", "3-1-1-0", 90, "Touch bells to shoulders, press up in slight arc."),
                    WorkoutExercise("e2", "Cable Lateral Raises Behind Back", "Lateral Delts", 5, "12-15", "2-0-1-1", 45, "Continuous tension on the side delt cap."),
                    WorkoutExercise("e3", "Barbell Upright Rows (Wide Grip)", "Lateral Delts & Traps", 4, "10-12", "2-1-1-0", 60, "Wide grip protects wrists, pull elbows to shoulder level."),
                    WorkoutExercise("e4", "Rear Delt Dumbbell Reverse Flyes", "Posterior Delts", 4, "15-20", "2-0-1-1", 45, "Pinkies up, squeeze shoulder blades outward."),
                    WorkoutExercise("e5", "Dumbbell Shrugs with 3-Sec Pause", "Upper Traps", 4, "10-12", "2-3-1-0", 60, "Do not roll shoulders. Straight elevation to ears."),
                    WorkoutExercise("e6", "Cable Front Raise with Rope", "Front Delts", 3, "12-15", "2-0-1-0", 45, "Raise rope to eye level, pause and burn.")
                )
                val home = listOf(
                    WorkoutExercise("e1", "Wall Handstand Holds / Press Negatives", "Shoulders", volumeMultiplier, "45-60s", "Static", 75, "Kick up against wall, push floor away through palms."),
                    WorkoutExercise("e2", "Backpack Lateral Raise Pyramids", "Side Delts", 5, "15-20", "2-0-1-1", 45, "Fill bag with books, raise arm straight out."),
                    WorkoutExercise("e3", "Pike Push-ups with Feet on Bed", "Deltoids", 4, "10-12", "3-1-1-0", 60, "Strict 90 degree hip bend, lower top of head to ground."),
                    WorkoutExercise("e4", "Prone Reverse Flyes on Floor", "Rear Delts", 4, "20-25", "2-1-1-1", 45, "Chest on carpet, pulse arms outward against air."),
                    WorkoutExercise("e5", "Towel Resisted Neck Extensions", "Neck & Traps", 4, "15-20", "2-1-1-0", 45, "Wrap towel around forehead, push back with neck power."),
                    WorkoutExercise("e6", "Scapular Wall Slides with Towel", "Serratus & Upper Back", 3, "15 reps", "3-1-1-0", 45, "Forearms flat on wall, slide upward maintaining contact.")
                )
                Pair(gym, home)
            }
            4 -> { // Arms & Core
                val gym = listOf(
                    WorkoutExercise("e1", "Close-Grip Barbell Bench Press", "Triceps Lateral & Medial", volumeMultiplier, "6-8", "3-1-1-0", 75, "Hands shoulder-width apart, elbows tucked 45 deg."),
                    WorkoutExercise("e2", "Barbell EZ-Bar Biceps Curls", "Biceps Brachii", 4, "8-10", "3-0-1-0", 60, "Strict form, no hip swinging. Pure bicep tension."),
                    WorkoutExercise("e3", "Skull Crushers (Lying Triceps Extensions)", "Triceps Long Head", 4, "10-12", "3-1-1-0", 60, "Lower bar behind head to maximize stretch."),
                    WorkoutExercise("e4", "Incline Dumbbell Hammer Curls", "Brachialis & Forearms", 4, "10-12", "3-0-1-0", 60, "Builds forearm width and pushes bicep peak higher."),
                    WorkoutExercise("e5", "Cable Triceps Rope Pushdown", "Triceps Horseshoe", 4, "12-15", "2-0-1-1", 45, "Spread rope wide at bottom, lock triceps hard."),
                    WorkoutExercise("e6", "Weighted Roman Chair Sit-ups", "Upper & Mid Abs", 4, "15-20", "2-1-1-0", 45, "Hold 10kg plate on chest, round spine as you curl up."),
                    WorkoutExercise("e7", "Russian Cable Twists", "Rotational Obliques", 3, "15 per side", "2-1-1-0", 45, "Brace core against horizontal cable torque.")
                )
                val home = listOf(
                    WorkoutExercise("e1", "Diamond Push-ups (Elevated Feet)", "Triceps", volumeMultiplier, "12-15", "3-1-1-0", 60, "Fingers close, elbows scraping ribs."),
                    WorkoutExercise("e2", "Backpack Biceps Chin-ups / Chins", "Biceps Peak", 4, "8-12", "3-0-1-0", 60, "Underhand grip on bar or doorframe, curl chin over."),
                    WorkoutExercise("e3", "Bodyweight Triceps Floor Extensions", "Triceps Long Head", 4, "10-12", "3-1-1-0", 60, "Plank position, lower forearms to touch floor, press up."),
                    WorkoutExercise("e4", "Reverse Grip Backpack Curls", "Forearms & Brachialis", 4, "12-15", "3-0-1-0", 45, "Overhand grip to smoke the brachioradialis."),
                    WorkoutExercise("e5", "Bench Triceps Dips (Legs Straight)", "Triceps", 4, "20-25", "2-0-1-0", 45, "High velocity pump set."),
                    WorkoutExercise("e6", "Dragon Flag Progressions", "Demon Core Shield", 4, "8-10", "4-0-1-0", 60, "Lie on ground holding anchor, raise entire body straight."),
                    WorkoutExercise("e7", "Bicycle Crunches to Failure", "Obliques", 3, "40 reps", "1-0-1-0", 30, "Elbow past opposite knee, maximum rotation.")
                )
                Pair(gym, home)
            }
            5 -> { // All-Out Calisthenics / Power
                val gym = listOf(
                    WorkoutExercise("e1", "Power Cleans / Barbell High Pulls", "Explosive Chain", 5, "5", "1-0-X-0", 90, "Explode hips, pop bar upward with traps."),
                    WorkoutExercise("e2", "Trap Bar Deadlift (Speed Sets)", "Full Body Mass", 5, "5", "2-1-X-0", 90, "Max speed off the floor on every rep."),
                    WorkoutExercise("e3", "Dips + Pull-ups Superset", "Upper Body Mass", 4, "10 + 10", "2-0-1-0", 75, "Go directly from dips to pull-ups without rest."),
                    WorkoutExercise("e4", "Heavy Farmer's Walk", "Traps & Grip Endurance", 4, "50 meters", "Stride", 60, "Carry half your bodyweight in each hand."),
                    WorkoutExercise("e5", "Medicine Ball Rotational Slams", "Explosive Core", 4, "12 per side", "1-0-X-0", 45, "Slam ball into floor with full hip snap."),
                    WorkoutExercise("e6", "Assault Bike / Rowing Sprints", "Vo2 Max Engine", 6, "30s sprint / 30s rest", "All-Out", 45, "Max RPM. Lactic acid tolerance trial.")
                )
                val home = listOf(
                    WorkoutExercise("e1", "Explosive Clapping Push-ups", "Chest Power", 5, "8-10", "1-0-X-0", 60, "Push off floor with enough power to clap hands twice."),
                    WorkoutExercise("e2", "Burpee Broad Jumps", "Full Body Explosive", 5, "10-12", "1-0-X-0", 60, "Chest to floor burpee immediately into max forward jump."),
                    WorkoutExercise("e3", "Pull-ups to Chest / High Pulls", "Lats & Biceps", 4, "8-10", "2-0-1-0", 60, "Pull bar down to sternum line, explosive tempo."),
                    WorkoutExercise("e4", "Bear Crawls Forward & Backward", "Core & Shoulders", 4, "20 meters", "Controlled", 45, "Knees 2 inches off floor, spine flat as a table."),
                    WorkoutExercise("e5", "Sprinter Sit-ups", "Rotational Core", 4, "20 reps", "1-0-1-0", 30, "Explode up, drive opposite knee to chest."),
                    WorkoutExercise("e6", "High-Knees Sprint in Place", "Anaerobic Engine", 5, "45s all out", "Max Rate", 30, "Pump arms, knees above hip line.")
                )
                Pair(gym, home)
            }
            else -> { // Active Recovery / Mobility
                val recoveryExercises = listOf(
                    WorkoutExercise("r1", "Stomach Vacuum Holds", "Transverse Abdominis", 5, "20s hold", "Static", 30, "Exhale all air, pull belly button toward spine."),
                    WorkoutExercise("r2", "Pike Stretch & Hamstring Release", "Posterior Chain", 4, "45s", "Gentle", 30, "Breathe deep into hamstrings and lower back."),
                    WorkoutExercise("r3", "Shoulder Dislocates with Towel", "Joint Mobility", 3, "20 reps", "Slow", 30, "Smooth 360 degree arc around shoulders."),
                    WorkoutExercise("r4", "Deep Cossack Squat Rocks", "Hip Adductors", 3, "10 per side", "Slow", 30, "Sit deep into lateral squat, open inner hips."),
                    WorkoutExercise("r5", "Dead Hang Spinal Decompression", "Spinal Column", 3, "60s", "Relaxed", 45, "Allow vertebrae to stretch and decompress."),
                    WorkoutExercise("r6", "Lizard Pose with Quad Stretch", "Hip Flexors & Quads", 3, "45s per side", "Static", 30, "Drop hips low, reach back for ankle.")
                )
                Pair(recoveryExercises, recoveryExercises)
            }
        }
    }
}
