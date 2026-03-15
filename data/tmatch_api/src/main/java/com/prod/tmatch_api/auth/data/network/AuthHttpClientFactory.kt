package com.prod.tmatch_api.auth.data.network

import android.util.Log
import com.prod.tmatch_api.data.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val HTTP_LOG_TAG = "AuthHttpClient"

fun createAuthHttpClient(
	baseUrl: String
): HttpClient {
	return HttpClient(OkHttp) {
		defaultRequest {
			url(baseUrl)
		}

		install(ContentNegotiation) {
			json(
				Json {
					ignoreUnknownKeys = true
					explicitNulls = false
				}
			)
		}

		if (BuildConfig.DEBUG) {
			install(Logging) {
				logger = object : Logger {
					override fun log(message: String) {
						Log.d(HTTP_LOG_TAG, message)
					}
				}
				level = LogLevel.BODY
				sanitizeHeader { header -> header == io.ktor.http.HttpHeaders.Authorization }
			}
		}
	}
}
