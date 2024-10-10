package com.example.trainalertsample.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RouteEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
}

