package com.nonoka.nhentai.gateway.local.datasource

import com.nonoka.nhentai.gateway.local.dao.FilterDao
import com.nonoka.nhentai.gateway.local.model.FilterModel
import javax.inject.Inject

interface FilterLocalDataSource {
    suspend fun activateFilter(filter: String): Boolean
    suspend fun deactivateFilter(filter: String): Boolean
    suspend fun getActiveFilters(): List<String>
}

class FilterLocalDataSourceImpl @Inject constructor(
    private val filterDao: FilterDao
) : FilterLocalDataSource {
    override suspend fun activateFilter(filter: String): Boolean {
        return if (filterDao.hasFilter(filter)) {
            filterDao.updateFilterActiveStatus(filter, 1) > 0
        } else {
            val model = FilterModel(id = filter, true)
            filterDao.addFilters(model).isNotEmpty()
        }
    }

    override suspend fun deactivateFilter(filter: String): Boolean {
        return if (filterDao.hasFilter(filter)) {
            filterDao.updateFilterActiveStatus(filter, 0) > 0
        } else {
            val model = FilterModel(id = filter, false)
            filterDao.addFilters(model).isNotEmpty()
        }
    }

    override suspend fun getActiveFilters(): List<String> {
        return filterDao.getActiveFilters().map { it.id }
    }
}
