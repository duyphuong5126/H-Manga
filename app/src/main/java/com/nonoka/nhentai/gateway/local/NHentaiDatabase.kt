package com.nonoka.nhentai.gateway.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nonoka.nhentai.gateway.local.dao.DoujinshiDao
import com.nonoka.nhentai.gateway.local.dao.FilterDao
import com.nonoka.nhentai.gateway.local.model.DoujinshiModel
import com.nonoka.nhentai.gateway.local.model.FilterModel

@Database(entities = [DoujinshiModel::class, FilterModel::class], version = 2)
abstract class NHentaiDatabase : RoomDatabase() {
    abstract fun doujinshiDao(): DoujinshiDao
    abstract fun filterDao(): FilterDao
}