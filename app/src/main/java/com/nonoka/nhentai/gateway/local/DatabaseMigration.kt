package com.nonoka.nhentai.gateway.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE `filter` (`id` TEXT NOT NULL, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
    }
}
