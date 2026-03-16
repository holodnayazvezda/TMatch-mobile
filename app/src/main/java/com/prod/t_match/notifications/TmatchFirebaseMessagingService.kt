package com.prod.t_match.notifications

import android.Manifest
import android.graphics.BitmapFactory
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.gateway.NotificationGateway
import com.prod.t_match.MainActivity
import com.prod.t_match.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import org.koin.android.ext.android.inject

class TmatchFirebaseMessagingService : FirebaseMessagingService() {

	private val notificationGateway by inject<NotificationGateway>()
	private val authSessionStore by inject<AuthSessionStore>()
	private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		Log.d(TAG, "New FCM token received")
		serviceScope.launch {
			FcmTokenRegistrar(
				authSessionStore = authSessionStore,
				notificationGateway = notificationGateway
			).registerTokenIfAuthorized(token)
		}
	}

	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)
		Log.d(
			TAG,
			"FCM message received: from=${message.from} data=${message.data} notificationTitle=${message.notification?.title} notificationBody=${message.notification?.body}"
		)
		showNotification(message)
	}

	override fun onDestroy() {
		serviceScope.cancel()
		super.onDestroy()
	}

	private companion object {
		const val TAG = "TmatchFcmService"
		const val CHANNEL_ID = "tmatch_push_messages"
		val nextNotificationId = AtomicInteger(1)
	}

	private fun showNotification(message: RemoteMessage) {
		if (
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
			ContextCompat.checkSelfPermission(
				this,
				Manifest.permission.POST_NOTIFICATIONS
			) != PackageManager.PERMISSION_GRANTED
		) {
			Log.w(TAG, "Notification permission is not granted; skipping system notification")
			return
		}

		createNotificationChannelIfNeeded()

		val title = message.notification?.title
			?: message.data["title"]
			?: getString(R.string.push_notification_fallback_title)
		val body = message.notification?.body
			?: message.data["body"]
			?: getString(R.string.push_notification_fallback_body)
		val notificationId = nextNotificationId.getAndIncrement()
		val contentIntent = PendingIntent.getActivity(
			this,
			notificationId,
			Intent(this, MainActivity::class.java).apply {
				flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			},
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)

		val notification = NotificationCompat.Builder(this, CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_launcher_round)
			.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round))
			.setContentTitle(title)
			.setContentText(body)
			.setStyle(NotificationCompat.BigTextStyle().bigText(body))
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setAutoCancel(true)
			.setContentIntent(contentIntent)
			.build()

		NotificationManagerCompat.from(this).notify(notificationId, notification)
	}

	private fun createNotificationChannelIfNeeded() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			return
		}

		val notificationManager = getSystemService(NotificationManager::class.java)
		if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) {
			return
		}

		val channel = NotificationChannel(
			CHANNEL_ID,
			getString(R.string.push_notification_channel_name),
			NotificationManager.IMPORTANCE_HIGH
		).apply {
			description = getString(R.string.push_notification_channel_description)
		}

		notificationManager.createNotificationChannel(channel)
	}
}
