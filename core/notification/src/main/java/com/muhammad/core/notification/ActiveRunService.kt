package com.muhammad.core.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.muhammad.core.presentation.designsystem.R
import com.muhammad.core.presentation.ui.formatted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import kotlin.time.Duration
import com.muhammad.core.notification.R as NotificationR

@SuppressLint("NewApi")
class ActiveRunService : Service() {
    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }
    private val baseNotification by lazy {
        NotificationCompat.Builder(applicationContext, CHANNEL_ID).setSmallIcon(R.drawable.logo)
            .setContentTitle(
                getString(NotificationR.string.active_run)
            )
    }
    private val elapsedTime by inject<StateFlow<Duration>>()
    private var serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val activityClass = intent.getStringExtra(EXTRA_ACTIVITY_CLASS)
                    ?: throw IllegalArgumentException("No activity class provided")
                start(Class.forName(activityClass))
            }
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    private fun start(activityClass: Class<*>) {
        if (!isServiceActive.value) {
            _isServiceActive.value = true
            createNotificationChannel()
            val activityIntent = Intent(applicationContext, activityClass).apply {
                data = "runwell://active_run".toUri()
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val pendingIntent = TaskStackBuilder.create(applicationContext).run {
                addNextIntentWithParentStack(activityIntent)
                getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
            }
            val notification =
                baseNotification.setContentText("00:00:00").setContentIntent(pendingIntent).build()
            startForeground(1, notification)
            updateNotification()
        }
    }

    private fun updateNotification() {
        elapsedTime.onEach { elapsedTime ->
            val notification = baseNotification.setContentText(elapsedTime.formatted()).build()
            notificationManager.notify(1, notification)
        }.launchIn(serviceScope)
    }

    fun stop() {
        stopSelf()
        _isServiceActive.value = false
        serviceScope.cancel()
        serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(NotificationR.string.active_run),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive = _isServiceActive.asStateFlow()
        private const val CHANNEL_ID = "active_run"
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"
        private const val EXTRA_ACTIVITY_CLASS = "EXTRA_ACTIVITY_CLASS"
        fun createStartIntent(context: Context, activityClass: Class<*>): Intent {
            return Intent(context, ActiveRunService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ACTIVITY_CLASS, activityClass.name)
            }
        }

        fun createStopIntent(context: Context): Intent {
            return Intent(context, ActiveRunService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }
}