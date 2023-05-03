package cu.suitetecsa.sdk.nauta.data.model

sealed class ResultType<T> {

    data class Success<T>(val result: T) : ResultType<T>()
    data class Error<T>(val error: Throwable) : ResultType<T>()
}