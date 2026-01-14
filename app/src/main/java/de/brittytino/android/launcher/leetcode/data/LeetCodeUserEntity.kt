package de.brittytino.android.launcher.leetcode.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leetcode_users")
data class LeetCodeUserEntity(
    @PrimaryKey val username: String,
    val realName: String?,
    val avatarUrl: String?,
    val totalSolved: Int,
    val easySolved: Int,
    val mediumSolved: Int,
    val hardSolved: Int,
    val ranking: Int,
    val submissionCalendarJson: String, // Stringified JSON of timestamp -> count
    val isMe: Boolean,
    val lastUpdated: Long
)
