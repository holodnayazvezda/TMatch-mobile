package com.prod.matching.data

import android.content.Context
import android.content.SharedPreferences
import com.prod.matching.domain.gateway.PendingMatchActionStore
import com.prod.matching.domain.model.MatchAction
import com.prod.matching.domain.model.PendingMatchAction
import org.json.JSONArray
import org.json.JSONObject

class SharedPrefsPendingMatchActionStore(
	context: Context
) : PendingMatchActionStore {

	private val preferences: SharedPreferences =
		context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

	override fun getAll(): List<PendingMatchAction> {
		val serialized = preferences.getString(KEY_PENDING_ACTIONS, null).orEmpty()
		if (serialized.isBlank()) {
			return emptyList()
		}

		// Corrupted local state should not break matching; fall back to an empty queue.
		return runCatching {
			val jsonArray = JSONArray(serialized)
			buildList(jsonArray.length()) {
				for (index in 0 until jsonArray.length()) {
					val item = jsonArray.optJSONObject(index) ?: continue
					val profileId = item.optString(KEY_PROFILE_ID, "")
					if (profileId.isBlank()) {
						continue
					}

					val action = item.optString(KEY_ACTION, "").toMatchActionOrNull() ?: continue
					add(
						PendingMatchAction(
							profileId = profileId,
							recommendationId = item.optString(KEY_RECOMMENDATION_ID).takeIf { it.isNotBlank() },
							action = action
						)
					)
				}
			}
		}.getOrDefault(emptyList())
	}

	override fun saveAll(actions: List<PendingMatchAction>) {
		if (actions.isEmpty()) {
			clear()
			return
		}

		// A single JSON blob is enough here because the queue is small and order matters.
		val payload = JSONArray().apply {
			actions.forEach { action ->
				put(
					JSONObject().apply {
						put(KEY_PROFILE_ID, action.profileId)
						put(KEY_ACTION, action.action.toStorageValue())
						put(KEY_RECOMMENDATION_ID, action.recommendationId)
					}
				)
			}
		}

		preferences.edit()
			.putString(KEY_PENDING_ACTIONS, payload.toString())
			.apply()
	}

	override fun clear() {
		preferences.edit()
			.remove(KEY_PENDING_ACTIONS)
			.apply()
	}

	private fun String.toMatchActionOrNull(): MatchAction? {
		return when (this) {
			"like" -> MatchAction.Like
			"dislike" -> MatchAction.Dislike
			"ban" -> MatchAction.Ban
			"skip" -> MatchAction.Skip
			else -> null
		}
	}

	private fun MatchAction.toStorageValue(): String {
		return when (this) {
			MatchAction.Like -> "like"
			MatchAction.Dislike -> "dislike"
			MatchAction.Ban -> "ban"
			MatchAction.Skip -> "skip"
		}
	}

	private companion object {
		const val PREFERENCES_NAME = "matching_pending_actions"
		const val KEY_PENDING_ACTIONS = "pending_actions"
		const val KEY_PROFILE_ID = "profile_id"
		const val KEY_RECOMMENDATION_ID = "recommendation_id"
		const val KEY_ACTION = "action"
	}
}
