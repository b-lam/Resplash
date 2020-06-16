package com.b_lam.resplash.domain

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import com.b_lam.resplash.di.Properties
import com.b_lam.resplash.util.error
import okio.IOException
import retrofit2.HttpException

abstract class BasePagingSource<T : Any> : PagingSource<Int, T>() {

    abstract suspend fun getPage(page: Int, perPage: Int): List<T>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val pageNumber = params.key ?: 1
            val response = getPage(pageNumber, params.loadSize)
            LoadResult.Page(
                data = response,
                prevKey = null,
                nextKey = if (response.isEmpty() || response.size < params.loadSize)
                    null else pageNumber + 1
            )
        } catch (e: IOException) {
            error("An error occurred: $e")
            return LoadResult.Error(e)
        } catch (e: HttpException) {
            error("An error occurred: $e")
            return LoadResult.Error(e)
        }
    }

    companion object {

        val config = PagingConfig(
            pageSize = Properties.DEFAULT_PAGE_SIZE,
            initialLoadSize = Properties.DEFAULT_PAGE_SIZE,
            prefetchDistance = Properties.DEFAULT_PAGE_SIZE / 2,
            enablePlaceholders = false
        )
    }
}
