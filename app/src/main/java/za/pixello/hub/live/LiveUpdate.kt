package za.pixello.hub.live

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.pixello.hub.MainActivity
import za.pixello.hub.R
import za.pixello.hub.server.ENDPOINTS
import za.pixello.hub.server.Status
import za.pixello.hub.server.check

/**
 * Live Update demo: checks every home-server service one by one and shows progress as an
 * Android 16+ promoted ongoing notification. One UI shows these in the Now Bar and lock screen.
 */
object LiveUpdate {
    private const val CHANNEL = "live_updates"
    private const val ID = 1001
    private var job: Job? = null

    fun canPromote(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return false
        return context.getSystemService(NotificationManager::class.java).canPostPromotedNotifications()
    }

    fun start(context: Context, scope: CoroutineScope) {
        val app = context.applicationContext
        val nm = app.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Live updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Progress of server sweeps"
                setSound(null, null)
            },
        )
        job?.cancel()
        job = scope.launch {
            val total = ENDPOINTS.size
            var up = 0
            ENDPOINTS.forEachIndexed { i, endpoint ->
                nm.notify(ID, progress(app, i, total, "Checking ${endpoint.name}…"))
                if (check(endpoint) is Status.Up) up++
                delay(1_500) // Slow enough to watch in the Now Bar.
            }
            nm.notify(ID, done(app, "$up of $total services up"))
        }
    }

    private fun openApp(context: Context) = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE,
    )

    private fun progress(context: Context, step: Int, total: Int, text: String): Notification =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            promoted(context, step, total, text)
        } else {
            Notification.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_hub)
                .setContentTitle("Server sweep")
                .setContentText(text)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(total, step, false)
                .setContentIntent(openApp(context))
                .build()
        }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun promoted(context: Context, step: Int, total: Int, text: String): Notification {
        val style = Notification.ProgressStyle()
            .setStyledByProgress(true)
            .setProgressSegments(List(total) { Notification.ProgressStyle.Segment(1) })
            .setProgress(step)
        return Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_hub)
            .setContentTitle("Server sweep")
            .setContentText(text)
            .setShortCriticalText("$step/$total")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setRequestPromotedOngoing(true)
            .setStyle(style)
            .setContentIntent(openApp(context))
            .build()
    }

    private fun done(context: Context, text: String): Notification =
        Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_hub)
            .setContentTitle("Server sweep finished")
            .setContentText(text)
            .setAutoCancel(true)
            .setTimeoutAfter(60_000)
            .setContentIntent(openApp(context))
            .build()
}
