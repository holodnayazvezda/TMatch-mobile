package com.prod.t_match.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.gateway.NotificationGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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
	}

	override fun onDestroy() {
		serviceScope.cancel()
		super.onDestroy()
	}

	private companion object {
		const val TAG = "TmatchFcmService"
	}
}
