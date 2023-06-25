package com.nonoka.nhentai.gateway

import com.nonoka.nhentai.domain.FilterRepository
import com.nonoka.nhentai.gateway.local.datasource.FilterLocalDataSource
import javax.inject.Inject

class FilterRepositoryImpl @Inject constructor(
    private val filterLocalDataSource: FilterLocalDataSource
) : FilterRepository {
    override suspend fun activateFilter(filter: String): Boolean =
        filterLocalDataSource.activateFilter(filter)

    override suspend fun deactivateFilter(filter: String): Boolean =
        filterLocalDataSource.deactivateFilter(filter)

    override suspend fun getActiveFilters(): List<String> = filterLocalDataSource.getActiveFilters()

    override suspend fun getAllFilters(): List<String> = filterLocalDataSource.getAllFilters()
}