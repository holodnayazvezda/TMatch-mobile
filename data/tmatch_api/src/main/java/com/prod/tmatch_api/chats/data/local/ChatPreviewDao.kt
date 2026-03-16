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

	@Query(
		"""
		SELECT * FROM chats
		WHERE ownerUserId = :ownerUserId AND chatId = :chatId
		LIMIT 1
		"""
	)
	@Transaction
	abstract fun observeChat(ownerUserId: String, chatId: String): Flow<ChatWithUserEntity?>

	@Query(
		"""
		SELECT * FROM chat_messages
		WHERE ownerUserId = :ownerUserId AND chatId = :chatId
		ORDER BY createdAt ASC
		"""
	)
	abstract fun observeChatMessages(ownerUserId: String, chatId: String): Flow<List<ChatMessageEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	protected abstract suspend fun insertChats(items: List<ChatEntity>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	protected abstract suspend fun insertUsers(items: List<ChatUserEntity>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	abstract suspend fun insertMessages(items: List<ChatMessageEntity>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	abstract suspend fun insertMessage(item: ChatMessageEntity)

	@Query(
		"""
		UPDATE chats
		SET lastMessageId = :messageId,
			lastMessageText = :text,
			lastMessageCreatedAt = :createdAt
		WHERE ownerUserId = :ownerUserId AND chatId = :chatId
		"""
	)
	abstract suspend fun updateChatLastMessage(
		ownerUserId: String,
		chatId: String,
		messageId: String,
		text: String,
		createdAt: String
	)

	@Query("DELETE FROM chats WHERE ownerUserId = :ownerUserId")
	protected abstract suspend fun deleteChatsByOwnerUserId(ownerUserId: String)

	@Query("DELETE FROM chat_users WHERE ownerUserId = :ownerUserId")
	protected abstract suspend fun deleteUsersByOwnerUserId(ownerUserId: String)

	@Query("DELETE FROM chat_messages WHERE ownerUserId = :ownerUserId")
	protected abstract suspend fun deleteMessagesByOwnerUserId(ownerUserId: String)

	@Query("DELETE FROM chats")
	protected abstract suspend fun deleteAllChats()

	@Query("DELETE FROM chat_users")
	protected abstract suspend fun deleteAllUsers()

	@Query("DELETE FROM chat_messages")
	protected abstract suspend fun deleteAllMessages()

	@Transaction
	open suspend fun deleteByOwnerUserId(ownerUserId: String) {
		deleteMessagesByOwnerUserId(ownerUserId)
		deleteChatsByOwnerUserId(ownerUserId)
		deleteUsersByOwnerUserId(ownerUserId)
	}

	@Transaction
	open suspend fun clearAllData() {
		deleteAllMessages()
		deleteAllChats()
		deleteAllUsers()
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
