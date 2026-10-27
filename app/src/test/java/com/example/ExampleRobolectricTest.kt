package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ARISE", appName)
    }

    @Test
    fun `strict zero-progress initial state and contract lifecycle`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = UserPreferences(context)

        // Reset to initial
        prefs.resetContractForNewMonth()
        prefs.totalXp = 0
        prefs.streakDays = 0
        prefs.equippedBadge = ""

        // STRICT ZERO-PROGRESS INITIAL STATE
        assertEquals(0, prefs.getCompletedDays().size)
        assertFalse(prefs.isContractSigned)
        assertEquals(0, prefs.totalXp)
        assertEquals(0, prefs.streakDays)
        assertEquals("", prefs.equippedBadge)
        assertEquals("UNCONTRACTED E-RANK", prefs.getHunterRank())

        // Sign contract with Jinwoo
        prefs.signContract("jinwoo", 75f, 178f, true, "content://media/photo1")
        assertTrue(prefs.isContractSigned)
        assertEquals("jinwoo", prefs.activeCharacterId)
        assertEquals("E-RANK AWAKENED", prefs.getHunterRank())

        // Complete Day 1
        prefs.setDayCompleted(1, true)
        prefs.addXp(200)
        assertEquals(1, prefs.getCompletedDays().size)
        assertEquals(1, prefs.streakDays)
        assertEquals("D-RANK HUNTER", prefs.getHunterRank())

        // Award Sovereign Badge on Day 30 evaluation for Jinwoo
        prefs.addEarnedBadge("SHADOW MONARCH")
        assertEquals("SHADOW MONARCH", prefs.equippedBadge)
        assertEquals("SHADOW MONARCH / S-RANK", prefs.getHunterRank())

        // Test non-Jinwoo character badge (e.g. Toji Fushiguro)
        prefs.addEarnedBadge("HEAVENLY RESTRICTION ASSASSIN")
        assertEquals("HEAVENLY RESTRICTION ASSASSIN", prefs.equippedBadge)
        assertEquals("HEAVENLY RESTRICTION ASSASSIN / S-RANK", prefs.getHunterRank())

        // Test Asta badge
        prefs.addEarnedBadge("ANTI-MAGIC DEMON TITAN")
        assertEquals("ANTI-MAGIC DEMON TITAN", prefs.equippedBadge)
        assertEquals("ANTI-MAGIC DEMON TITAN / S-RANK", prefs.getHunterRank())

        // Test Live Camera Repetition Verification
        assertFalse(prefs.isExerciseCameraVerified(1, "pushup_1"))
        prefs.markExerciseCameraVerified(1, "pushup_1", 15)
        assertTrue(prefs.isExerciseCameraVerified(1, "pushup_1"))
        assertEquals(15, prefs.getVerifiedReps(1, "pushup_1"))
        assertTrue(prefs.getCheckedExercises(1).contains("pushup_1"))
    }

    @Test
    fun `test system notification channels initialization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.ui.components.SystemNotificationHelper.initNotificationChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channel = nm.getNotificationChannel(com.example.ui.components.SystemNotificationHelper.CHANNEL_DAILY_QUEST)
        org.junit.Assert.assertNotNull(channel)
    }

    @Test
    fun `test memory lane 30-day time-lapse frame generation and daily photo propagation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = UserPreferences(context)
        prefs.resetContractForNewMonth()

        prefs.saveDailyPhoto(1, "file:///cache/day1.jpg")
        prefs.saveDailyPhoto(14, "file:///cache/day14.jpg")
        prefs.saveDailyPhoto(30, "file:///cache/day30.jpg")

        val toji = com.example.data.CharacterRepository.getCharacterById("toji")
        val frames = com.example.data.GeminiVisionService.buildMemoryLaneFrames(toji, prefs.getAllDailyPhotos())

        assertEquals(30, frames.size)
        assertEquals(1, frames.first().dayNumber)
        assertEquals("file:///cache/day1.jpg", frames.first().photoUri)
        // Day 7 carries forward Day 1 photo until Day 14 is logged
        assertEquals("file:///cache/day1.jpg", frames[6].photoUri)
        assertEquals("file:///cache/day14.jpg", frames[13].photoUri)
        assertEquals("file:///cache/day30.jpg", frames.last().photoUri)
        assertEquals(1.0f, frames.last().morphProgress, 0.01f)
    }
}

