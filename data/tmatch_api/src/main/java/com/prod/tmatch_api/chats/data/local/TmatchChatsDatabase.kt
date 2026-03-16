package com.prod.tmatch_api.chats.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
	entities = [ChatEntity::class, ChatUserEntity::class],
	version = 2,
	exportSchema = false
)
abstract class TmatchChatsDatabase : RoomDatabase() {
	abstract fun chatPreviewDao(): ChatPreviewDao
}
