package killua.dev.twitterdownloader.Model

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int = 0, val message: String) : NetworkResult<Nothing>()
}