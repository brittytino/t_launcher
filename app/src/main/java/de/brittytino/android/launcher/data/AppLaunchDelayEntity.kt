package de.brittytino.android.launcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_launch_delay")
data class AppLaunchDelayEntity(
    @PrimaryKey
    val packageName: String,
    val delaySeconds: Int,
    val enabled: Boolean
)
