package com.prod.tmatch_api.auth.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

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
	}
}
