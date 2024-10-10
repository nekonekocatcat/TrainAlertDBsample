package com.example.trainalertsample.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RouteDao {
    // すべてのルートデータを取得するメソッド
    @Query("SELECT * FROM route_alert_data")
    fun loadAllRoute(): List<RouteEntity>

    // ルートデータを新規作成するメソッド
    @Insert(onConflict = OnConflictStrategy.ABORT) // 新規作成時のみ使用。既存データがあれば失敗。
    fun saveRoute(routeEntity: RouteEntity): Long

    // ルートデータを更新するメソッド
    @Update
    fun update(route: RouteEntity): Int

    // ルートデータを削除するメソッド
    @Delete
    fun deleteRoute(route: RouteEntity)
}
