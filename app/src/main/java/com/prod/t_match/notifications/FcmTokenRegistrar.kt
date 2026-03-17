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
	private val notificationGateway: NotificationGateway,
	private val logger: Logger = AndroidLogger
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
				logger.debug("FCM token registered")
			}

			is ApiOperationResult.ValidationError -> {
				logger.warn("FCM token registration validation error: ${result.message}")
			}

			is ApiOperationResult.ApiError -> {
				logger.warn("FCM token registration failed: ${result.reason} ${result.message.orEmpty()}")
			}

			ApiOperationResult.NoInternet -> {
				logger.warn("FCM token registration skipped: no internet")
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
					logger.warn("Failed to fetch FCM token", error)
					continuation.resume(null)
				}
		}
	}

	interface Logger {
		fun debug(message: String)

		fun warn(message: String) {
			warn(message, null)
		}

		fun warn(message: String, error: Throwable?)
	}

	private companion object {
		const val TAG = "FcmTokenRegistrar"
		val AndroidLogger = object : Logger {
			override fun debug(message: String) {
				Log.d(TAG, message)
			}

			override fun warn(message: String, error: Throwable?) {
				Log.w(TAG, message, error)
			}
		}
	}
}
