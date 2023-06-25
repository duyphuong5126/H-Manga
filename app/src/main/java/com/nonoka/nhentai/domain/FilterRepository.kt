package com.nonoka.nhentai.domain

interface FilterRepository {
    suspend fun activateFilter(filter: String): Boolean
    suspend fun deactivateFilter(filter: String): Boolean
    suspend fun getActiveFilters(): List<String>
}