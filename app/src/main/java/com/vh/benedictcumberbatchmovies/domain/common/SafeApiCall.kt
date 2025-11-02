package com.vh.benedictcumberbatchmovies.domain.common

import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.UnknownHostException

/**
 * Centralized exception → DataState.Error mapping for API calls.
 */
suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): DataState<T> {
    return try {
        val result = block()
        DataState.Success(result)
    } catch (e: UnknownHostException) { // DNS / no network
        DataState.Error("No internet connection.", cause = e)
    } catch (e: IOException) {          // timeouts, connection drops
        DataState.Error("Network error. Please try again.", cause = e)
    } catch (e: retrofit2.HttpException) {        // HTTP 4xx / 5xx
        val code = e.code()
        val msg = when {
            code >= 500 -> "Server is unreachable (HTTP $code)."
            code == 404 -> "Requested resource was not found (404)."
            code == 401 || code == 403 -> "You are not authorized (HTTP $code)."
            else -> "Request failed (HTTP $code)."
        }
        DataState.Error(msg, code = code, cause = e)
    } catch (e: SerializationException) { // JSON parse
        DataState.Error("We couldn’t read the server response.", cause = e)
    } catch (e: Exception) {              // anything else
        DataState.Error(e.message ?: "Something went wrong.", cause = e)
    }
}