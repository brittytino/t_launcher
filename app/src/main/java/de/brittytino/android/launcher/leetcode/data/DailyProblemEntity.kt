package de.brittytino.android.launcher.leetcode.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_problems")
data class DailyProblemEntity(
    @PrimaryKey val date: String,
    val title: String,
    val difficulty: String,
    val titleSlug: String,
    val frontendId: String,
    val link: String? = null
)
