package com.nonoka.nhentai.gateway.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DoujinshiModel::class], version = 1)
abstract class NHentaiDatabase : RoomDatabase() {
    abstract fun doujinshiDao(): DoujinshiDao
}