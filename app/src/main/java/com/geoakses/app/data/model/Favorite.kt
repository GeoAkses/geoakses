package com.geoakses.app.data.model

import androidx.room.Entity

object FavoriteKind {
    const val ROUTE = "route"
    const val STOP = "stop"
}

@Entity(tableName = "favorites", primaryKeys = ["itemId", "kind"])
data class Favorite(
    val itemId: String,
    val kind: String,
    val displayName: String,
    val subtitle: String,
    val savedAt: Long
)
