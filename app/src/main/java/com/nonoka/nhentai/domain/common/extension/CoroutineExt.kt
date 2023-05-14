package com.nonoka.nhentai.domain.common.extension

import com.nonoka.nhentai.domain.Resource

/**
 * Execute a suspend function, wrap its result in a Result object with a desired data.
 *
 * @param executor: The function to be executed. The function must return a value type of T.
 *
 * @return Success<T> if the action is executed successfully, otherwise Error<T>.
 */
suspend fun <T : Any> executeForResult(executor: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(executor.invoke())
    } catch (error: Throwable) {
        Resource.Error(error)
    }
}

/**
 * Execute a suspend function, wrap its result in a Result object without a desired data.
 *
 * @param executor: The function to be executed. The function is not expected to return anything.
 *
 * @return Success<Unit> if the action is executed successfully, otherwise Error<Unit>.
 */
suspend fun execute(executor: suspend () -> Unit): Resource<Unit> {
    return try {
        Resource.Success(executor.invoke())
    } catch (error: Throwable) {
        Resource.Error(error)
    }
}