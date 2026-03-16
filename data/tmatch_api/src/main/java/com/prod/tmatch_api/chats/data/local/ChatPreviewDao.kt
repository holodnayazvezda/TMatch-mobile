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
		SELECT * FROM chats
		WHERE ownerUserId = :ownerUserId
		ORDER BY lastMessageCreatedAt IS NULL ASC, lastMessageCreatedAt DESC, chatId ASC
		"""
	)
	@Transaction
	abstract fun observeChats(ownerUserId: String): Flow<List<ChatWithUserEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	protected abstract suspend fun insertChats(items: List<ChatEntity>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	protected abstract suspend fun insertUsers(items: List<ChatUserEntity>)

	@Query("DELETE FROM chats WHERE ownerUserId = :ownerUserId")
	protected abstract suspend fun deleteChatsByOwnerUserId(ownerUserId: String)

	@Query("DELETE FROM chat_users WHERE ownerUserId = :ownerUserId")
	protected abstract suspend fun deleteUsersByOwnerUserId(ownerUserId: String)

	@Transaction
	open suspend fun deleteByOwnerUserId(ownerUserId: String) {
		deleteChatsByOwnerUserId(ownerUserId)
		deleteUsersByOwnerUserId(ownerUserId)
	}

	@Transaction
	open suspend fun replaceChats(
		ownerUserId: String,
		chats: List<ChatEntity>,
		users: List<ChatUserEntity>
	) {
		deleteByOwnerUserId(ownerUserId)
		if (users.isNotEmpty()) {
			insertUsers(users)
		}
		if (chats.isNotEmpty()) {
			insertChats(chats)
		}
	}
}
