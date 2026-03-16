package com.prod.t_match.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.prod.auth.domain.gateway.AuthSessionStore
import com.prod.common.api.gateway.NotificationGateway
import com.prod.common.api.model.ApiOperationResult
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FcmTokenRegistrar(
	private val authSessionStore: AuthSessionStore,
	private val notificationGateway: NotificationGateway
) {

	suspend fun syncCurrentTokenIfAuthorized() {
		if (authSessionStore.get() == null) {
			return
		}

		val token = fetchCurrentToken() ?: return

		registerToken(token)
	}

	suspend fun registerTokenIfAuthorized(token: String) {
		if (authSessionStore.get() == null) {
			return
		}

		registerToken(token)
	}

	suspend fun registerToken(token: String) {
		when (val result = notificationGateway.registerDeviceToken(token)) {
			is ApiOperationResult.Success -> {
				Log.d(TAG, "FCM token registered")
			}

			is ApiOperationResult.ValidationError -> {
				Log.w(TAG, "FCM token registration validation error: ${result.message}")
			}

			is ApiOperationResult.ApiError -> {
				Log.w(TAG, "FCM token registration failed: ${result.reason} ${result.message.orEmpty()}")
			}

			ApiOperationResult.NoInternet -> {
				Log.w(TAG, "FCM token registration skipped: no internet")
			}
		}
	}

	private suspend fun fetchCurrentToken(): String? {
		return suspendCancellableCoroutine { continuation ->
			FirebaseMessaging.getInstance().token
				.addOnSuccessListener { token ->
					continuation.resume(token)
				}
				.addOnFailureListener { error ->
					Log.w(TAG, "Failed to fetch FCM token", error)
					continuation.resume(null)
				}
		}
	}

	private companion object {
		const val TAG = "FcmTokenRegistrar"
	}
}
