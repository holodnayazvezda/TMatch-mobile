package com.prod.tmatch_api.chats.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ChatPreviewDao {
	@Query(
		"""
		SELECT * FROM chat_previews
		WHERE ownerUserId = :ownerUserId
		ORDER BY lastMessageCreatedAt IS NULL ASC, lastMessageCreatedAt DESC, chatId ASC
		"""
	)
	abstract fun observeChats(ownerUserId: String): Flow<List<ChatPreviewEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	protected abstract suspend fun insertAll(items: List<ChatPreviewEntity>)

	@Query("DELETE FROM chat_previews WHERE ownerUserId = :ownerUserId")
	abstract suspend fun deleteByOwnerUserId(ownerUserId: String)

	@Transaction
	open suspend fun replaceChats(
		ownerUserId: String,
		items: List<ChatPreviewEntity>
	) {
		deleteByOwnerUserId(ownerUserId)
		if (items.isNotEmpty()) {
			insertAll(items)
		}
	}
}
