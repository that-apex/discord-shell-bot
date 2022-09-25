package net.thatapex.shellbot.vm.gcloud.api

import kotlinx.coroutines.delay
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

data class ApiResult<T> private constructor(
    val result: T?,
    val error: Throwable?
) {
    fun get(): T {
        if (error != null) {
            throw error
        }

        return result!!
    }

    companion object {
        fun <T> success(result: T): ApiResult<T> {
            return ApiResult(result, null)
        }

        fun <T> error(error: Throwable): ApiResult<T> {
            return ApiResult(null, error)
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun <T : Any> Future<T>.suspend(): ApiResult<T> {
    while (!isDone)
        delay(100)

    return this.wrapResult()
}

private fun <V> Future<V>.wrapResult(): ApiResult<V> =
    try {
        ApiResult.success(get())
    } catch (e: Exception) {
        ApiResult.error(
            when (e) {
                is ExecutionException -> e.cause!!
                else -> e
            }
        )
    }

