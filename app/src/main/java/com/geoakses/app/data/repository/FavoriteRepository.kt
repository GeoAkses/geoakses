package com.geoakses.app.data.repository

import com.geoakses.app.data.local.dao.FavoriteDao
import com.geoakses.app.data.model.Favorite
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {
    fun observeFavorites(): Flow<List<Favorite>> = favoriteDao.observeFavorites()

    suspend fun toggle(itemId: String, kind: String, displayName: String, subtitle: String) {
        if (favoriteDao.contains(itemId, kind)) {
            favoriteDao.remove(itemId, kind)
        } else {
            favoriteDao.save(
                Favorite(
                    itemId = itemId,
                    kind = kind,
                    displayName = displayName,
                    subtitle = subtitle,
                    savedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun remove(itemId: String, kind: String) = favoriteDao.remove(itemId, kind)
}
