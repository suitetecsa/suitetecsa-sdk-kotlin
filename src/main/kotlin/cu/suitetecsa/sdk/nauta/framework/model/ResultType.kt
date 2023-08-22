package cu.suitetecsa.sdk.nauta.framework.model

sealed class ResultType<T> {

    data class Success<T>(val result: T) : ResultType<T>()
    data class Failure<T>(val throwable: Throwable) : ResultType<T>()
}