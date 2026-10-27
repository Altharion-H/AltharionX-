package com.example

import com.example.data.CharacterCategory
import com.example.data.CharacterRepository
import com.example.data.WorkoutProtocolGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun test30CharacterRosterAndBadges() {
        assertEquals(30, CharacterRepository.characters.size)

        // Verify each character has a unique sovereign badge and complete dossier fields
        CharacterRepository.characters.forEach { character ->
            assertNotNull("Character ${character.name} should have a sovereign badge", character.sovereignBadge)
            assertTrue("Badge title should not be blank", character.sovereignBadge.title.isNotBlank())
            assertTrue("Weekly split should not be blank", character.weeklySplit.isNotBlank())
            assertTrue("Exercise style should not be blank", character.exerciseStyle.isNotBlank())
            assertTrue("Caloric strategy should not be blank", character.caloricStrategy.isNotBlank())
            assertTrue("Protein per kg should be > 1.5", character.proteinPerKg >= 2.0)
        }
    }

    @Test
    fun testAllFourCategoriesRepresented() {
        val categories = CharacterRepository.characters.map { it.category }.toSet()
        assertEquals(4, categories.size)
        assertTrue(categories.contains(CharacterCategory.MODERN_WEBTOON))
        assertTrue(categories.contains(CharacterCategory.ABSOLUTE_GIANTS))
        assertTrue(categories.contains(CharacterCategory.PEAK_AESTHETICS))
        assertTrue(categories.contains(CharacterCategory.COMICALLY_MAJESTIC))
    }

    @Test
    fun testHellModeOverloadModifiers() {
        val jinwoo = CharacterRepository.getCharacterById("jinwoo")
        val standardWorkouts = WorkoutProtocolGenerator.generate30Days(jinwoo, isHellMode = false)
        val hellWorkouts = WorkoutProtocolGenerator.generate30Days(jinwoo, isHellMode = true)

        assertEquals(30, standardWorkouts.size)
        assertEquals(30, hellWorkouts.size)

        // Check Day 1
        val standardDay1 = standardWorkouts[0]
        val hellDay1 = hellWorkouts[0]

        // Sets should be increased by 2
        assertEquals(standardDay1.exercisesGym[0].sets + 2, hellDay1.exercisesGym[0].sets)
        // Rest should be reduced by 30%
        val expectedRest = (standardDay1.exercisesGym[0].restSeconds * 0.70).toInt().coerceAtLeast(35)
        assertEquals(expectedRest, hellDay1.exercisesGym[0].restSeconds)
        // Boss quest should contain HELL MODE
        assertTrue(hellDay1.penaltyBossQuest.contains("HELL MODE"))
    }
}
