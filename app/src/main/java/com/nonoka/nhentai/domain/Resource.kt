package com.nonoka.nhentai.domain

import com.nonoka.nhentai.domain.common.extension.execute
import com.nonoka.nhentai.domain.common.extension.executeForResult

/**
 * Result of any action. It can be a  or an Error result with a Throwable.
 */
sealed class Resource<T : Any> {
    /**
     * Success result with a desired data.
     *
     * @param data: The data that's expected when the action completes.
     */
    data class Success<Data : Any>(val data: Data) : Resource<Data>()

    /**
     * Failure result with a desired data.
     *
     * @param error: The error that causes the failure.
     */
    data class Error<Data : Any>(val error: Throwable) : Resource<Data>()

    /**
     * Transform the data of a Success result to a value of the type Output.
     * If the current result is Error, nothing will happen.
     *
     * @param outputFunc: The function that gets the data from the current Success result and returns an Output.
     * If this function causes an exception, the result will be an Error.
     *
     * @return Can be either Error<T> or Success<Output> or Error<Output>
     */
    suspend fun <Output : Any> map(outputFunc: suspend (T) -> Output): Resource<Output> {
        return when (this) {
            is Success -> executeForResult { outputFunc.invoke(data) }
            is Error -> Error(error = this.error)
        }
    }

    /**
     * Transform the data of a Success result to another value of the type Output.
     * If the current result is Error, nothing will happen.
     *
     * @param outputFunc: The function that gets the data from the current Success result and return a Result<Output>.
     * No exception should be thrown.
     *
     * @return Can be either Error<T> or Success<Output>
     */
    suspend fun <Output : Any> flatMap(outputFunc: suspend (T) -> Resource<Output>): Resource<Output> {
        return when (this) {
            is Success -> outputFunc.invoke(this.data)
            is Error -> Error(error = this.error)
        }
    }

    /**
     * Combine two Result object.
     * If the current result is Error, nothing will happen.
     *
     * @param resource1: The Result object with that the current Result is combined.
     *
     * For example, Result<A> zips with Result<B> = Result<Pair<A, B>>
     *
     * @return Can be either Error<T> or Success<Output>
     */
    suspend fun <T1 : Any> zipWith(resource1: Resource<T1>): Resource<Pair<T, T1>> {
        return when (this) {
            is Success -> resource1.map { Pair(this.data, it) }
            is Error -> Error(error = this.error)
        }
    }

    /**
     * Execute an action when the Result is a Success.
     *
     * @param onSuccess: The function to be executed in case the Result is a Success.
     * This function receives the data of that Success result.
     *
     * @return If the onSuccess function is executed successfully, return the Success object that call this method (doOnSuccess).
     * Otherwise return an Error object that contains the exception thrown by the onSuccess function.
     */
    suspend fun doOnSuccess(onSuccess: suspend (T) -> Unit): Resource<T> {
        return if (this is Success) {
            execute { onSuccess.invoke(this.data) }.flatMap { this }
        } else {
            this
        }
    }

    /**
     * Execute an action when the Result is an Error.
     *
     * @param onError: The function to be executed in case the Result is a Error.
     * This function receives the Throwable of that Error result.
     *
     * @return If the onError function is executed successfully, return the Error object that call this method (doOnError).
     * Otherwise return an Error object that contains the exception thrown by the onError function.
     */
    suspend fun doOnError(onError: suspend (Throwable) -> Unit): Resource<T> {
        return if (this is Error) {
            execute { onError.invoke(this.error) }.flatMap { this }
        } else {
            this
        }
    }
}