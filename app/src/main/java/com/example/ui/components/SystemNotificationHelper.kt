package com.example.ui.components

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object SystemNotificationHelper {

    const val CHANNEL_DAILY_QUEST = "arise_daily_quest"
    const val CHANNEL_PENALTY_ZONE = "arise_penalty_zone"
    const val CHANNEL_STREAK = "arise_streak_evolution"

    private const val NOTIF_ID_DAILY_QUEST = 1001
    private const val NOTIF_ID_PENALTY = 1002
    private const val NOTIF_ID_STREAK = 1003
    private const val NOTIF_ID_CAMERA = 1004
    private const val NOTIF_ID_ROADWORK = 1005

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            // 1. Daily Quest Channel
            val dailyQuestChannel = NotificationChannel(
                CHANNEL_DAILY_QUEST,
                "Solo Leveling Daily Quest Dispatches",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily 30-Day contract quests, rest timers, and exercise progressions"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }

            // 2. Penalty Zone Alarm Channel
            val penaltyChannel = NotificationChannel(
                CHANNEL_PENALTY_ZONE,
                "Penalty Zone Emergency Lockdown Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent Duolingo-style warnings before 4-hour desert penalty lockout"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            }

            // 3. Streak Evolution Channel
            val streakChannel = NotificationChannel(
                CHANNEL_STREAK,
                "Hunter Streak Evolution & Aura Ranks",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly streak flames, co-op invites, and Sovereign Hunter rank promotions"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(dailyQuestChannel)
            notificationManager.createNotificationChannel(penaltyChannel)
            notificationManager.createNotificationChannel(streakChannel)
        }
    }

    private fun canPostNotification(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    private fun createLaunchIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    fun sendDailyQuestNotification(context: Context, dayNumber: Int, characterName: String) {
        if (!canPostNotification(context)) return
        val pendingIntent = createLaunchIntent(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_QUEST)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚔️ SYSTEM QUEST ACTIVE: DAY $dayNumber")
            .setContentText("Blueprint: $characterName. Show all reps live on camera to complete!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "HUNTER AWAKENING: Day $dayNumber is unsealed. The System requires real-time camera validation for all prescribed sets. Do not slack off or face the Penalty Zone."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_DAILY_QUEST, notification)
    }

    fun sendPenaltyWarningNotification(context: Context, hoursLeft: Int) {
        if (!canPostNotification(context)) return
        val pendingIntent = createLaunchIntent(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_PENALTY_ZONE)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 EMERGENCY: PENALTY ZONE LOCKOUT IN $hoursLeft HOURS!")
            .setContentText("The System detected uncompleted daily quests. Complete reps on camera now!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "WARNING: If daily exercises are not validated before midnight, your terminal will be LOCKED into the 4-Hour Desert Penalty Zone (100 Burpees, 200 Squats, 5km Roadwork). ARISE and finish now!"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_PENALTY, notification)
    }

    fun sendStreakEvolutionNotification(context: Context, streakDays: Int, tierName: String) {
        if (!canPostNotification(context)) return
        val pendingIntent = createLaunchIntent(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_STREAK)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔥 STREAK FLAME EVOLVED: $streakDays DAYS!")
            .setContentText("Aura Tier: $tierName. Co-op Hunter network capabilities boosted.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "CONGRATULATIONS HUNTER! Your $streakDays-Day streak has unlocked the $tierName. Maintain the Monarch Contract to preserve your Sovereign status."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_STREAK, notification)
    }

    fun sendCameraRepSuccessNotification(context: Context, exerciseName: String, reps: Int) {
        if (!canPostNotification(context)) return
        val pendingIntent = createLaunchIntent(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_QUEST)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚡ CAMERA REP VALIDATION CLEARED!")
            .setContentText("$exerciseName: $reps Reps verified by Computer Vision (+25 XP).")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_CAMERA, notification)
    }

    fun sendRoadworkNotification(context: Context, distanceKm: Double, paceText: String) {
        if (!canPostNotification(context)) return
        val pendingIntent = createLaunchIntent(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_QUEST)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🏃 5KM ROADWORK MILESTONE RECORDED")
            .setContentText("Distance: ${String.format("%.2f", distanceKm)} km | Pace: $paceText")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_ROADWORK, notification)
    }
}
